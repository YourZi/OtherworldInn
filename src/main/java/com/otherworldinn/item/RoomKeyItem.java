package com.otherworldinn.item;

import com.otherworldinn.entity.base.GuestEntity;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.util.AdvancementUtils;
import com.otherworldinn.world.inn.GuestData;
import com.otherworldinn.world.inn.InnData;
import com.otherworldinn.world.inn.RoomData;
import com.otherworldinn.world.inn.decoration.InnDecorationBuffType;
import com.otherworldinn.world.inn.service.RoomThemeManager;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import com.otherworldinn.world.team.TeamSavedData;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * 房间钥匙
 *
 * <p>用于绑定特定房间。 右键房间内方块绑定，左键点击取消绑定。 右键旅客可将其分配到绑定房间（需消耗钥匙）。
 */
public class RoomKeyItem extends Item {

    public RoomKeyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity interactionTarget,
            InteractionHand usedHand) {
        if (interactionTarget.level().isClientSide) {
            return InteractionResult.PASS;
        }

        if (interactionTarget instanceof GuestEntity guestEntity
                && player instanceof ServerPlayer serverPlayer) {
            Optional<Integer> roomIdOpt = getBoundRoomId(stack);
            if (roomIdOpt.isEmpty()) {
                return InteractionResult.PASS;
            }

            int roomId = roomIdOpt.get();
            GuestData guestData = guestEntity.getGuestData();

            // 检查旅客是否能入住
            GuestData.GuestState state = guestData.getState();
            if (guestData.getRoomId() != -1 && state != GuestData.GuestState.WAITING) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_guest_busy")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            // 检查是否已退房
            if (guestData.isCheckedOut()) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_checked_out")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            TeamData team =
                    TeamManager.getInstance()
                            .getTeamAt(interactionTarget.blockPosition(), serverPlayer.getServer());

            // 如果不在旅社范围内，且旅客处于等待状态，尝试在所有队伍中寻找匹配的房间
            if (team == null && state == GuestData.GuestState.WAITING) {
                TeamSavedData teamData =
                        TeamManager.getInstance().getData(serverPlayer.getServer());
                Optional<UUID> boundUuidOpt = getBoundRoomUUID(stack);

                if (teamData != null) {
                    for (TeamData t : teamData.getTeams().values()) {
                        RoomData potentialRoom = t.getInnData().getRoom(roomId);
                        if (potentialRoom != null) {
                            // 如果绑定了UUID，必须匹配
                            if (boundUuidOpt.isPresent()) {
                                if (boundUuidOpt.get().equals(potentialRoom.getUuid())) {
                                    team = t;
                                    break;
                                }
                            } else {
                                // 如果没有UUID绑定（旧数据？），直接匹配ID（可能不准确，但在单人/少队伍情况下通常没问题）
                                team = t;
                                break;
                            }
                        }
                    }
                }
            }

            if (team == null) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_not_in_inn")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            InnData innData = team.getInnData();
            RoomData room = innData.getRoom(roomId);

            // 检查房间是否存在
            if (room == null) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_no_room")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            // 校验房间UUID（防止 ID 复用导致的错误）
            Optional<UUID> boundUuidOpt = getBoundRoomUUID(stack);
            if (boundUuidOpt.isPresent() && !boundUuidOpt.get().equals(room.getUuid())) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_id_mismatch")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            // 检查房间是否有空床位
            if (room.getCurrentGuests().size() >= room.getMaxGuests()) {
                player.displayClientMessage(
                        Component.translatable("message.otherworldinn.room_key.checkin_fail_full")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            // 执行入住
            if (innData.checkIn(guestData.getUuid(), roomId, serverPlayer.serverLevel())) {
                if (innData.getTotalCheckInCount() >= 1) {
                    AdvancementUtils.award(serverPlayer, AdvancementUtils.SERVE_FIRST_GUEST);
                }
                // 消耗钥匙
                stack.shrink(1);

                // 播放音效
                player.level()
                        .playSound(
                                null,
                                player.blockPosition(),
                                SoundEvents.PLAYER_LEVELUP,
                                SoundSource.PLAYERS,
                                0.5F,
                                1.0F);

                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_success",
                                        RoomData.getDisplayName(room))
                                .withStyle(style -> style.withColor(ModColors.SUCCESS)),
                        true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel) {
            TeamData team = TeamManager.getInstance().getTeamAt(pos, serverLevel.getServer());
            if (team != null) {
                // 获取点击位置的房间
                RoomData room = team.getInnData().getRoomAt(pos);
                if (room != null) {
                    // 绑定到该房间
                    bindRoom(stack, room.getId(), room.getUuid());
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.room_key.bound",
                                            RoomData.getDisplayName(room))
                                    .withStyle(style -> style.withColor(ModColors.SUCCESS)),
                            true);

                    // 播放音效 (音符盒叮声)
                    level.playSound(
                            null,
                            pos,
                            SoundEvents.NOTE_BLOCK_BELL.value(),
                            SoundSource.PLAYERS,
                            1.0F,
                            1.5F);

                    return InteractionResult.SUCCESS;
                }
            }

            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.room_key.no_room")
                            .withStyle(style -> style.withColor(ModColors.ERROR)),
                    true);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public Component getName(ItemStack stack) {
        Optional<Integer> roomId = getBoundRoomId(stack);
        if (roomId.isPresent()) {
            int id = roomId.get();
            // 尝试获取房间显示名称
            TeamData clientTeam = TeamManager.getInstance().getClientPlayerTeam();
            String roomDisplay = null;
            if (clientTeam != null) {
                RoomData room = clientTeam.getInnData().getRoom(id);
                if (room != null) {
                    roomDisplay = RoomData.getDisplayName(room);
                }
            }
            if (roomDisplay == null) {
                roomDisplay = id + ""; // 回退到数字ID
            }
            if (isBoundRoomFull(stack)) {
                return Component.translatable("item.otherworldinn.room_key.bound_full", roomDisplay);
            }
            return Component.translatable("item.otherworldinn.room_key.bound", roomDisplay);
        }
        return super.getName(stack);
    }

    public static boolean isBoundRoomFull(ItemStack stack) {
        Optional<Integer> roomId = getBoundRoomId(stack);
        if (roomId.isEmpty()) {
            return false;
        }
        TeamData team = TeamManager.getInstance().getClientPlayerTeam();
        if (team == null) {
            return false;
        }
        RoomData room = team.getInnData().getRoom(roomId.get());
        return room != null && room.getCurrentGuests().size() >= room.getMaxGuests();
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getBoundRoomId(stack).isPresent();
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        getBoundRoomId(stack)
                .ifPresent(
                        roomId -> {
                            // 从客户端缓存获取房间信息
                            TeamData team = TeamManager.getInstance().getClientPlayerTeam();
                            if (team != null) {
                                RoomData room = team.getInnData().getRoom(roomId);
                                if (room != null) {
                                    // 房间编号
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.room_id",
                                                            roomId)
                                                    .withStyle(ChatFormatting.GOLD));

                                    // 位置
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.pos",
                                                            room.getMinPos().toShortString(),
                                                            room.getMaxPos().toShortString())
                                                    .withStyle(ChatFormatting.GRAY));

                                    // 价格
                                    int rating = team.getInnData().getRating();
                                    int price =
                                            team.getInnData()
                                                    .applyPositiveDecorationBuff(
                                                            room.getBedPrice(rating),
                                                            InnDecorationBuffType
                                                                    .LODGING_INCOME_MULTIPLIER);
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.price",
                                                            price)
                                                    .withStyle(ChatFormatting.YELLOW));

                                    // 床位数
                                    int maxGuests = room.getMaxGuests();
                                    int currentGuests = room.getCurrentGuests().size();
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.beds",
                                                            currentGuests,
                                                            maxGuests)
                                                    .withStyle(ChatFormatting.BLUE));

                                    // 房间属性
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.furniture.comfort",
                                                            room.getComfort())
                                                    .withStyle(
                                                            style ->
                                                                    style.withColor(
                                                                            ModColors.COMFORT)));
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.furniture.light",
                                                            room.getLight())
                                                    .withStyle(
                                                            style ->
                                                                    style.withColor(
                                                                            ModColors.LIGHT)));
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.furniture.humidity",
                                                            room.getHumidity())
                                                    .withStyle(
                                                            style ->
                                                                    style.withColor(
                                                                            ModColors.HUMIDITY)));

                                    String themeId = room.getTheme();
                                    if (themeId != null && !themeId.isEmpty()) {
                                        String themeName =
                                                RoomThemeManager.getDisplayName(themeId, true);
                                        tooltipComponents.add(
                                                Component.translatable(
                                                                "tooltip.otherworldinn.room_key.theme",
                                                                themeName)
                                                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                                    }
                                }
                            }
                        });
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // --- 辅助方法 ---

    public static void bindRoom(ItemStack stack, int roomId, UUID roomUuid) {
        CompoundTag tag =
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("RoomId", roomId);
        tag.putUUID("RoomUUID", roomUuid);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void unbindRoom(ItemStack stack) {
        CompoundTag tag =
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove("RoomId");
        tag.remove("RoomUUID");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static Optional<Integer> getBoundRoomId(ItemStack stack) {
        CompoundTag tag =
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        if (tag != null && tag.contains("RoomId")) {
            return Optional.of(tag.getInt("RoomId"));
        }
        return Optional.empty();
    }

    public static Optional<UUID> getBoundRoomUUID(ItemStack stack) {
        CompoundTag tag =
                stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        if (tag != null && tag.contains("RoomUUID")) {
            return Optional.of(tag.getUUID("RoomUUID"));
        }
        return Optional.empty();
    }
}
