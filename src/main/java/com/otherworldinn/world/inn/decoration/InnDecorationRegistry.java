package com.otherworldinn.world.inn.decoration;

import com.otherworldinn.init.ModBlocks;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * 旅社装饰注册表。
 *
 * <p>形式与家具系统保持一致，按方块、标签或关键词匹配全局装饰属性。
 */
public final class InnDecorationRegistry {
    private static final Map<String, InnDecorationStats> ID_STATS = new HashMap<>();
    private static final Map<Block, InnDecorationStats> BLOCK_STATS = new HashMap<>();
    private static final Map<TagKey<Block>, InnDecorationStats> TAG_STATS = new HashMap<>();
    private static final List<KeywordRule> KEYWORD_RULES =
            List.of(
                    //new KeywordRule("photograph_frame", new InnDecorationStats("photograph_frame", 0.02D, 0.0D, 0.0D, 0.0D, 5))
                );

    static {
        initDefaultDecorations();
    }

    private InnDecorationRegistry() {}

    public static void registerBlock(Block block, InnDecorationStats stats) {
        BLOCK_STATS.put(block, stats);
        ID_STATS.put(stats.id(), stats);
    }

    public static void registerTag(TagKey<Block> tag, InnDecorationStats stats) {
        TAG_STATS.put(tag, stats);
        ID_STATS.put(stats.id(), stats);
    }

    public static Optional<InnDecorationStats> getStats(Block block) {
        return getBaseStats(block);
    }

    public static InnDecorationStats getById(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return ID_STATS.get(id);
    }

    private static Optional<InnDecorationStats> getBaseStats(Block block) {
        if (BLOCK_STATS.containsKey(block)) {
            return Optional.of(BLOCK_STATS.get(block));
        }

        var state = block.defaultBlockState();
        for (var entry : TAG_STATS.entrySet()) {
            if (state.is(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId != null) {
            String key = blockId.toString().toLowerCase(java.util.Locale.ROOT);
            for (KeywordRule rule : KEYWORD_RULES) {
                if (key.contains(rule.keyword())) {
                    ID_STATS.putIfAbsent(rule.stats().id(), rule.stats());
                    return Optional.of(rule.stats());
                }
            }
        }

        return Optional.empty();
    }

    /** 初始化默认全局装饰配置 */
    private static void initDefaultDecorations() {
        registerBlock(
                Blocks.DRAGON_EGG,
                new InnDecorationStats("dragon_egg", 0.1D, 0.1D, 0.0D, 0.0D, 1));
        registerBlock(
                ModBlocks.CLUTTER.get(),
                new InnDecorationStats("clutter", 0.0D, 0.0D, 0.0D, -0.1D, 8));
        registerBlock(
                ModBlocks.INFRASTRUCTURE_TROPHY.get(),
                new InnDecorationStats("infrastructure_trophy", 0.1D, 0.0D, 0.0D, 0.0D, 1));
        registerBlock(
                ModBlocks.SOCIAL_TROPHY.get(),
                new InnDecorationStats("social_trophy", 0.0D, 0.0D, 0.0D, 0.1D, 1));
        registerBlock(
                ModBlocks.AQUATIC_TROPHY.get(),
                new InnDecorationStats("aquatic_trophy", 0.0D, 0.1D, 0.0D, 0.0D, 1));
        registerBlock(
                ModBlocks.TRAVEL_TROPHY.get(),
                new InnDecorationStats("travel_trophy", 0.0D, 0.0D, 0.1D, 0.0D, 1));
        for (KeywordRule rule : KEYWORD_RULES) {
            ID_STATS.put(rule.stats().id(), rule.stats());
        }
    }

    private record KeywordRule(String keyword, InnDecorationStats stats) {
        private KeywordRule {
            keyword = keyword.toLowerCase(java.util.Locale.ROOT);
        }
    }
}
