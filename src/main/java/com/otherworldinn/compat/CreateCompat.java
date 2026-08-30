package com.otherworldinn.compat;

import com.otherworldinn.entity.store.BuilderBlueprintManager;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.BlockMovementChecks.CheckResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Create 模组兼容性处理 */
public class CreateCompat {

    public static void init() {
        BuilderBlueprintManager.exportBlueprintFiles();
        registerMovementCheck();
    }

    private static void registerMovementCheck() {
        BlockMovementChecks.registerMovementAllowedCheck(
                (state, level, pos) -> checkMovementAllowed(level, pos, state));
    }

    private static CheckResult checkMovementAllowed(Level level, BlockPos pos, BlockState state) {
        // 仅在城镇维度生效
        if (level.dimension() == TownDimensions.TOWN_LEVEL) {
            // 物理逻辑主要由服务端决定
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                TeamData team = TeamManager.getInstance().getTeamAt(pos, serverLevel.getServer());

                // 不属于任何队伍的旅社区域则禁止 Create 移动
                if (team == null) {
                    return CheckResult.FAIL;
                }
            }
        }

        // 其他情况（其他维度、旅社内、或客户端）默认放行，交给其他规则判断
        return CheckResult.PASS;
    }
}
