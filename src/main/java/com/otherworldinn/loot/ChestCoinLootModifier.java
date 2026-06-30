package com.otherworldinn.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.otherworldinn.init.ModItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class ChestCoinLootModifier extends LootModifier {
    private static final float COIN_CHANCE = 0.5F;
    private static final int MIN_COIN_COUNT = 1;
    private static final int MAX_COIN_COUNT = 5;

    public static final MapCodec<ChestCoinLootModifier> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            LootModifier.codecStart(instance)
                                    .apply(instance, ChestCoinLootModifier::new));

    public ChestCoinLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!isChestLoot(context)) {
            return generatedLoot;
        }
        if (context.getRandom().nextFloat() >= COIN_CHANCE) {
            return generatedLoot;
        }

        int coinCount = MIN_COIN_COUNT + context.getRandom().nextInt(MAX_COIN_COUNT - MIN_COIN_COUNT + 1);
        generatedLoot.add(new ItemStack(ModItems.COIN.get(), coinCount));
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    private static boolean isChestLoot(LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        return lootTableId != null && lootTableId.getPath().startsWith("chests/");
    }
}
