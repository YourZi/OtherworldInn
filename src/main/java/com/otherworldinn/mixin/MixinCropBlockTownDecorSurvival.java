package com.otherworldinn.mixin;

import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.inn.facility.FacilityRegistry;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 城镇普通区域装饰作物生存兜底：
 * 旅社/温室范围外，允许作物在非耕地上保持存在，避免进图后大面积自毁掉落。
 */
@Mixin(CropBlock.class)
public class MixinCropBlockTownDecorSurvival {
    private static final String GREENHOUSE_FACILITY_ID = "greenhouse";

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$allowDecorCropOutsideInnAndGreenhouse(
            BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!isNormalTownDecorArea(level, pos) 
            || level.getBlockState(pos).is(Blocks.WATER)  
            || level.getBlockState(pos).is(Blocks.SEAGRASS)
        ) {
            return;
        }
        cir.setReturnValue(true);
    }

    private static boolean isNormalTownDecorArea(LevelReader level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) {
            return false;
        }
        if (serverLevel.dimension() != TownDimensions.TOWN_LEVEL) {
            return false;
        }
        if (TeamManager.getInstance().getTeamAt(pos, serverLevel.getServer()) != null) {
            return false;
        }
        return getGreenhouseLevelAtPos(serverLevel, pos) <= 0;
    }

    private static int getGreenhouseLevelAtPos(ServerLevel level, BlockPos pos) {
        FacilityRegistry.FacilityDefinition greenhouse = FacilityRegistry.get(GREENHOUSE_FACILITY_ID);
        if (greenhouse == null || pos == null) {
            return 0;
        }
        TeamManager manager = TeamManager.getInstance();
        TeamData teamAtPos = manager.getTeamAt(pos, level.getServer());
        int levelAtPos = getGreenhouseLevelForTeamAtPos(teamAtPos, greenhouse, pos);
        if (levelAtPos > 0) {
            return levelAtPos;
        }
        TeamData nearestTeam = manager.getNearestInn(pos, level.getServer());
        if (nearestTeam == null || nearestTeam == teamAtPos) {
            return 0;
        }
        return getGreenhouseLevelForTeamAtPos(nearestTeam, greenhouse, pos);
    }

    private static int getGreenhouseLevelForTeamAtPos(
            TeamData team, FacilityRegistry.FacilityDefinition greenhouse, BlockPos pos) {
        if (team == null || greenhouse == null || pos == null) {
            return 0;
        }
        int greenhouseLevel = Math.max(0, team.getInnData().getFacilityLevel(GREENHOUSE_FACILITY_ID));
        if (greenhouseLevel <= 0) {
            return 0;
        }
        if (greenhouse.facilityRange().contains(pos)) {
            return greenhouseLevel;
        }
        for (FacilityRegistry.FacilityRange range : greenhouse.getExtraBuildAllowRanges(greenhouseLevel)) {
            if (range.contains(pos)) {
                return greenhouseLevel;
            }
        }
        return 0;
    }
}
