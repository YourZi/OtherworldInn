package com.otherworldinn.network.packet;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.foundation.MapModeConstants;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 客户端 -> 服务端 地图模式同步包。
 *
 * <p>负责在服务端执行地图模式相关的玩家传送与可见状态切换。
 */
@EventBusSubscriber(modid = OtherworldInn.MODID)
public record C2SMapModeSyncPacket(int action, double x, double y, double z, float yaw, float pitch)
        implements CustomPacketPayload {
    private static final double MAP_MODE_MAX_HEIGHT_DRIFT = 0.05D;
    private static final double MAP_MODE_MAX_HORIZONTAL_DRIFT_SQR = 0.25D;

    public static final int ACTION_ENTER = 0;
    public static final int ACTION_MOVE = 1;
    public static final int ACTION_EXIT = 2;
    public static final int ACTION_EXIT_KEEP_POSITION = 3;

    public static final Type<C2SMapModeSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "map_mode_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SMapModeSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    C2SMapModeSyncPacket::action,
                    ByteBufCodecs.DOUBLE,
                    C2SMapModeSyncPacket::x,
                    ByteBufCodecs.DOUBLE,
                    C2SMapModeSyncPacket::y,
                    ByteBufCodecs.DOUBLE,
                    C2SMapModeSyncPacket::z,
                    ByteBufCodecs.FLOAT,
                    C2SMapModeSyncPacket::yaw,
                    ByteBufCodecs.FLOAT,
                    C2SMapModeSyncPacket::pitch,
                    C2SMapModeSyncPacket::new);

    private static final Map<UUID, PlayerMapModeState> ACTIVE_STATES = new ConcurrentHashMap<>();

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SMapModeSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(
                () -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        return;
                    }

                    switch (packet.action()) {
                        case ACTION_ENTER -> enterMapMode(player, packet);
                        case ACTION_MOVE -> moveInMapMode(player, packet);
                        case ACTION_EXIT -> exitMapMode(player, true);
                        case ACTION_EXIT_KEEP_POSITION -> exitMapMode(player, false);
                        default -> {}
                    }
                });
    }

    private static void enterMapMode(ServerPlayer player, C2SMapModeSyncPacket packet) {
        PlayerMapModeState state = ACTIVE_STATES.computeIfAbsent(player.getUUID(), id -> captureState(player));
        state.updateTarget(packet);
        applyHiddenAppearance(player);
        applyMapHoverState(player);
        teleportTo(player, packet);
    }

    private static void moveInMapMode(ServerPlayer player, C2SMapModeSyncPacket packet) {
        PlayerMapModeState state = ACTIVE_STATES.get(player.getUUID());
        if (state == null) {
            state = captureState(player);
            ACTIVE_STATES.put(player.getUUID(), state);
            applyHiddenAppearance(player);
        }
        state.updateTarget(packet);
        applyMapHoverState(player);
        teleportTo(player, packet);
    }

    private static void exitMapMode(ServerPlayer player, boolean restorePosition) {
        PlayerMapModeState state = ACTIVE_STATES.remove(player.getUUID());
        if (state == null) {
            return;
        }

        restoreAppearance(player, state);
        restoreMapHoverState(player, state);
        if (restorePosition) {
            restorePosition(player, state);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerMapModeState state = ACTIVE_STATES.remove(player.getUUID());
            if (state != null) {
                restoreAppearance(player, state);
                restoreMapHoverState(player, state);
                restorePosition(player, state);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerMapModeState state = ACTIVE_STATES.remove(player.getUUID());
            if (state != null) {
                restoreAppearance(player, state);
                restoreMapHoverState(player, state);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // 维度切换兜底：强制退出地图模式，但保留当前新维度位置
            exitMapMode(player, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        PlayerMapModeState state = ACTIVE_STATES.get(player.getUUID());
        if (state == null) {
            return;
        }

        maintainMapHover(player, state);
    }

    private static PlayerMapModeState captureState(ServerPlayer player) {
        return new PlayerMapModeState(
                player.serverLevel().dimension(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot(),
                player.isInvisible(),
                player.isInvulnerable(),
                player.getTags().contains(MapModeConstants.HIDDEN_TAG),
                player.isNoGravity());
    }

    private static void applyHiddenAppearance(ServerPlayer player) {
        player.addTag(MapModeConstants.HIDDEN_TAG);
        player.setInvisible(true);
        player.setInvulnerable(true);
    }

    private static void restoreAppearance(ServerPlayer player, PlayerMapModeState state) {
        if (state.wasMapModeHidden) {
            player.addTag(MapModeConstants.HIDDEN_TAG);
        } else {
            player.removeTag(MapModeConstants.HIDDEN_TAG);
        }
        player.setInvisible(state.wasInvisible);
        player.setInvulnerable(state.wasInvulnerable);
    }

    private static void applyMapHoverState(ServerPlayer player) {
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static void restoreMapHoverState(ServerPlayer player, PlayerMapModeState state) {
        player.setNoGravity(state.wasNoGravity);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
    }

    private static void maintainMapHover(ServerPlayer player, PlayerMapModeState state) {
        applyMapHoverState(player);

        double horizontalDriftSqr =
                (player.getX() - state.targetX) * (player.getX() - state.targetX)
                        + (player.getZ() - state.targetZ) * (player.getZ() - state.targetZ);
        double heightDrift = Math.abs(player.getY() - state.targetY);
        if (heightDrift <= MAP_MODE_MAX_HEIGHT_DRIFT
                && horizontalDriftSqr <= MAP_MODE_MAX_HORIZONTAL_DRIFT_SQR) {
            return;
        }

        player.teleportTo(
                player.serverLevel(),
                state.targetX,
                state.targetY,
                state.targetZ,
                state.targetYaw,
                state.targetPitch);
        applyMapHoverState(player);
    }

    private static void restorePosition(ServerPlayer player, PlayerMapModeState state) {
        ServerLevel level = player.server.getLevel(state.dimension);
        if (level == null) {
            level = player.serverLevel();
        }

        player.teleportTo(level, state.x, state.y, state.z, state.yaw, state.pitch);
    }

    private static void teleportTo(ServerPlayer player, C2SMapModeSyncPacket packet) {
        player.teleportTo(player.serverLevel(), packet.x(), packet.y(), packet.z(), packet.yaw(), packet.pitch());
    }

    private static final class PlayerMapModeState {
        private final ResourceKey<Level> dimension;
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;
        private final boolean wasInvisible;
        private final boolean wasInvulnerable;
        private final boolean wasMapModeHidden;
        private final boolean wasNoGravity;
        private double targetX;
        private double targetY;
        private double targetZ;
        private float targetYaw;
        private float targetPitch;

        private PlayerMapModeState(
                ResourceKey<Level> dimension,
                double x,
                double y,
                double z,
                float yaw,
                float pitch,
                boolean wasInvisible,
                boolean wasInvulnerable,
                boolean wasMapModeHidden,
                boolean wasNoGravity) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.wasInvisible = wasInvisible;
            this.wasInvulnerable = wasInvulnerable;
            this.wasMapModeHidden = wasMapModeHidden;
            this.wasNoGravity = wasNoGravity;
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
            this.targetYaw = yaw;
            this.targetPitch = pitch;
        }

        private void updateTarget(C2SMapModeSyncPacket packet) {
            this.targetX = packet.x();
            this.targetY = packet.y();
            this.targetZ = packet.z();
            this.targetYaw = packet.yaw();
            this.targetPitch = packet.pitch();
        }
    }
}
