package com.otherworldinn.world.expedition;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

@SuppressWarnings("unused")
public enum ChartComponentType {

    // ═══════════════════════════════════════════
    // 世界类型（互斥）
    // ═══════════════════════════════════════════
    SURFACE_WORLD("surface_world", ComponentCategory.WORLD_TYPE,
            DimensionCategory.MAIN_WORLD, Rarity.COMMON,
            "地表世界", "Surface World",
            List.of("基础地形以主世界的方式生成"), List.of(),
            () -> Items.GRASS_BLOCK),

    FLOATING_ISLANDS("floating_islands", ComponentCategory.WORLD_TYPE,
            DimensionCategory.MAIN_WORLD, Rarity.RARE,
            "浮岛世界", "Floating Islands",
            List.of("基础地形以浮岛世界的方式生成"), List.of(),
            () -> Items.END_STONE),

    AMPLIFIED_WORLD("amplified_world", ComponentCategory.WORLD_TYPE,
            DimensionCategory.MAIN_WORLD, Rarity.RARE,
            "放大化世界", "Amplified World",
            List.of("基础地形以放大化世界的方式生成"), List.of(),
            () -> Items.MOSSY_COBBLESTONE),

    CAVE_WORLD("cave_world", ComponentCategory.WORLD_TYPE,
            DimensionCategory.MAIN_WORLD, Rarity.EPIC,
            "洞穴世界", "Cave World",
            List.of("基础地形以洞穴世界的方式生成"), List.of(),
            () -> Items.DEEPSLATE),

    NETHER_CAVE("nether_cave", ComponentCategory.WORLD_TYPE,
            DimensionCategory.NETHER, Rarity.COMMON,
            "下界洞穴", "Nether Cave",
            List.of("下界洞穴世界"), List.of(),
            () -> Items.NETHER_BRICKS),

    END_VOID("end_void", ComponentCategory.WORLD_TYPE,
            DimensionCategory.END, Rarity.COMMON,
            "末地虚空", "End Void",
            List.of("末地虚空世界"), List.of(),
            () -> Items.END_STONE_BRICKS),

    // ═══════════════════════════════════════════
    // 生物群系（可共存）
    // ═══════════════════════════════════════════
    PLAINS("plains_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.COMMON,
            "平原生物群系", "Plains Biomes",
            List.of("平原、草甸、向日葵平原"), List.of(),
            () -> Items.SUNFLOWER),

    FORESTS("forests_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.COMMON,
            "森林生物群系", "Forest Biomes",
            List.of("森林、繁花森林、樱花树林、桦木森林"), List.of(),
            () -> Items.OAK_SAPLING),

    TAIGAS("taigas_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.COMMON,
            "针叶林生物群系", "Taiga Biomes",
            List.of("针叶林、原始松木针叶林、原始云杉针叶林"), List.of(),
            () -> Items.SPRUCE_SAPLING),

    SAVANNAS("savannas_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.COMMON,
            "热带草原生物群系", "Savanna Biomes",
            List.of("热带草原、热带高原、风袭热带草原"), List.of(),
            () -> Items.ACACIA_SAPLING),

    DESERT("desert_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.UNCOMMON,
            "沙漠生物群系", "Desert Biomes",
            List.of("沙漠、恶地、风蚀恶地、繁茂恶地"), List.of(),
            () -> Items.SAND),

    SNOWY("snowy_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.UNCOMMON,
            "积雪生物群系", "Snowy Biomes",
            List.of("积雪平原、冰刺平原、冰封山峰、积雪山坡、雪林"), List.of(),
            () -> Items.SNOWBALL),

    JUNGLE("jungle_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.UNCOMMON,
            "丛林生物群系", "Jungle Biomes",
            List.of("丛林、稀疏丛林、竹林"), List.of(),
            () -> Items.JUNGLE_SAPLING),

    SWAMP("swamp_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.UNCOMMON,
            "沼泽生物群系", "Swamp Biomes",
            List.of("沼泽、红树林沼泽"), List.of(),
            () -> Items.LILY_PAD),

    OCEAN("ocean_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.UNCOMMON,
            "海洋生物群系", "Ocean Biomes",
            List.of("暖海、温水海、冷水海、冻洋及其深海变种"), List.of(),
            () -> Items.WATER_BUCKET),

