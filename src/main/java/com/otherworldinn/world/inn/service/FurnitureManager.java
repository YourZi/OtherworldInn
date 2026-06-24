package com.otherworldinn.world.inn.service;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.ModColors;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 家具管理器
 *
 * <p>管理所有家具的属性配置，包括舒适度、光照度和湿度。 支持为特定方块或方块标签配置属性。
 */
public class FurnitureManager {

    private static final Map<Block, FurnitureStats> BLOCK_STATS = new HashMap<>();
    private static final Map<TagKey<Block>, FurnitureStats> TAG_STATS = new HashMap<>();
    private static final Map<Block, Integer> BLOCK_LIGHT_LEVEL_CACHE = new HashMap<>();
    private static final List<KeywordRule> KEYWORD_RULES = List.of(
            new KeywordRule("fish_tank", new FurnitureStats(2, 0, 10)),
            new KeywordRule("potted", new FurnitureStats(3, 0, 7)),
            new KeywordRule("bookshelf", new FurnitureStats(5, 0, 0)),
            new KeywordRule("desk", new FurnitureStats(4, 0, 0)),
            new KeywordRule("table", new FurnitureStats(4, 0, 0)),
            new KeywordRule("drawer", new FurnitureStats(3, 0, 0)),
            new KeywordRule("sofa", new FurnitureStats(3, 0, 0)),
            new KeywordRule("carpet", new FurnitureStats(3, 0, 0)),
            new KeywordRule("chair", new FurnitureStats(2, 0, 0)),
            new KeywordRule("wardrobe", new FurnitureStats(5, 0, 0)),
            new KeywordRule("sink", new FurnitureStats(2, 0, 7)),
            new KeywordRule("couch", new FurnitureStats(3, 0, 0)),
            new KeywordRule("chaise", new FurnitureStats(3, 0, 0)),
            new KeywordRule("toilet", new FurnitureStats(5, 0, 10)),
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

    /**
     * 为指定方块注册家具属性
     *
     * @param block 目标方块
     * @param stats 家具属性
     */
    public static void registerBlock(Block block, FurnitureStats stats) {
        BLOCK_STATS.put(block, stats);
    }

    /**
     * 为指定方块标签注册家具属性
     *
     * @param tag 目标方块标签
     * @param stats 家具属性
     */
    public static void registerTag(TagKey<Block> tag, FurnitureStats stats) {
        TAG_STATS.put(tag, stats);
    }

    /**
     * 获取指定方块的家具属性
     *
     * <p>优先匹配方块本身的配置，其次匹配标签配置。
     *
     * @param block 目标方块
     * @return 对应的家具属性，如果未配置则返回空
     */
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

        // 检查标签匹配
        // 注意：这里需要遍历所有已注册的标签，可能会有性能影响
        // 对于服务端频繁查询，建议后续增加缓存机制
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

    /**
     * 家具属性记录类
     *
     * <p>包含舒适度、光照度和湿度三个维度的数值。
     *
     * @param comfort 舒适度 (-20 ~ 20)
     * @param light 光照度 (-20 ~ 20)
     * @param humidity 湿度 (-20 ~ 20)
     */
    public record FurnitureStats(int comfort, int light, int humidity) {}

    private record KeywordRule(String keyword, FurnitureStats stats) {}

    /** 客户端事件处理器 */
    @EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
    public static class ClientHandler {
        /**
         * 处理物品提示框事件，显示家具属性
         *
         * @param event 物品提示框事件
         */
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
            }
        }
    }
}
