package com.otherworldinn.init;

import com.mojang.serialization.MapCodec;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.loot.ChestCoinLootModifier;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, OtherworldInn.MODID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<ChestCoinLootModifier>>
            CHEST_COIN = LOOT_MODIFIER_SERIALIZERS.register("chest_coin", () -> ChestCoinLootModifier.CODEC);

    private ModLootModifiers() {}
}
