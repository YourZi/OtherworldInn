package com.wdiscute.starcatcher.registry.items;

import com.wdiscute.starcatcher.fishentity.FishEntity;
import com.wdiscute.starcatcher.registry.SCEntities;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class FishItem extends Item
{
    public FishItem()
    {
        super(new Item.Properties().food(SCFoodProperties.BASIC_RAW_FISH));
    }

    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        if(context.getPlayer().isCreative())
        {
            FishEntity fe = new FishEntity(SCEntities.FISH.get(), context.getLevel());
            fe.setFish(context.getItemInHand().copyWithCount(1));
            fe.setPos(context.getClickedPos().relative(context.getClickedFace()).getCenter());

            context.getLevel().addFreshEntity(fe);

            ItemStack is = context.getPlayer().getItemInHand(context.getHand());
            is.shrink(1);

            context.getPlayer().setItemInHand(context.getHand(), is);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
