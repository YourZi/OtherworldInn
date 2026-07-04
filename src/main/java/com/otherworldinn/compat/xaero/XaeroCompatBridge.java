package com.otherworldinn.compat.xaero;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import java.lang.reflect.Field;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class XaeroCompatBridge {
    private static final String WORLD_MAP_GUI_PREFIX = "xaero.map.gui.";
    private static final String MINIMAP_SESSION_PREFIX = "xaero.hud.minimap.module.";
    private static final String MINIMAP_SESSION_SUFFIX = "MinimapSession";
    private static final String MINIMAP_RENDERER_CLASS = "xaero.hud.minimap.module.MinimapRenderer";
    private static final String MINIMAP_MODULE_OWNER_CLASS = "xaero.hud.minimap.BuiltInHudModules";
    private static final String MINIMAP_MODULE_FIELD = "MINIMAP";
    private static final String WORLD_MAP_CONTROLS_CLASS = "xaero.map.controls.ControlsRegister";

    private static boolean worldMapControlsFailureLogged;

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

    public static void suppressWorldMapInputs() {
        try {
            Class<?> controlsClass = Class.forName(WORLD_MAP_CONTROLS_CLASS);
            for (Field field : controlsClass.getDeclaredFields()) {
                if (!KeyMapping.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(null);
                if (value instanceof KeyMapping keyMapping) {
                    clearKeyMapping(keyMapping);
                }
            }
        } catch (ClassNotFoundException ignored) {
            // Xaero World Map 未安装时无需处理。
        } catch (IllegalAccessException exception) {
            if (!worldMapControlsFailureLogged) {
                worldMapControlsFailureLogged = true;
                OtherworldInn.LOGGER.warn("Failed to suppress Xaero world map key inputs.", exception);
            }
        }
    }

    private static void clearKeyMapping(KeyMapping keyMapping) {
        while (keyMapping.consumeClick()) {
            // 清空累计点击，避免离开城镇后补触发。
        }
        keyMapping.setDown(false);
    }
}
