package com.otherworldinn.world.map;

import com.otherworldinn.OtherworldInn;
import net.minecraft.resources.ResourceLocation;

public final class MapIconAtlas {
    public static final int ICON_SIZE = 16;
    public static final int STATE_COUNT = 4;
    public static final int SLOT_COUNT = 16; // 预留拓展槽位
    public static final int ATLAS_WIDTH = ICON_SIZE * SLOT_COUNT;
    public static final int ATLAS_HEIGHT = ICON_SIZE * STATE_COUNT;
    public static final ResourceLocation ATLAS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/gui/map/point_icons_atlas.png");

    public static final int SLOT_INN = 0;
    public static final int SLOT_BLACKSMITH = 1;
    public static final int SLOT_TOWN_GATE = 2;
    public static final int SLOT_BOILER_ROOM = 3;
    public static final int SLOT_GREENHOUSE = 4;
    public static final int SLOT_MAGICIAN_WORKSHOP = 5;
    public static final int SLOT_MARKET = 6;
    public static final int SLOT_DOCK = 7;

    public static final int STATE_NORMAL = 0;
    public static final int STATE_NORMAL_HOVER_OR_PRESSED = 1;
    public static final int STATE_DISABLED = 2;
    public static final int STATE_DISABLED_HOVER_OR_PRESSED = 3;

    private MapIconAtlas() {}

    public static int slotForPointId(ResourceLocation pointId) {
        if (pointId == null) {
            return SLOT_INN;
        }
        String path = pointId.getPath();
        return switch (path) {
            case "inn" -> SLOT_INN;
            case "blacksmith" -> SLOT_BLACKSMITH;
            case "town_gate" -> SLOT_TOWN_GATE;
            case "boiler_room" -> SLOT_BOILER_ROOM;
            case "greenhouse" -> SLOT_GREENHOUSE;
            case "magician_workshop" -> SLOT_MAGICIAN_WORKSHOP;
            case "market" -> SLOT_MARKET;
            case "dock" -> SLOT_DOCK;
            default -> SLOT_INN;
        };
    }
}