    MOUNTAIN("mountain_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.RARE,
            "山地生物群系", "Mountain Biomes",
            List.of("尖峭山峰、冰封山峰、石峰、草甸"), List.of(),
            () -> Items.SNOW_BLOCK),

    MUSHROOM("mushroom_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.RARE,
            "蘑菇生物群系", "Mushroom Biomes",
            List.of("蘑菇岛"), List.of(),
            () -> Items.RED_MUSHROOM),

    DARK_FOREST("dark_forest_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.RARE,
            "黑森林生物群系", "Dark Forest Biomes",
            List.of("黑森林"), List.of(),
            () -> Items.DARK_OAK_SAPLING),

    SCULK_BIOME("sculk_biome", ComponentCategory.BIOME,
            DimensionCategory.MAIN_WORLD, Rarity.EPIC,
            "幽匿生物群系", "Sculk Biomes",
            List.of("深暗之域大面积覆盖"), List.of(),
            () -> Items.SCULK_CATALYST),

    NETHER_WASTES("nether_wastes_biome", ComponentCategory.BIOME,
            DimensionCategory.NETHER, Rarity.COMMON,
            "下界荒地生物群系", "Nether Wastes Biomes",
            List.of("下界荒原"), List.of(),
            () -> Items.NETHERRACK),

    CRIMSON("crimson_biome", ComponentCategory.BIOME,
            DimensionCategory.NETHER, Rarity.COMMON,
            "绯红森林生物群系", "Crimson Biomes",
            List.of("绯红森林"), List.of(),
            () -> Items.CRIMSON_FUNGUS),

    WARPED("warped_biome", ComponentCategory.BIOME,
            DimensionCategory.NETHER, Rarity.COMMON,
            "诡异森林生物群系", "Warped Biomes",
            List.of("诡异森林"), List.of(),
            () -> Items.WARPED_FUNGUS),

    BASALT("basalt_biome", ComponentCategory.BIOME,
            DimensionCategory.NETHER, Rarity.UNCOMMON,
            "玄武岩生物群系", "Basalt Biomes",
            List.of("玄武岩三角洲"), List.of(),
            () -> Items.BASALT),

    SOUL_VALLEY("soul_valley_biome", ComponentCategory.BIOME,
            DimensionCategory.NETHER, Rarity.UNCOMMON,
            "灵魂沙峡谷生物群系", "Soul Valley Biomes",
            List.of("灵魂沙峡谷"),
            List.of("恶魂与骷髅生成量提升"),
            () -> Items.SOUL_SAND),

    END_HIGHLANDS("end_highlands_biome", ComponentCategory.BIOME,
            DimensionCategory.END, Rarity.COMMON,
            "末地高地生物群系", "End Highlands Biomes",
            List.of("末地高地、末地内陆、末地荒地"), List.of(),
            () -> Items.CHORUS_FLOWER),

    END_ISLANDS("end_islands_biome", ComponentCategory.BIOME,
            DimensionCategory.END, Rarity.RARE,
            "末地群岛生物群系", "End Islands Biomes",
            List.of("末地小型岛屿"), List.of(),
            () -> Items.CHORUS_FRUIT),

    // ═══════════════════════════════════════════
    // 基础岩石类型（互斥）
    // ═══════════════════════════════════════════
    STONE_BASE("stone_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.COMMON,
            "石头", "Stone",
            List.of("地下由石头构成"), List.of(),
            () -> Items.STONE),

    DEEPSLATE_BASE("deepslate_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "深板岩", "Deepslate",
            List.of("地下由深板岩构成"), List.of(),
            () -> Items.POLISHED_DEEPSLATE),

    GRANITE_BASE("granite_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "花岗岩", "Granite",
            List.of("地下由花岗岩构成"), List.of(),
            () -> Items.GRANITE),

    ANDESITE_BASE("andesite_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "安山岩", "Andesite",
            List.of("地下由安山岩构成"), List.of(),
            () -> Items.ANDESITE),

    DIORITE_BASE("diorite_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "闪长岩", "Diorite",
            List.of("地下由闪长岩构成"), List.of(),
            () -> Items.DIORITE),

    SANDSTONE_BASE("sandstone_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "砂岩", "Sandstone",
            List.of("地下由砂岩构成"), List.of(),
            () -> Items.SANDSTONE),

    TUFF_BASE("tuff_base", ComponentCategory.STONE_TYPE,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "凝灰岩", "Tuff",
            List.of("地下由凝灰岩构成"), List.of(),
            () -> Items.TUFF),

