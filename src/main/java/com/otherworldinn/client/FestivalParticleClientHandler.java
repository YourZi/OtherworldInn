package com.otherworldinn.client;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.api.OtherworldInnHudSnapshotApi;
import com.otherworldinn.world.dimension.TownDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public final class FestivalParticleClientHandler {
    private static final String SPRING_FESTIVAL_KEY = "festival.otherworldinn.spring_festival";
    private static final String DEEP_WINTER_FESTIVAL_KEY = "festival.otherworldinn.deep_winter_festival";

    private static final int BURST_INTERVAL_TICKS = 4;
    private static final int SPRING_PARTICLES_PER_BURST = 8;
    private static final int DEEP_WINTER_PARTICLES_PER_BURST = 12;
    private static final double HORIZONTAL_RADIUS = 28.0D;
    private static final double MIN_HEIGHT_OFFSET = 4.0D;
    private static final double HEIGHT_RANGE = 12.0D;

    private FestivalParticleClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null || minecraft.isPaused()) {
            return;
        }
        if (level.dimension() != TownDimensions.TOWN_LEVEL) {
            return;
        }
        if (player.tickCount % BURST_INTERVAL_TICKS != 0) {
            return;
        }

        FestivalParticleConfig config = resolveFestivalParticle();
        if (config == null) {
            return;
        }

        for (int i = 0; i < config.particlesPerBurst(); i++) {
            spawnAmbientParticle(level, player, config.particle());
        }
    }

    private static FestivalParticleConfig resolveFestivalParticle() {
        CompoundTag festivalTag = OtherworldInnHudSnapshotApi.getTaskHudSnapshot().getCompound("Festival");
        if (!festivalTag.getBoolean("Active")) {
            return null;
        }

        String nameKey = festivalTag.getString("NameKey");
        if (SPRING_FESTIVAL_KEY.equals(nameKey)) {
            return new FestivalParticleConfig(ParticleTypes.CHERRY_LEAVES, SPRING_PARTICLES_PER_BURST);
        }
        if (DEEP_WINTER_FESTIVAL_KEY.equals(nameKey)) {
            return new FestivalParticleConfig(ParticleTypes.SNOWFLAKE, DEEP_WINTER_PARTICLES_PER_BURST);
        }
        return null;
    }

    private static void spawnAmbientParticle(ClientLevel level, Player player, ParticleOptions particle) {
        double x = player.getX() + randomOffset(level, HORIZONTAL_RADIUS);
        double y = player.getY() + MIN_HEIGHT_OFFSET + level.random.nextDouble() * HEIGHT_RANGE;
        double z = player.getZ() + randomOffset(level, HORIZONTAL_RADIUS);

        double xSpeed;
        double ySpeed;
        double zSpeed;
        if (particle == ParticleTypes.CHERRY_LEAVES) {
            xSpeed = randomOffset(level, 0.015D);
            ySpeed = -0.012D - level.random.nextDouble() * 0.01D;
            zSpeed = randomOffset(level, 0.015D);
        } else {
            xSpeed = randomOffset(level, 0.006D);
            ySpeed = -0.03D - level.random.nextDouble() * 0.015D;
            zSpeed = randomOffset(level, 0.006D);
        }

        level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    private static double randomOffset(ClientLevel level, double radius) {
        return (level.random.nextDouble() - 0.5D) * radius * 2.0D;
    }

    private record FestivalParticleConfig(ParticleOptions particle, int particlesPerBurst) {}
}
