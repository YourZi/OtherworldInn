package com.otherworldinn.init;

import com.mojang.serialization.Codec;
import com.otherworldinn.OtherworldInn;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 自定义数据组件注册。 */
public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, OtherworldInn.MODID);

    /** 棉花糖烤熟度（0-100），复用耐久条显示。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> MARSHMALLOW_ROAST =
            DATA_COMPONENT_TYPES.register(
                    "marshmallow_roast",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.intRange(0, 100))
                            .networkSynchronized(ByteBufCodecs.VAR_INT)
                            .build());
}