    // ═══════════════════════════════════════════
    // 其他
    // ═══════════════════════════════════════════
    THUNDERSTORM("thunderstorm", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "永恒雷暴", "Eternal Lightning",
            List.of("持续生命恢复与抗性提升"),
            List.of("永远雷雨，落雷频率大幅提升"),
            () -> Items.TRIDENT),

    ETERNAL_DAY("eternal_day", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "永恒白昼", "Eternal Day",
            List.of("永远是白天"), List.of(),
            () -> Items.GLOWSTONE_DUST),

    ETERNAL_NIGHT("eternal_night", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "永恒之夜", "Eternal Night",
            List.of("永远是黑夜", "夜视效果常驻"), List.of(),
            () -> Items.CLOCK),

    THRIVING_REALM("thriving_realm", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "繁华之境", "Thriving Realm",
            List.of("结构生成频率大幅提高"), List.of(),
            () -> Items.EMERALD),

    LAVA_FLOOD("lava_flood", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "熔岩灌注", "Lava Flood",
            List.of("抗火效果常驻"),
            List.of("海平面以下的水体全部变为熔岩"),
            () -> Items.LAVA_BUCKET),

    GRAVITY_LOW("gravity_low", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "低重力", "Low Gravity",
            List.of("跳跃高度翻倍，无摔落伤害"), List.of(),
            () -> Items.FEATHER),

    DRY_LAND("dry_land", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "旱地", "Dry Land",
            List.of("急迫效果常驻"),
            List.of("海洋与湖泊不再生成，含水层大幅减少"),
            () -> Items.DEAD_BUSH),

    WATER_WORLD("water_world", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "水世界", "Water World",
            List.of("水下呼吸与海豚的恩惠常驻"),
            List.of("海平面上升至Y=127处"),
            () -> Items.TROPICAL_FISH),

    ONE_HP("one_hp", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.EPIC,
            "一点生命值", "One HP",
            List.of("抗性提升III、生命恢复II常驻"),
            List.of("最大生命值为1点"),
            () -> Items.GOLDEN_APPLE),

    UNIVERSAL_ANGER("universal_anger", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "无差别愤怒", "Universal Anger",
            List.of("力量效果常驻"),
            List.of("所有中立生物始终与玩家敌对"),
            () -> Items.IRON_SWORD),

    ETERNAL_RAIN("eternal_rain", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.UNCOMMON,
            "永恒之雨", "Eternal Rain",
            List.of("速度与饱和效果常驻"),
            List.of("天气恒为降雨"),
            () -> Items.CAULDRON),

    INSOMNIACS("insomniacs", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.RARE,
            "失眠", "Insomniacs",
            List.of("缓降与夜视效果常驻"),
            List.of("无法入睡，幻翼生成"),
            () -> Items.PHANTOM_MEMBRANE),

    NO_DROPS("no_drops", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.EPIC,
            "没有掉落物", "No Drops",
            List.of("击杀生物经验翻倍"),
            List.of("生物死亡不掉落任何物品"),
            () -> Items.BARRIER),

    FISH_OUT_OF_WATER("fish_out_of_water", ComponentCategory.OTHER,
            DimensionCategory.UNIVERSAL, Rarity.EPIC,
            "如鱼失水", "Fish Out of Water",
            List.of("海豚的恩惠常驻"),
            List.of("只能在水中呼吸"),
            () -> Items.COD);



    public enum ComponentCategory {
        WORLD_TYPE,
        BIOME,
        STONE_TYPE,
        OTHER
    }

    public enum DimensionCategory {
        MAIN_WORLD,
        NETHER,
        END,
        UNIVERSAL
    }

    private final String id;
    private final ComponentCategory componentCategory;
    private final DimensionCategory dimensionCategory;
    private final Rarity rarity;
    private final String zhName;
    private final String enName;
    private final List<String> effectLines;
    private final List<String> downsideLines;
    private final java.util.function.Supplier<Item> iconSupplier;

    ChartComponentType(String id, ComponentCategory componentCategory,
            DimensionCategory dimensionCategory, Rarity rarity,
            String zhName, String enName,
            List<String> effectLines, List<String> downsideLines,
            java.util.function.Supplier<Item> iconSupplier) {
        this.id = id;
        this.componentCategory = componentCategory;
        this.dimensionCategory = dimensionCategory;
        this.rarity = rarity;
        this.zhName = zhName;
        this.enName = enName;
        this.effectLines = List.copyOf(effectLines);
        this.downsideLines = List.copyOf(downsideLines);
        this.iconSupplier = iconSupplier;
    }

