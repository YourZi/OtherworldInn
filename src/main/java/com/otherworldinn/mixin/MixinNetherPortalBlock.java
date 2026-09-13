package com.otherworldinn.mixin;

import com.otherworldinn.init.ModAttachments;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.teleport.NetherPortalReturnData;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 城镇⇄下界回程机制：玩家从城镇进入下界后（来源已记录到附件），
 * 再于下界走入传送门时改为回到城镇——先就近查找城镇中对应的传送门，
 * 找不到则直接回到城镇默认出生点。来源不是城镇时保持原版行为。
 */
@Mixin(NetherPortalBlock.class)
public abstract class MixinNetherPortalBlock {

    private static final BlockPos TOWN_SPAWN_POS = new BlockPos(51, 71, 0);

    @Inject(method = "getPortalDestination", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$returnToTownPortal(
            ServerLevel level, Entity entity, BlockPos pos, CallbackInfoReturnable<DimensionTransition> cir) {
        // 仅拦截“身处下界走入下界传送门”的回程，其余维度（含城镇侧入口）保持原版。
        if (level.dimension() != Level.NETHER || !(entity instanceof ServerPlayer player)) {
            return;
        }
        NetherPortalReturnData returnData = player.getData(ModAttachments.PLAYER_NETHER_PORTAL_RETURN);
        if (!returnData.shouldReturnToTown()) {
            return;
        }
        ServerLevel townLevel = level.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) {
            return; // 城镇维度不可用时回退原版行为
        }
        returnData.setReturnToTown(false); // 本次回程后清除标记，避免二次触发

        // 与下界传送门缩放比例一致地换算城镇坐标（town coordinate_scale=1，比例即 8 倍）。
        double scale = DimensionType.getTeleportationScale(level.dimensionType(), townLevel.dimensionType());
        WorldBorder townBorder = townLevel.getWorldBorder();
        BlockPos exitPos = townBorder.clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);

        Optional<BlockPos> portal =
                townLevel.getPortalForcer().findClosestPortalPosition(exitPos, false, townBorder);
        DimensionTransition transition = portal.isPresent()
                ? ((NetherPortalBlockAccessor) (Object) this)
                        .otherworldinn$getExitPortal(townLevel, entity, pos, exitPos, false, townBorder)
                : fallbackToTownSpawn(townLevel, entity);
        if (transition != null) {
            cir.setReturnValue(transition);
        }
    }

    private static DimensionTransition fallbackToTownSpawn(ServerLevel townLevel, Entity entity) {
        Vec3 spawn = new Vec3(
                TOWN_SPAWN_POS.getX() + 0.5,
                TOWN_SPAWN_POS.getY() + 1.0,
                TOWN_SPAWN_POS.getZ() + 0.5);
        return new DimensionTransition(
                townLevel, spawn, Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
    }
}
