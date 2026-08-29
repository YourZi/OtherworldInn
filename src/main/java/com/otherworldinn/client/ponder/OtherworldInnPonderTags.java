package com.otherworldinn.client.ponder;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class OtherworldInnPonderTags {
    public static final ResourceLocation INN_SYSTEM =
            ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "inn_system");

    private OtherworldInnPonderTags() {}

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<ItemLike> itemHelper =
                helper.withKeyFunction(itemLike -> BuiltInRegistries.ITEM.getKey(itemLike.asItem()));

        itemHelper.registerTag(INN_SYSTEM)
                .title("Inn Systems")
                .description("Tutorials for facilities, rooms, and inn management in Otherworld Inn")
                .item(ModItems.ROOM_REGISTER.get())
                .addToIndex()
                .register();

        itemHelper.addToTag(INN_SYSTEM)
                .add(createItem())
                .add(createWrench())
                .add(Items.WOODEN_HOE)
                .add(Items.STONE_HOE)
                .add(Items.IRON_HOE)
                .add(Items.WOODEN_PICKAXE)
                .add(Items.STONE_PICKAXE)
                .add(Items.IRON_PICKAXE)
                .add(ModItems.INN_KEY.get())
                .add(ModItems.ROOM_REGISTER.get())
                .add(ModItems.ROOM_KEY.get());
    }

    private static Item createItem() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", "depot"));
    }

    private static Item createWrench() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("create", "wrench"));
    }
}
