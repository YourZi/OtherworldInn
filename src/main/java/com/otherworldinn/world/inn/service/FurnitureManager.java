package com.otherworldinn.world.inn.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.util.tooltip.FurnitureSetTooltipHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** 管理家具属性配置（舒适度/光照/湿度），支持方块、标签与关键词匹配。 */
public class FurnitureManager {

    private static final Map<Block, FurnitureStats> BLOCK_STATS = new HashMap<>();
    private static final Map<TagKey<Block>, FurnitureStats> TAG_STATS = new HashMap<>();
    private static final Map<Block, Integer> BLOCK_LIGHT_LEVEL_CACHE = new HashMap<>();
    private static final List<KeywordRule> KEYWORD_RULES = List.of(
            new KeywordRule("fish_tank", new FurnitureStats(2, 0, 12)),
            new KeywordRule("potted", new FurnitureStats(2, 0, 8)),
            new KeywordRule("bookshelf", new FurnitureStats(5, 0, 0)),
            new KeywordRule("desk", new FurnitureStats(4, 0, 0)),
            new KeywordRule("table", new FurnitureStats(4, 0, 0)),
            new KeywordRule("drawer", new FurnitureStats(3, 0, 0)),
            new KeywordRule("sofa", new FurnitureStats(5, 0, 0)),
            new KeywordRule("carpet", new FurnitureStats(3, 0, 0)),
            new KeywordRule("chair", new FurnitureStats(4, 0, 0)),
            new KeywordRule("wardrobe", new FurnitureStats(5, 0, 0)),
            new KeywordRule("sink", new FurnitureStats(2, 0, 7)),
            new KeywordRule("couch", new FurnitureStats(3, 0, 0)),
            new KeywordRule("chaise", new FurnitureStats(3, 0, 0)),
            new KeywordRule("toilet", new FurnitureStats(3, 0, 8)),
            new KeywordRule("clutter", new FurnitureStats(-20, 0, 0)),
            new KeywordRule("photograph_frame", new FurnitureStats(6, 0, 0))
        );

    static {
        initDefaultFurniture();
    }

    /** 初始化默认家具配置 */
    private static void initDefaultFurniture() {

        FurnitureStats bedStats = new FurnitureStats(15, 0, 0);
        TagKey<Block> bedsTag =
                TagKey.create(
                        BuiltInRegistries.BLOCK.key(),
                        ResourceLocation.withDefaultNamespace("beds"));
        registerTag(bedsTag, bedStats);

        FurnitureStats cushionStats = new FurnitureStats(10, 0, 0);
        TagKey<Block> createSeatsTag =
                TagKey.create(
                        BuiltInRegistries.BLOCK.key(),
                        ResourceLocation.fromNamespaceAndPath("create", "seats"));
        registerTag(createSeatsTag, cushionStats);

        FurnitureStats storageStats = new FurnitureStats(7, 0, 0);
        registerBlock(Blocks.CHEST, storageStats);
        registerBlock(Blocks.BARREL, storageStats);

        FurnitureStats flowerPotStats = new FurnitureStats(5, 0, 8);
        TagKey<Block> flowerPotsTag =
                TagKey.create(
                        BuiltInRegistries.BLOCK.key(),
                        ResourceLocation.withDefaultNamespace("flower_pots"));
        TagKey<Block> commonFlowerPotsTag =
                TagKey.create(
                        BuiltInRegistries.BLOCK.key(),
                        ResourceLocation.fromNamespaceAndPath("c", "flower_pots"));
        registerTag(flowerPotsTag, flowerPotStats);
        registerTag(commonFlowerPotsTag, flowerPotStats);

        registerBlock(Blocks.FURNACE, new FurnitureStats(5, 0, -5));
        registerBlock(Blocks.BLAST_FURNACE, new FurnitureStats(3, 0, -7));
        registerBlock(Blocks.SMOKER, new FurnitureStats(-3, 0, -6));
        registerBlock(Blocks.CAMPFIRE, new FurnitureStats(5, 0, -9));
        registerBlock(Blocks.SOUL_CAMPFIRE, new FurnitureStats(-5, 0, -5));

        FurnitureStats uncomfortableStats = new FurnitureStats(-12, -10, 0);
        registerBlock(Blocks.COBWEB, uncomfortableStats);
        registerBlock(Blocks.SCULK, uncomfortableStats);
        registerBlock(Blocks.SCULK_VEIN, uncomfortableStats);
        registerBlock(Blocks.SCULK_CATALYST, uncomfortableStats);
        registerBlock(Blocks.SCULK_SENSOR, uncomfortableStats);
        registerBlock(Blocks.CALIBRATED_SCULK_SENSOR, uncomfortableStats);
        registerBlock(Blocks.SCULK_SHRIEKER, uncomfortableStats);
    }

