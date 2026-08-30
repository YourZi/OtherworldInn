package com.otherworldinn.datagen;

import com.otherworldinn.foundation.BlockDataGenInfo;
import com.otherworldinn.foundation.LootConfig;
import com.otherworldinn.init.ModBlocks;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredBlock;

/** 方块战利品表生成器 负责生成方块被破坏时的掉落物 */
public class ModBlockLootSubProvider extends BlockLootSubProvider {
    private final HolderLookup.Provider registries;

    protected ModBlockLootSubProvider(HolderLookup.Provider provider) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), provider);
        this.registries = provider;
    }

    @Override
    protected void generate() {
        for (Map.Entry<DeferredBlock<?>, BlockDataGenInfo> entry :
                ModBlocks.BLOCK_INFOS.entrySet()) {
            DeferredBlock<?> block = entry.getKey();

            if (block.get().getLootTable() == BuiltInLootTables.EMPTY) {
                continue;
            }

            BlockDataGenInfo info = entry.getValue();
            LootConfig lootConfig = info.lootConfig();

            switch (lootConfig.type()) {
                case DROP_SELF -> dropSelf(block.get());
                case DROP_NOTHING -> add(block.get(), noDrop());
                case CUSTOM -> {
                    LootTable.Builder poolBuilder = LootTable.lootTable();

                    if (lootConfig.silkTouchDropSelf()) {
                        LootPool.Builder silkTouchPool =
                                LootPool.lootPool()
                                        .when(hasSilkTouch())
                                        .setRolls(ConstantValue.exactly(1.0F))
                                        .add(LootItem.lootTableItem(block.get()));
                        poolBuilder.withPool(silkTouchPool);
                    }

                    for (LootConfig.LootEntry lootEntry : lootConfig.entries()) {
                        Item item =
                                BuiltInRegistries.ITEM.get(
                                        ResourceLocation.parse(lootEntry.itemId()));

                        LootPool.Builder entryPool =
                                LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));

                        // 如果启用了精准采集掉落自身，则这些自定义掉落物仅在未使用精准采集时掉落
                        if (lootConfig.silkTouchDropSelf()) {
                            entryPool.when(doesNotHaveSilkTouch());
                        }

                        if (lootEntry.chance() < 1.0f) {
                            entryPool.when(
                                    LootItemRandomChanceCondition.randomChance(lootEntry.chance()));
                        }

                        entryPool.add(
                                LootItem.lootTableItem(item)
                                        .apply(
                                                SetItemCountFunction.setCount(
                                                        UniformGenerator.between(
                                                                lootEntry.minCount(),
                                                                lootEntry.maxCount())))
                                        .apply(
                                                ApplyBonusCount.addOreBonusCount(
                                                        this.registries
                                                                .lookupOrThrow(
                                                                        Registries.ENCHANTMENT)
                                                                .getOrThrow(
                                                                        net.minecraft.world.item
                                                                                .enchantment
                                                                                .Enchantments
                                                                                .FORTUNE))));

                        poolBuilder.withPool(entryPool);
                    }

                    add(block.get(), poolBuilder);
                }
            }
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // 返回所有已注册的方块，确保验证通过
        return ModBlocks.BLOCKS.getEntries().stream()
                .map(holder -> (Block) holder.get())
                .collect(Collectors.toList());
    }
}
