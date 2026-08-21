package com.otherworldinn.world.inn.facility;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.map.MapPoint;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class FacilityRegistry {
    private static final Map<String, FacilityDefinition> FACILITIES = new LinkedHashMap<>();
    private static final List<ResourceLocation> FACILITY_TOOL_ITEM_IDS =
            List.of(
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "facility_upgrade_template"));

    static {
        registerDefaults();
    }

    private FacilityRegistry() {}

    public static FacilityDefinition registerFacility(
            String id,
            String enName,
            String zhName,
            int maxLevel,
            BlockPos centerPos,
            FacilityRange facilityRange,
            List<LevelUpgradeCost> levelUpgradeCosts,
            FacilityMapPointConfig mapPointConfig,
            Map<Integer, List<FacilityRange>> extraBuildAllowRangesByLevel) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Facility id cannot be blank.");
        }
        if (maxLevel < 1) {
            throw new IllegalArgumentException("Facility maxLevel must be >= 1.");
        }
        if (levelUpgradeCosts == null || levelUpgradeCosts.size() != maxLevel) {
            throw new IllegalArgumentException(
                    "levelUpgradeCosts size must be equal to maxLevel. expected="
                            + maxLevel
                            + ", actual="
                            + (levelUpgradeCosts == null ? 0 : levelUpgradeCosts.size()));
        }
        if (centerPos == null) {
            throw new IllegalArgumentException("Facility centerPos cannot be null.");
        }
        if (facilityRange == null) {
            throw new IllegalArgumentException("Facility facilityRange cannot be null.");
        }

        Map<Integer, FacilityLevelDefinition> levels = new LinkedHashMap<>();
        levels.put(
                0,
                new FacilityLevelDefinition(
                        0,
                        List.of(),
                        0,
                        buildStructureId(id, 0)));

        for (int level = 1; level <= maxLevel; level++) {
            LevelUpgradeCost cost = levelUpgradeCosts.get(level - 1);
            List<ItemStack> copiedItems = copyItemStacks(cost.requiredItems());
            levels.put(
                    level,
                    new FacilityLevelDefinition(
                            level,
                            copiedItems,
                            Math.max(0, cost.requiredCoins()),
                            buildStructureId(id, level)));
        }

        FacilityDefinition definition =
                new FacilityDefinition(
                        id,
                        enName == null || enName.isBlank() ? id : enName,
                        zhName == null || zhName.isBlank() ? enName : zhName,
                        maxLevel,
                        centerPos.immutable(),
                        facilityRange.normalize(),
                        "facility." + OtherworldInn.MODID + "." + id,
                        normalizeMapPointConfig(id, centerPos, mapPointConfig),
                        Map.copyOf(levels),
                        normalizeExtraBuildAllowRanges(maxLevel, extraBuildAllowRangesByLevel));
        FACILITIES.put(id, definition);
        return definition;
    }

    public static FacilityDefinition get(String id) {
        return FACILITIES.get(id);
    }

    public static Collection<FacilityDefinition> getAll() {
        return FACILITIES.values();
    }

    public static Optional<FacilityDefinition> findFacilityInRange(BlockPos pos) {
        if (pos == null) {
            return Optional.empty();
        }
        for (FacilityDefinition facility : FACILITIES.values()) {
            if (facility.facilityRange().contains(pos)) {
                return Optional.of(facility);
            }
        }
        return Optional.empty();
    }

    public static boolean isHoldingFacilityTool(Player player) {
        if (player == null) {
            return false;
        }
        return isToolStack(player.getItemInHand(InteractionHand.MAIN_HAND))
                || isToolStack(player.getItemInHand(InteractionHand.OFF_HAND));
    }

    public static boolean isToolStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        for (ResourceLocation itemId : FACILITY_TOOL_ITEM_IDS) {
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != null && item != net.minecraft.world.item.Items.AIR && stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public static ResourceLocation buildStructureId(String facilityId, int level) {
        return ResourceLocation.fromNamespaceAndPath(
                OtherworldInn.MODID, "facility/" + facilityId + "/level_" + level);
    }

    public static ResourceLocation buildMapPointId(String facilityId) {
        return ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, facilityId);
    }

    private static List<ItemStack> copyItemStacks(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return List.of();
        }
        List<ItemStack> copied = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (stack != null && !stack.isEmpty()) {
                copied.add(stack.copy());
            }
        }
        return List.copyOf(copied);
    }

    private static Map<Integer, List<FacilityRange>> normalizeExtraBuildAllowRanges(
            int maxLevel, Map<Integer, List<FacilityRange>> rangesByLevel) {
        if (rangesByLevel == null || rangesByLevel.isEmpty()) {
            return Map.of();
        }
        Map<Integer, List<FacilityRange>> normalized = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<FacilityRange>> entry : rangesByLevel.entrySet()) {
            Integer level = entry.getKey();
            if (level == null || level <= 0 || level > maxLevel) {
                continue;
            }
            List<FacilityRange> ranges = entry.getValue();
            if (ranges == null || ranges.isEmpty()) {
                normalized.put(level, List.of());
                continue;
            }
            List<FacilityRange> copied = new ArrayList<>(ranges.size());
            for (FacilityRange range : ranges) {
                if (range != null) {
                    copied.add(range.normalize());
                }
            }
            normalized.put(level, List.copyOf(copied));
        }
        return Map.copyOf(normalized);
    }

    private static void registerDefaults() {
        registerFacility(
                "boiler_room",
                "Boiler Room",
                "锅炉房",
                3,
                new BlockPos(31, 62, -35),
                new FacilityRange(new BlockPos(28, 61, -37), new BlockPos(36, 67, -31)),
                List.of(
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.COPPER_INGOT, 12),
                                        new ItemStack(Items.FURNACE, 4)),
                                400),
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.COPPER_INGOT, 32),
                                        new ItemStack(AllBlocks.BLAZE_BURNER.asItem(), 4)),
                                900),
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(AllBlocks.COPPER_CASING.asItem(), 16),
                                        new ItemStack(AllBlocks.STEAM_ENGINE.asItem(), 4),
                                        new ItemStack(AllBlocks.BLAZE_BURNER.asItem(), 9)),
                                1300)),
                new FacilityMapPointConfig(
                        new Vec3(30, 62, -39),
                        new Vec2(70, 50),
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "textures/gui/map/icon_boiler_room.png"),
                        MapPoint.MapPointType.SHOP,
                        0,
                        -1),
                Map.of());

        registerFacility(
                "greenhouse",
                "Greenhouse",
                "温室",
                3,
                new BlockPos(-29, 70, 49),
                new FacilityRange(new BlockPos(0, 85, 65), new BlockPos(-30, 70, 49)),
                List.of(
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.GLASS, 24),
                                        new ItemStack(Items.OAK_PLANKS, 16)),
                                300),
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.GLASS_PANE, 32),
                                        new ItemStack(Items.IRON_INGOT, 12)),
                                700),
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.GLASS, 32),
                                        new ItemStack(Items.IRON_INGOT, 48),
                                        new ItemStack(Items.LANTERN, 8)),
                                1200)),
                new FacilityMapPointConfig(
                        new Vec3(2, 71, 52),
                        new Vec2(20, -40),
                        ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "textures/gui/map/icon_greenhouse.png"),
                        MapPoint.MapPointType.SHOP,
                        0,
                        1),
                Map.of(
                        1,
                        List.of(new FacilityRange(new BlockPos(-2, 71, 63), new BlockPos(-11, 75, 51))),
                        2,
                        List.of(new FacilityRange(new BlockPos(-2, 71, 63), new BlockPos(-17, 75, 51))),
                        3,
                        List.of(new FacilityRange(new BlockPos(-2, 71, 63), new BlockPos(-23, 75, 51)))));

        // TODO 矿井：centerPos / facilityRange 为临时占位坐标，正式建筑与坐标待设计确定
        // TODO 结构 NBT：data/otherworldinn/structure/facility/mine/level_0..3.nbt 待补充
        // TODO 地图图标：已绘入 point_icons_atlas.png 第 8 格（MapIconAtlas.SLOT_MINE）
        registerFacility(
                "mine",
                "Mine",
                "矿井",
                3,
                new BlockPos(9, 71, 79),
                new FacilityRange(new BlockPos(-17, 70, 66), new BlockPos(7, 81, 91)),
                List.of(
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.TORCH, 16),
                                        new ItemStack(Items.IRON_PICKAXE, 1)),
                                300),
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.GOLD_INGOT, 8),
                                        new ItemStack(AllBlocks.ANDESITE_CASING.asItem(), 4)),
                                700),
                        new LevelUpgradeCost(
                                List.of(
                                        new ItemStack(Items.DIAMOND_PICKAXE, 1),
                                        new ItemStack(AllBlocks.MECHANICAL_SAW.asItem(), 2),
                                        new ItemStack(Items.REDSTONE, 16)),
                                1200)),
                // 地图点与温室同页 (0, 1)；传送落点 (3, 70, 75)
                new FacilityMapPointConfig(
                        new Vec3(3, 70, 75), null, null, MapPoint.MapPointType.SHOP, 0, 1),
                Map.of());
    }

    private static FacilityMapPointConfig normalizeMapPointConfig(
            String facilityId, BlockPos centerPos, FacilityMapPointConfig config) {
        if (config == null) {
            return new FacilityMapPointConfig(
                    Vec3.atCenterOf(centerPos),
                    Vec2.ZERO,
                    ResourceLocation.fromNamespaceAndPath(
                            OtherworldInn.MODID, "textures/gui/map/icon_blacksmith.png"),
                    MapPoint.MapPointType.SHOP,
                    0,
                    0);
        }
        Vec3 worldPosition = config.worldPosition() == null ? Vec3.atCenterOf(centerPos) : config.worldPosition();
        Vec2 screenOffset = config.screenOffset() == null ? Vec2.ZERO : config.screenOffset();
        ResourceLocation iconTexture =
                config.iconTexture() == null
                        ? ResourceLocation.fromNamespaceAndPath(
                                OtherworldInn.MODID, "textures/gui/map/icon_blacksmith.png")
                        : config.iconTexture();
        MapPoint.MapPointType type =
                config.type() == null ? MapPoint.MapPointType.SHOP : config.type();
        return new FacilityMapPointConfig(
                worldPosition, screenOffset, iconTexture, type, config.pageX(), config.pageZ());
    }

    public record FacilityDefinition(
            String id,
            String enName,
            String zhName,
            int maxLevel,
            BlockPos centerPos,
            FacilityRange facilityRange,
            String translationKey,
            FacilityMapPointConfig mapPointConfig,
            Map<Integer, FacilityLevelDefinition> levels,
            Map<Integer, List<FacilityRange>> extraBuildAllowRangesByLevel) {
        public FacilityLevelDefinition getLevel(int level) {
            int clamped = Math.max(0, Math.min(level, maxLevel));
            return levels.get(clamped);
        }

        public List<FacilityRange> getExtraBuildAllowRanges(int level) {
            int clamped = Math.max(0, Math.min(level, maxLevel));
            if (clamped <= 0) {
                return List.of();
            }
            return extraBuildAllowRangesByLevel.getOrDefault(clamped, List.of());
        }

        public ResourceLocation mapPointId() {
            return buildMapPointId(id);
        }
    }

    public record FacilityLevelDefinition(
            int level,
            List<ItemStack> requiredItems,
            int requiredCoins,
            ResourceLocation structureId) {}

    public record FacilityMapPointConfig(
            Vec3 worldPosition,
            Vec2 screenOffset,
            ResourceLocation iconTexture,
            MapPoint.MapPointType type,
            int pageX,
            int pageZ) {}

    public record FacilityRange(BlockPos from, BlockPos to) {
        public FacilityRange {
            if (from == null || to == null) {
                throw new IllegalArgumentException("Facility range points cannot be null.");
            }
            from = from.immutable();
            to = to.immutable();
        }

        public FacilityRange normalize() {
            return new FacilityRange(
                    new BlockPos(Math.min(from.getX(), to.getX()), from.getY(), Math.min(from.getZ(), to.getZ())),
                    new BlockPos(Math.max(from.getX(), to.getX()), to.getY(), Math.max(from.getZ(), to.getZ())));
        }

        public boolean contains(BlockPos pos) {
            if (pos == null) {
                return false;
            }
            FacilityRange normalized = this.normalize();
            return pos.getX() >= normalized.from().getX()
                    && pos.getX() <= normalized.to().getX()
                    && pos.getZ() >= normalized.from().getZ()
                    && pos.getZ() <= normalized.to().getZ();
        }
    }

    public record LevelUpgradeCost(List<ItemStack> requiredItems, int requiredCoins) {}
}
