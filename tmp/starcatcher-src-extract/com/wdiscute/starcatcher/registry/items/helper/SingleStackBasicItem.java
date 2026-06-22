package com.wdiscute.starcatcher.registry.items.helper;

import net.minecraft.world.item.Item;

public class SingleStackBasicItem extends Item
{
    public SingleStackBasicItem()
    {
        super(new Properties().stacksTo(1));
    }
}
