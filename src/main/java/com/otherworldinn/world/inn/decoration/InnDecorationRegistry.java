package com.otherworldinn.world.inn.decoration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 旅社装饰注册表。
 *
 * <p>新版改为单方块全局装饰，按方块、标签或关键词匹配效果定义。
 */
public final class InnDecorationRegistry {
    private static final Map<String, InnDecorationDefinition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<Block, InnDecorationDefinition> BLOCK_DEFINITIONS = new LinkedHashMap<>();
    private static final Map<TagKey<Block>, InnDecorationDefinition> TAG_DEFINITIONS =
            new LinkedHashMap<>();
    private static final List<KeywordRule> KEYWORD_RULES = new ArrayList<>();

    static {
        registerDefaults();
    }

    private InnDecorationRegistry() {}

    public static InnDecorationDefinition register(
            String id,
            String enName,
            String zhName,
            String translationKey,
            List<InnDecorationBuff> buffs,
            int maxInstances) {
        InnDecorationDefinition definition =
                new InnDecorationDefinition(id, enName, zhName, translationKey, buffs, maxInstances);
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static InnDecorationDefinition get(String id) {
        return DEFINITIONS.get(id);
    }

    public static Collection<InnDecorationDefinition> getAll() {
        return DEFINITIONS.values();
    }

    public static Optional<InnDecorationDefinition> resolve(Block block) {
        InnDecorationDefinition definition = BLOCK_DEFINITIONS.get(block);
        if (definition != null) {
            return Optional.of(definition);
        }

        var defaultState = block.defaultBlockState();
        for (Map.Entry<TagKey<Block>, InnDecorationDefinition> entry : TAG_DEFINITIONS.entrySet()) {
            if (defaultState.is(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        if (blockId != null) {
            String key = blockId.toString().toLowerCase(java.util.Locale.ROOT);
            for (KeywordRule rule : KEYWORD_RULES) {
                if (key.contains(rule.keyword())) {
                    return Optional.of(rule.definition());
                }
            }
        }

        return Optional.empty();
    }

    public static void registerBlock(Block block, InnDecorationDefinition definition) {
        BLOCK_DEFINITIONS.put(block, definition);
    }

    public static void registerTag(TagKey<Block> tag, InnDecorationDefinition definition) {
        TAG_DEFINITIONS.put(tag, definition);
    }

    public static void registerKeyword(String keyword, InnDecorationDefinition definition) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("Decoration keyword cannot be blank.");
        }
        KEYWORD_RULES.add(new KeywordRule(keyword, definition));
    }

    private static void registerDefaults() {
        InnDecorationDefinition coralRockery =
                register(
                "coral_rockery",
                "Coral Rockery",
                "珊瑚假山",
                "inn_decoration.otherworldinn.coral_rockery",
                List.of(new InnDecorationBuff(InnDecorationBuffType.LODGING_INCOME_MULTIPLIER, 0.02D)),
                5);
        registerKeyword("coral", coralRockery);

        InnDecorationDefinition fishTankDisplay =
                register(
                        "fish_tank_display",
                        "Fish Tank Display",
                        "观赏鱼缸",
                        "inn_decoration.otherworldinn.fish_tank_display",
                        List.of(
                                new InnDecorationBuff(
                                        InnDecorationBuffType.GUEST_ARRIVAL_SPEED_MULTIPLIER,
                                        0.03D)),
                        3);
        registerKeyword("fish_tank", fishTankDisplay);

        InnDecorationDefinition photographFrame =
                register(
                        "photograph_frame",
                        "Photograph Frame",
                        "摄影相框",
                        "inn_decoration.otherworldinn.photograph_frame",
                        List.of(
                                new InnDecorationBuff(
                                        InnDecorationBuffType.REPUTATION_GAIN_MULTIPLIER, 0.02D)),
                        5);
        registerKeyword("photograph_frame", photographFrame);
    }

    private record KeywordRule(String keyword, InnDecorationDefinition definition) {
        private KeywordRule {
            keyword = keyword.toLowerCase(java.util.Locale.ROOT);
        }
    }
}
