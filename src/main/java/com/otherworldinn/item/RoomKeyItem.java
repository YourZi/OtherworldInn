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

/** 房间钥匙：右键方块绑定/左键解绑房间，右键旅客为其办理入住并消耗钥匙。 */
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

            GuestData.GuestState state = guestData.getState();
            if (guestData.getRoomId() != -1 && state != GuestData.GuestState.WAITING) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_guest_busy")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

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

            // 不在旅社范围且旅客在等待时，跨队伍寻找匹配房间
            if (team == null && state == GuestData.GuestState.WAITING) {
                TeamSavedData teamData =
                        TeamManager.getInstance().getData(serverPlayer.getServer());
                Optional<UUID> boundUuidOpt = getBoundRoomUUID(stack);

                if (teamData != null) {
                    for (TeamData t : teamData.getTeams().values()) {
                        RoomData potentialRoom = t.getInnData().getRoom(roomId);
                        if (potentialRoom != null) {
                            if (boundUuidOpt.isPresent()) {
                                if (boundUuidOpt.get().equals(potentialRoom.getUuid())) {
                                    team = t;
                                    break;
                                }
                            } else {
                                // 旧存档可能只有ID绑定：按ID匹配，多队伍时可能不准
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

            if (room == null) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_no_room")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            // 房间ID可能被复用，须用UUID确认是绑定时的那个房间
            Optional<UUID> boundUuidOpt = getBoundRoomUUID(stack);
            if (boundUuidOpt.isPresent() && !boundUuidOpt.get().equals(room.getUuid())) {
                player.displayClientMessage(
                        Component.translatable(
                                        "message.otherworldinn.room_key.checkin_fail_id_mismatch")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            if (room.getCurrentGuests().size() >= room.getMaxGuests()) {
                player.displayClientMessage(
                        Component.translatable("message.otherworldinn.room_key.checkin_fail_full")
                                .withStyle(style -> style.withColor(ModColors.ERROR)),
                        true);
                return InteractionResult.FAIL;
            }

            if (innData.checkIn(guestData.getUuid(), roomId, serverPlayer.serverLevel())) {
                if (innData.getTotalCheckInCount() >= 1) {
                    AdvancementUtils.award(serverPlayer, AdvancementUtils.SERVE_FIRST_GUEST);
                }
                stack.shrink(1);

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
                RoomData room = team.getInnData().getRoomAt(pos);
                if (room != null) {
                    bindRoom(stack, room.getId(), room.getUuid());
                    if (player instanceof ServerPlayer serverPlayer) {
                        AdvancementUtils.award(
                                serverPlayer, AdvancementUtils.BIND_FIRST_ROOM_HIDDEN);
                    }
                    player.displayClientMessage(
                            Component.translatable(
                                            "message.otherworldinn.room_key.bound",
                                            RoomData.getDisplayName(room))
                                    .withStyle(style -> style.withColor(ModColors.SUCCESS)),
                            true);

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
            TeamData clientTeam = TeamManager.getInstance().getClientPlayerTeam();
            String roomDisplay = null;
            if (clientTeam != null) {
                RoomData room = clientTeam.getInnData().getRoom(id);
                if (room != null) {
                    roomDisplay = RoomData.getDisplayName(room);
                }
            }
            if (roomDisplay == null) {
                roomDisplay = id + "";
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
                            TeamData team = TeamManager.getInstance().getClientPlayerTeam();
                            if (team != null) {
                                RoomData room = team.getInnData().getRoom(roomId);
                                if (room != null) {
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.room_id",
                                                            roomId)
                                                    .withStyle(ChatFormatting.GOLD));

                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.pos",
                                                            room.getMinPos().toShortString(),
                                                            room.getMaxPos().toShortString())
                                                    .withStyle(ChatFormatting.GRAY));

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

                                    int maxGuests = room.getMaxGuests();
                                    int currentGuests = room.getCurrentGuests().size();
                                    tooltipComponents.add(
                                            Component.translatable(
                                                            "tooltip.otherworldinn.room_key.beds",
                                                            currentGuests,
                                                            maxGuests)
                                                    .withStyle(ChatFormatting.BLUE));

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
