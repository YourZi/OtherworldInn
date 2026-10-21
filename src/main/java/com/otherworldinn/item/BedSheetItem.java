package com.otherworldinn.item;

import com.otherworldinn.foundation.ModBlockProperties;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.world.event.listener.TownZonePolicyService;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

/** 床单物品：右键清理脏乱的床，消耗耐久并获得脏床单。 */
public class BedSheetItem extends Item {

    public BedSheetItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (state.getBlock() instanceof BedBlock
                && state.hasProperty(ModBlockProperties.MESSY)
                && state.getValue(ModBlockProperties.MESSY)) {

            if (!level.isClientSide) {
                if (level instanceof ServerLevel serverLevel && cleanMessyBed(serverLevel, pos)) {
                    ItemStack dirtySheet = createDirtySheet(stack, 1);
                    if (stack.getCount() > 1) {
                        stack.shrink(1);
                        if (!player.getInventory().add(dirtySheet)) {
                            player.drop(dirtySheet, false);
                        }
                    } else {
                        player.setItemInHand(context.getHand(), dirtySheet);
                    }
                }
            } else {
                level.playSound(
                        player, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                for (int i = 0; i < 5; i++) {
                    level.addParticle(
                            ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5 + (level.random.nextDouble() - 0.5),
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5),
                            0,
                            0,
                            0);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    /**
     * 清除床的脏乱状态（换床单）：只有整张床（含另一半）都确认不再脏乱才返回 {@code true}，调用方据此决定是否消耗干净床单。
     * 换床单属于玩家/女仆主动整理客房，故显式进入 {@link TownZonePolicyService.ProtectionBypassReason#ROOM_CLEAN_BED_SHEET} 旁路窗口。
     */
    public static boolean cleanMessyBed(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isMessyBedState(state)) {
            return false;
        }

        BedPart part = state.getValue(BedBlock.PART);
        BlockPos otherPos =
                pos.relative(
                        part == BedPart.HEAD
                                ? state.getValue(BedBlock.FACING).getOpposite()
                                : state.getValue(BedBlock.FACING));

        try (TownZonePolicyService.ProtectionBypassScope ignored =
                TownZonePolicyService.beginProtectionBypass(
                        TownZonePolicyService.ProtectionBypassReason.ROOM_CLEAN_BED_SHEET,
                        List.of(pos.immutable(), otherPos.immutable()))) {
            clearMessyAt(level, pos);
            clearMessyAt(level, otherPos);
        }

        if (isMessyBedState(level.getBlockState(pos))
                || isMessyBedState(level.getBlockState(otherPos))) {
            return false;
        }

        TeamData team = TeamManager.getInstance().getTeamAt(pos, level.getServer());
        if (team != null) {
            InnData innData = team.getInnData();
            RoomData room = innData.getRoomAt(pos);
            if (room != null) {
                innData.checkAndUpdateRoom(room.getId(), level, team);
                TeamManager.getInstance().syncTeam(team, level.getServer());
            }
        }
        return true;
    }

    /** 单个床格（头或脚）去脏；不在此处校验，由 {@link #cleanMessyBed} 统一校验整张床的最终状态。 */
    private static void clearMessyAt(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!isMessyBedState(state)) {
            return;
        }
        level.setBlock(pos, state.setValue(ModBlockProperties.MESSY, false), 3);
    }

    private static boolean isMessyBedState(BlockState state) {
        return state.getBlock() instanceof BedBlock
                && state.hasProperty(ModBlockProperties.MESSY)
                && state.getValue(ModBlockProperties.MESSY);
    }

    public static ItemStack createDirtySheet(ItemStack source, int extraDamage) {
        ItemStack dirtySheet = new ItemStack(ModItems.MESSY_BED_SHEET.get());
        int damage = Math.max(0, source.getDamageValue() + Math.max(0, extraDamage));
        dirtySheet.setDamageValue(Math.min(dirtySheet.getMaxDamage(), damage));
        return dirtySheet;
    }
}
