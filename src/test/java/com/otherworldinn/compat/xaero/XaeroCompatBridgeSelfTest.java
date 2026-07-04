package com.otherworldinn.compat.xaero;

public final class XaeroCompatBridgeSelfTest {

    private XaeroCompatBridgeSelfTest() {}

    public static void main(String[] args) {
        expectTrue(XaeroCompatBridge.isBlockedWorldMapScreenClassName("xaero.map.gui.GuiMap"));
        expectTrue(XaeroCompatBridge.isBlockedWorldMapScreenClassName("xaero.map.gui.GuiMapSwitching"));
        expectTrue(XaeroCompatBridge.isBlockedWorldMapScreenClassName("xaero.map.gui.GuiWorldMapSettings"));

        expectFalse(XaeroCompatBridge.isBlockedWorldMapScreenClassName(null));
        expectFalse(XaeroCompatBridge.isBlockedWorldMapScreenClassName(""));
        expectFalse(XaeroCompatBridge.isBlockedWorldMapScreenClassName("xaero.common.gui.GuiMinimapSettings"));
        expectFalse(XaeroCompatBridge.isBlockedWorldMapScreenClassName("com.example.SomeOtherScreen"));

        expectTrue(XaeroCompatBridge.isMinimapSessionClassName("xaero.hud.minimap.module.MinimapSession"));
        expectTrue(XaeroCompatBridge.isMinimapSessionClassName("xaero.hud.minimap.module.CustomMinimapSession"));
        expectFalse(XaeroCompatBridge.isMinimapSessionClassName("xaero.map.WorldMapSession"));
        expectFalse(XaeroCompatBridge.isMinimapSessionClassName(null));

        expectTrue(XaeroCompatBridge.isMinimapRendererClassName("xaero.hud.minimap.module.MinimapRenderer"));
        expectFalse(XaeroCompatBridge.isMinimapRendererClassName("xaero.hud.render.HudRenderer"));
        expectFalse(XaeroCompatBridge.isMinimapRendererClassName(null));

        expectTrue(XaeroCompatBridge.isMinimapModuleOwnerClassName("xaero.hud.minimap.BuiltInHudModules"));
        expectFalse(XaeroCompatBridge.isMinimapModuleOwnerClassName("xaero.hud.render.HudRenderer"));
        expectFalse(XaeroCompatBridge.isMinimapModuleOwnerClassName(null));
    }

    private static void expectTrue(boolean value) {
        if (!value) {
            throw new AssertionError("Expected condition to be true");
        }
    }

    private static void expectFalse(boolean value) {
        if (value) {
            throw new AssertionError("Expected condition to be false");
        }
    }
}
