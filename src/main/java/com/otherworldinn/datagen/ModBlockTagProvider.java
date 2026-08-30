package com.otherworldinn.datagen;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.BlockDataGenInfo;
import com.otherworldinn.init.ModBlocks;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import org.jetbrains.annotations.Nullable;

/** 方块标签生成器 负责生成 tags/block JSON 文件 (如 mineable/pickaxe) */
public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, OtherworldInn.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(OtherworldInn.INN_FREE_INTERACT)
                .add(Blocks.CAKE);

        for (Map.Entry<DeferredBlock<?>, BlockDataGenInfo> entry :
                ModBlocks.BLOCK_INFOS.entrySet()) {
            DeferredBlock<?> block = entry.getKey();
            BlockDataGenInfo info = entry.getValue();

            switch (info.toolType()) {
                case PICKAXE -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get());
                case AXE -> tag(BlockTags.MINEABLE_WITH_AXE).add(block.get());
                case SHOVEL -> tag(BlockTags.MINEABLE_WITH_SHOVEL).add(block.get());
                case HOE -> tag(BlockTags.MINEABLE_WITH_HOE).add(block.get());
            }

            switch (info.miningLevel()) {
                case STONE -> tag(BlockTags.NEEDS_STONE_TOOL).add(block.get());
                case IRON -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get());
                case DIAMOND -> tag(BlockTags.NEEDS_DIAMOND_TOOL).add(block.get());
            }
        }
    }
}
