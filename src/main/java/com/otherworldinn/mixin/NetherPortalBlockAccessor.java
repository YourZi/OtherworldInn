package com.otherworldinn.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 暴露 NetherPortalBlock 的私有回程逻辑（找到传送门分支），供 MixinNetherPortalBlock 复用。 */
@Mixin(NetherPortalBlock.class)
public interface NetherPortalBlockAccessor {

    @Invoker("getExitPortal")
    DimensionTransition otherworldinn$getExitPortal(
            ServerLevel level, Entity entity, BlockPos pos, BlockPos exitPos, boolean isNether, WorldBorder worldBorder);
}