    public static void registerBlock(Block block, FurnitureStats stats) {
        BLOCK_STATS.put(block, stats);
    }

    public static void registerTag(TagKey<Block> tag, FurnitureStats stats) {
        TAG_STATS.put(tag, stats);
    }

    /** 获取家具属性，优先方块配置其次标签；无配置但有发光时按发光等级生成光照属性。 */
    public static Optional<FurnitureStats> getStats(Block block) {
        Optional<FurnitureStats> baseStats = getBaseStats(block);
        int lightLevel = getMaxLightLevel(block);
        if (lightLevel > 0) {
            if (baseStats.isPresent()) {
                FurnitureStats stats = baseStats.get();
                return Optional.of(new FurnitureStats(stats.comfort(), lightLevel, stats.humidity()));
            }
            return Optional.of(new FurnitureStats(0, lightLevel, 0));
        }
        return baseStats;
    }

    private static Optional<FurnitureStats> getBaseStats(Block block) {
        if (BLOCK_STATS.containsKey(block)) {
            return Optional.of(BLOCK_STATS.get(block));
        }

        // 需遍历所有已注册标签，频繁查询可能有性能影响
        var state = block.defaultBlockState();
        for (var entry : TAG_STATS.entrySet()) {
            if (state.is(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId != null) {
            String key = blockId.toString();
            for (KeywordRule rule : KEYWORD_RULES) {
                if (key.contains(rule.keyword())) {
                    return Optional.of(rule.stats());
                }
            }
        }

        return Optional.empty();
    }

    private static int getMaxLightLevel(Block block) {
        if (BLOCK_LIGHT_LEVEL_CACHE.containsKey(block)) {
            return BLOCK_LIGHT_LEVEL_CACHE.get(block);
        }
        int max =
                block.getStateDefinition().getPossibleStates().stream()
                        .mapToInt(state -> state.getLightEmission())
                        .max()
                        .orElse(0);
        BLOCK_LIGHT_LEVEL_CACHE.put(block, max);
        return max;
    }

    /** 家具属性（舒适度/光照/湿度，各 -20 ~ 20）。 */
    public record FurnitureStats(int comfort, int light, int humidity) {}

    /** 一个家具方块及其属性（展示用）。 */
    public record BlockFurniture(Block block, FurnitureStats stats) {}

    /** 遍历方块注册表收集家具方块（JEI 展示用）；一次性调用，勿在 tick 内频繁使用。 */
    public static List<BlockFurniture> getAllFurniture() {
        List<BlockFurniture> result = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Optional<FurnitureStats> base = getBaseStats(block);
            int light = 0;
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                light = Math.max(light, state.getLightEmission());
            }
            if (base.isEmpty() && light <= 0) {
                continue;
            }
            FurnitureStats stats = base.orElse(new FurnitureStats(0, 0, 0));
            if (light > 0) {
                stats = new FurnitureStats(stats.comfort(), light, stats.humidity());
            }
            if (stats.comfort() == 0 && stats.light() == 0 && stats.humidity() == 0) {
                continue;
            }
            result.add(new BlockFurniture(block, stats));
        }
        return result;
    }

    private record KeywordRule(String keyword, FurnitureStats stats) {}

    /** 客户端事件处理器 */
    @EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
    public static class ClientHandler {
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                getStats(block)
                        .ifPresent(
                                stats -> {
                                    if (stats.comfort != 0) {
                                        event.getToolTip()
                                                .add(
                                                        Component.translatable(
                                                                        "tooltip.otherworldinn.furniture.comfort",
                                                                        String.format(
                                                                                "%+d",
                                                                                stats.comfort))
                                                                .withStyle(
                                                                        style ->
                                                                                style.withColor(
                                                                                        ModColors
                                                                                                .COMFORT)));
                                    }
                                    if (stats.light != 0) {
                                        event.getToolTip()
                                                .add(
                                                        Component.translatable(
                                                                        "tooltip.otherworldinn.furniture.light",
                                                                        String.format(
                                                                                "%+d", stats.light))
                                                                .withStyle(
                                                                        style ->
                                                                                style.withColor(
                                                                                        ModColors
                                                                                                .LIGHT)));
                                    }
                                    if (stats.humidity != 0) {
                                        event.getToolTip()
                                                .add(
                                                        Component.translatable(
                                                                        "tooltip.otherworldinn.furniture.humidity",
                                                                        String.format(
                                                                                "%+d",
                                                                                stats.humidity))
                                                                .withStyle(
                                                                        style ->
                                                                                style.withColor(
                                                                                        ModColors
                                                                                                .HUMIDITY)));
                                    }
                });
                FurnitureSetTooltipHelper.appendSetTooltip(
                        block, event.getToolTip(), ClientServices.isShiftDown());
            }
        }
    }
}
