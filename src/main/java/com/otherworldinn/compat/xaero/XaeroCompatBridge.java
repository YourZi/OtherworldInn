package com.otherworldinn.compat.xaero;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModKeyBindings;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import java.lang.reflect.Field;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public final class XaeroCompatBridge {
    private static final String WORLD_MAP_GUI_PREFIX = "xaero.map.gui.";
    private static final String MINIMAP_SESSION_PREFIX = "xaero.hud.minimap.module.";
    private static final String MINIMAP_SESSION_SUFFIX = "MinimapSession";
    private static final String MINIMAP_RENDERER_CLASS = "xaero.hud.minimap.module.MinimapRenderer";
    private static final String MINIMAP_MODULE_OWNER_CLASS = "xaero.hud.minimap.BuiltInHudModules";
    private static final String MINIMAP_MODULE_FIELD = "MINIMAP";
    private static final String WORLD_MAP_CONTROLS_CLASS = "xaero.map.controls.ControlsRegister";
    private static final String WORLD_MAP_OPEN_KEY_FIELD = "keyOpenMap";

    @Nullable
    private static KeyMapping openMapKey;
    private static boolean openMapKeyResolved;

    private XaeroCompatBridge() {}

    public static boolean isTownBlocked(Minecraft mc) {
        return mc != null && mc.player != null && TownZonePolicyService.isTownDimension(mc.player.level());
    }

    public static boolean isBlockedWorldMapScreen(Screen screen) {
        return screen != null && isBlockedWorldMapScreenClassName(screen.getClass().getName());
    }

    static boolean isBlockedWorldMapScreenClassName(String className) {
        return className != null && className.startsWith(WORLD_MAP_GUI_PREFIX);
    }

    static boolean isMinimapSessionClassName(String className) {
        return className != null
                && className.startsWith(MINIMAP_SESSION_PREFIX)
                && className.endsWith(MINIMAP_SESSION_SUFFIX);
    }

    static boolean isMinimapRendererClassName(String className) {
        return MINIMAP_RENDERER_CLASS.equals(className);
    }

    static boolean isMinimapModuleOwnerClassName(String className) {
        return MINIMAP_MODULE_OWNER_CLASS.equals(className);
    }

    public static boolean isMinimapModule(Object module) {
        if (module == null) {
            return false;
        }
        try {
            Class<?> ownerClass = Class.forName(MINIMAP_MODULE_OWNER_CLASS);
            Field field = ownerClass.getDeclaredField(MINIMAP_MODULE_FIELD);
            field.setAccessible(true);
            return module == field.get(null);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static boolean closeBlockedWorldMapScreen(Minecraft mc) {
        if (mc == null || !isBlockedWorldMapScreen(mc.screen)) {
            return false;
        }
        mc.setScreen(null);
        return true;
    }

    public static void suppressConflictingOpenMapInput() {
        if (!isOpenMapKeyConflicted()) {
            return;
        }
        KeyMapping xaeroOpenMap = xaeroOpenMapKey();
        if (xaeroOpenMap == null) {
            return;
        }
        while (xaeroOpenMap.consumeClick()) {
            // 吃掉城镇里积压的点击，避免离开城镇后补触发 Xaero 世界地图
        }
        xaeroOpenMap.setDown(false);
    }

    /** Xaero 世界地图的"打开地图"键；未安装世界地图时为 null（解析一次后缓存）。 */
    @Nullable
    public static KeyMapping xaeroOpenMapKey() {
        if (!openMapKeyResolved) {
            openMapKeyResolved = true;
            try {
                Field field = Class.forName(WORLD_MAP_CONTROLS_CLASS)
                        .getDeclaredField(WORLD_MAP_OPEN_KEY_FIELD);
                field.setAccessible(true);
                if (field.get(null) instanceof KeyMapping mapping) {
                    openMapKey = mapping;
                }
            } catch (ReflectiveOperationException ignored) {
                // Xaero World Map 未安装：无需处理。
            }
        }
        return openMapKey;
    }

    /**
     * 城镇地图与 Xaero 世界地图是否绑在同一个物理键上（含修饰键）。只比较实际绑定而不看
     * {@code KeyConflictContext}：玩家按的是同一个键，就必须二选一。
     */
    public static boolean isOpenMapKeyConflicted() {
        KeyMapping xaeroOpenMap = xaeroOpenMapKey();
        KeyMapping townMap = ModKeyBindings.TOGGLE_MAP_MODE;
        if (xaeroOpenMap == null || townMap.isUnbound() || xaeroOpenMap.isUnbound()) {
            return false;
        }
        return townMap.getKey().equals(xaeroOpenMap.getKey())
                && townMap.getKeyModifier() == xaeroOpenMap.getKeyModifier();
    }
}
