package com.otherworldinn.mixin;

import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import com.otherworldinn.world.inn.listener.InnEventHandler;
import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin 类，用于修改 Contraption 的行为。
 *
 * <p>
 * 主要功能：
 * <ol>
 *   <li>捕获当前的 StructureBlockInfo，用于在 customBlockPlacement 中判断是否在合法区域。
 *   <li>拦截 customBlockPlacement 方法，在城镇维度且不在任何旅社范围内时，禁止放置。
 * </ol>
 */
@Mixin(Contraption.class)
public class MixinContraption {

    @Unique private final ThreadLocal<StructureBlockInfo> currentBlockInfo = new ThreadLocal<>();

    @Shadow
    protected boolean customBlockPlacement(LevelAccessor world, BlockPos pos, BlockState state) {
        return false;
    }

    // 1. 捕获当前的 StructureBlockInfo
    @ModifyVariable(
            method = "addBlocksToWorld",
            at = @At("LOAD"), // 在加载 block 变量时捕获
            ordinal = 0)
    private StructureBlockInfo captureCurrentBlock(StructureBlockInfo block) {
        currentBlockInfo.set(block);
        return block;
    }

    // 2. 拦截 customBlockPlacement
    @Redirect(
            method = "addBlocksToWorld",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/simibubi/create/content/contraptions/Contraption;customBlockPlacement(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean interceptPlacement(
            Contraption instance, LevelAccessor worldAccessor, BlockPos pos, BlockState state) {
        if (worldAccessor instanceof Level world && !world.isClientSide) {
            // 仅在城镇维度生效
            if (world.dimension() == TownDimensions.TOWN_LEVEL) {
                if (world instanceof net.minecraft.server.level.ServerLevel serverLevel
                        && TownZonePolicyService.resolveContraptionPlacementPolicy(serverLevel, pos, state)
                                == TownZonePolicyService.ContraptionPlacementPolicy.DENY_AND_DROP) {
                    Block.dropResources(state, world, pos, null);

                    StructureBlockInfo info = currentBlockInfo.get();
                    if (info != null
                            && info.nbt() != null
                            && state.getBlock() instanceof EntityBlock entityBlock) {
                        try {
                            BlockEntity be = entityBlock.newBlockEntity(pos, state);
                            if (be != null) {
                                be.loadWithComponents(info.nbt(), world.registryAccess());
                                if (be instanceof net.minecraft.world.Container container) {
                                    Containers.dropContents(world, pos, container);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
            }
        }

        // 合法区域，执行默认逻辑
        boolean placed = this.customBlockPlacement(worldAccessor, pos, state);
        if (placed && worldAccessor instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            InnEventHandler.markInnBlockChanged(serverLevel, pos);
        }
        return placed;
    }
}
