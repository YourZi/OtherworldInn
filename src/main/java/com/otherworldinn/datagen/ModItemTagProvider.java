package com.otherworldinn.datagen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModBlocks;
import com.otherworldinn.init.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

/** 物品标签生成器：生成 tags/item JSON 文件 */
public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, OtherworldInn.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(OtherworldInn.BANNED_IN_TOWN)
                .addOptional(ResourceLocation.fromNamespaceAndPath("naturescompass", "naturescompass"));

        tag(OtherworldInn.ONLY_IN_TOWN)
                .add(ModItems.ROOM_REGISTER.get())
                .add(ModItems.LAND_DEED.get())
                .add(ModItems.INN_KEY.get())
                .add(ModItems.ROOM_KEY.get())
                .add(ModBlocks.CRYSTAL_BALL.asItem())
                ;
    }
}
