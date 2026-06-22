package com.wdiscute.starcatcher.registry.items;

import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.catchmodifiers.SCCatchModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HatItem extends BlockItem implements Equipable
{

    public HatItem(Block block)
    {
        super(block, new Properties()
                .stacksTo(1)
        );
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot()
    {
        return EquipmentSlot.HEAD;
    }
}
