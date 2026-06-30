#!/usr/bin/env python3
import json
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src" / "main" / "java"
MIXIN_CONFIG = ROOT / "src" / "main" / "resources" / "otherworldinn.mixins.json"

CLIENT_IMPORTS = (
    "import net.minecraft.client.",
    "import com.otherworldinn.client.",
    "import com.github.tartaricacid.touhoulittlemaid.client.",
)

ALLOWED_CLIENT_PATH_PARTS = (
    Path("src/main/java/com/otherworldinn/client"),
    Path("src/main/java/com/otherworldinn/compat/waystones/client"),
    Path("src/main/java/com/otherworldinn/compat/jei"),
)

ALLOWED_CLIENT_FILES = {
    Path("src/main/java/com/otherworldinn/init/ModClientEvents.java"),
    Path("src/main/java/com/otherworldinn/init/ModKeyBindings.java"),
    Path("src/main/java/com/otherworldinn/init/ModTooltips.java"),
}


def rel(path: Path) -> Path:
    return path.relative_to(ROOT)


def has_client_import(text: str) -> bool:
    return any(pattern in text for pattern in CLIENT_IMPORTS)


def load_client_mixin_files() -> set[Path]:
    config = json.loads(MIXIN_CONFIG.read_text(encoding="utf-8"))
    return {
        Path("src/main/java/com/otherworldinn/mixin") / f"{name}.java"
        for name in config.get("client", [])
    }


def is_under(path: Path, parent: Path) -> bool:
    try:
        path.relative_to(parent)
        return True
    except ValueError:
        return False


def is_allowed_client_file(path: Path, client_mixin_files: set[Path]) -> bool:
    relative = rel(path)
    if relative in ALLOWED_CLIENT_FILES or relative in client_mixin_files:
        return True
    return any(is_under(relative, allowed) for allowed in ALLOWED_CLIENT_PATH_PARTS)


def check_source_imports(client_mixin_files: set[Path]) -> list[str]:
    errors = []
    for path in sorted(JAVA_ROOT.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        if not has_client_import(text):
            continue
        if is_allowed_client_file(path, client_mixin_files):
            continue
        errors.append(f"{rel(path)} imports client-only classes from a common-loaded location")
    return errors


def check_common_mixins() -> list[str]:
    errors = []
    config = json.loads(MIXIN_CONFIG.read_text(encoding="utf-8"))
    for mixin_name in config.get("mixins", []):
        path = JAVA_ROOT / "com" / "otherworldinn" / "mixin" / f"{mixin_name}.java"
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8")
        if has_client_import(text):
            errors.append(f"{rel(path)} is listed in common mixins but imports client-only classes")
    return errors


def main() -> int:
    client_mixin_files = load_client_mixin_files()
    errors = check_source_imports(client_mixin_files)
    errors.extend(check_common_mixins())
    if errors:
        print("Client boundary check failed:")
        for error in errors:
            print(f"  - {error}")
        print()
        print("Move client-only code under com.otherworldinn.client or list client-only mixins in the")
        print("'client' section of otherworldinn.mixins.json.")
        return 1
    print("Client boundary check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