    public String id() { return id; }
    public ComponentCategory componentCategory() { return componentCategory; }
    public DimensionCategory dimensionCategory() { return dimensionCategory; }
    public Rarity rarity() { return rarity; }
    public String zhName() { return zhName; }
    public String enName() { return enName; }
    public List<String> effectLines() { return effectLines; }
    public List<String> downsideLines() { return downsideLines; }
    public Item iconItem() { return iconSupplier.get(); }

    public String displayName(boolean zh) { return zh ? zhName : enName; }
    public boolean isUniversal() { return dimensionCategory == DimensionCategory.UNIVERSAL; }
    public boolean isWorldType() { return componentCategory == ComponentCategory.WORLD_TYPE; }

    public int fee() {
        return switch (rarity) {
            case COMMON -> 5;
            case UNCOMMON -> 15;
            case RARE -> 40;
            case EPIC -> 100;
        };
    }

    public ChatFormatting categoryColor() {
        return switch (componentCategory) {
            case WORLD_TYPE -> ChatFormatting.GOLD;
            case BIOME -> ChatFormatting.AQUA;
            case STONE_TYPE -> ChatFormatting.GRAY;
            case OTHER -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    public ChatFormatting rarityColor() {
        return switch (rarity) {
            case COMMON -> ChatFormatting.WHITE;
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.BLUE;
            case EPIC -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    public String categoryDisplayName(boolean zh) {
        return switch (componentCategory) {
            case WORLD_TYPE -> zh ? "世界类型" : "World Type";
            case BIOME -> zh ? "生物群系" : "Biome";
            case STONE_TYPE -> zh ? "岩石类型" : "Stone Type";
            case OTHER -> zh ? "其他" : "Other";
        };
    }

    public MutableComponent displayComponent(boolean zh) {
        return Component.literal("[" + displayName(zh) + "]").withStyle(rarityColor());
    }

    public MutableComponent displayWithCategory(boolean zh) {
        ChatFormatting catColor = categoryColor();
        String catName = categoryDisplayName(zh);
        return Component.literal("[" + catName + "|" + displayName(zh) + "]").withStyle(catColor);
    }

    public static ChartComponentType byId(String id) {
        for (ChartComponentType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    public static boolean componentMatchesChartDimension(String componentId,
            DimensionCategory chartDimension) {
        ChartComponentType t = byId(componentId);
        if (t == null) return false;
        if (chartDimension == null || chartDimension == DimensionCategory.UNIVERSAL)
            return true;
        if (t.isUniversal()) return true;
        return t.dimensionCategory == chartDimension;
    }

    public static DimensionCategory determineDimension(List<String> componentIds) {
        boolean hasNether = false;
        boolean hasEnd = false;
        for (String id : componentIds) {
            ChartComponentType t = byId(id);
            if (t == null || t.isUniversal()) continue;
            if (t.dimensionCategory == DimensionCategory.NETHER) hasNether = true;
            if (t.dimensionCategory == DimensionCategory.END) hasEnd = true;
        }
        if (hasNether && hasEnd) return null;
        if (hasNether) return DimensionCategory.NETHER;
        if (hasEnd) return DimensionCategory.END;
        return DimensionCategory.MAIN_WORLD;
    }

    public static DimensionCategory fromNbtName(String name) {
        for (DimensionCategory c : DimensionCategory.values()) {
            if (c.name().equalsIgnoreCase(name)) return c;
        }
        return DimensionCategory.MAIN_WORLD;
    }

    public static ChartComponentType resolveWorldType(List<String> componentIds) {
        for (String id : componentIds) {
            ChartComponentType t = byId(id);
            if (t != null && t.isWorldType()) return t;
        }
        return SURFACE_WORLD;
    }

    public static boolean conflictsWithExisting(List<String> existingIds,
            ChartComponentType newComponent) {
        ComponentCategory cat = newComponent.componentCategory;
        if (cat == ComponentCategory.BIOME || cat == ComponentCategory.OTHER) return false;
        for (String id : existingIds) {
            ChartComponentType t = byId(id);
            if (t != null && t.componentCategory == cat) return true;
        }
        return false;
    }
}
