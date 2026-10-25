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
    # Exposure 的提示逻辑整体放在 client 子包里，只被客户端事件与 client mixin 调用
    Path("src/main/java/com/otherworldinn/compat/exposure/client"),
    # 数据生成器只在 runData 的独立 JVM 里加载，永远不进专用服务端
    Path("src/main/java/com/otherworldinn/datagen"),
)

ALLOWED_CLIENT_FILES = {
    Path("src/main/java/com/otherworldinn/init/ModClientEvents.java"),
    Path("src/main/java/com/otherworldinn/init/ModKeyBindings.java"),
    Path("src/main/java/com/otherworldinn/init/ModTooltips.java"),
    # 已标注 Dist.CLIENT，只在客户端事件总线注册、只在客户端加载
    Path("src/main/java/com/otherworldinn/init/ModClientCommandEvents.java"),
    # 只由上面的 ModClientCommandEvents（Dist.CLIENT）注册，服务端不会加载
    Path("src/main/java/com/otherworldinn/command/PanoramixScreenshotCommands.java"),
    # 只被 XaeroMapBlocker（Dist.CLIENT）与两个 client mixin 调用，服务端不会加载；
    # 若将来有 common 代码调用它们，必须挪进 compat/xaero/client
    Path("src/main/java/com/otherworldinn/compat/xaero/XaeroCompatBridge.java"),
    Path("src/main/java/com/otherworldinn/compat/xaero/XaeroMapBlocker.java"),
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


def is_s2c_packet(path: Path) -> bool:
    """服务端只会发送 S2C 包、不会收到并执行它的 handler，client 引用位于 lambda 体内。"""
    return (
        path.parent == Path("src/main/java/com/otherworldinn/network/packet")
        and path.name.startswith("S2C")
    )


def is_allowed_client_file(path: Path, client_mixin_files: set[Path]) -> bool:
    relative = rel(path)
    if relative in ALLOWED_CLIENT_FILES or relative in client_mixin_files:
        return True
    if is_s2c_packet(relative):
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
