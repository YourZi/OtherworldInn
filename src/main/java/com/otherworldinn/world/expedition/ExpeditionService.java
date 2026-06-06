package com.otherworldinn.world.expedition;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.init.ModItems;
import com.otherworldinn.mixin.MixinMinecraftServerLevelsAccessor;
import com.otherworldinn.world.dimension.TownDimensions;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import sun.misc.Unsafe;

@EventBusSubscriber(modid = OtherworldInn.MODID)
public final class ExpeditionService {

    private static final Map<ResourceKey<Level>, ExpeditionSession> ACTIVE_EXPEDITIONS =
            new ConcurrentHashMap<>();
    private static final Map<UUID, ResourceKey<Level>> PLAYER_EXPEDITION_MAP =
            new ConcurrentHashMap<>();

    private static final Map<ResourceKey<Level>, Set<String>> DIM_COMPONENT_FLAGS =
            new ConcurrentHashMap<>();

    private static volatile ExpeditionSession activeSession;
    private static volatile boolean structureBoostActive;

    private static final Unsafe UNSAFE;
    private static final long CM_RANDOM_STATE_OFFSET;
    private static final long CM_STRUCTURE_STATE_OFFSET;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (Unsafe) f.get(null);

            Class<?> cmClass = Class.forName("net.minecraft.server.level.ChunkMap");
            CM_RANDOM_STATE_OFFSET = UNSAFE.objectFieldOffset(
                    cmClass.getDeclaredField("randomState"));
            CM_STRUCTURE_STATE_OFFSET = UNSAFE.objectFieldOffset(
                    cmClass.getDeclaredField("chunkGeneratorState"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to init ExpeditionService offsets", e);
        }
    }

    private ExpeditionService() {}

    public static void setStructureBoostActive(boolean active) {
        structureBoostActive = active;
    }

    public static boolean isStructureBoostActive() {
        return structureBoostActive;
    }

    public static boolean hasDimComponent(ResourceKey<Level> dimKey, String componentId) {
        Set<String> flags = DIM_COMPONENT_FLAGS.get(dimKey);
        return flags != null && flags.contains(componentId);
    }

    private static void setDimComponents(ResourceKey<Level> dimKey, List<String> comps) {
        DIM_COMPONENT_FLAGS.put(dimKey, Set.copyOf(comps));
    }

    private static void clearDimComponents(ResourceKey<Level> dimKey) {
        DIM_COMPONENT_FLAGS.remove(dimKey);
    }

    public static boolean registerSession(ExpeditionSession session) {
        if (activeSession != null) return false;
        ACTIVE_EXPEDITIONS.put(session.dimensionKey(), session);
        activeSession = session;
        for (UUID playerId : session.activePlayers()) {
            PLAYER_EXPEDITION_MAP.put(playerId, session.dimensionKey());
        }
        return true;
    }

    public static ExpeditionSession getSession(ResourceKey<Level> key) {
        return ACTIVE_EXPEDITIONS.get(key);
    }

    public static ExpeditionSession getPlayerSession(UUID playerId) {
        ResourceKey<Level> key = PLAYER_EXPEDITION_MAP.get(playerId);
        return key != null ? ACTIVE_EXPEDITIONS.get(key) : null;
    }

    public static boolean canEnter(UUID playerId, ResourceKey<Level> dimensionKey) {
        ExpeditionSession session = getPlayerSession(playerId);
        return session != null && session.canEnter(playerId);
    }

    public static void markDeparted(UUID playerId, MinecraftServer server) {
        ExpeditionSession session = getPlayerSession(playerId);
        if (session != null) {
            session.markDeparted(playerId);
        }
    }

    public static void markDeparted(UUID playerId) {
        markDeparted(playerId, null);
    }

    public static ExpeditionSession getActiveSession() {
        return activeSession;
    }

    public static void forceAbort(MinecraftServer server) {
        ExpeditionSession session = activeSession;
        if (session == null) return;
        ResourceKey<Level> dimKey = session.dimensionKey();
        abortSingleExpedition(server, dimKey, session);
    }

    public static void cleanupAllExpeditionLevels(MinecraftServer server) {
        for (var entry : new ArrayList<>(ACTIVE_EXPEDITIONS.entrySet())) {
            abortSingleExpedition(server, entry.getKey(), entry.getValue());
        }
        ACTIVE_EXPEDITIONS.clear();
        PLAYER_EXPEDITION_MAP.clear();
        activeSession = null;
    }

    private static void abortSingleExpedition(MinecraftServer server,
            ResourceKey<Level> dimKey, ExpeditionSession session) {
        ServerLevel level = server.getLevel(dimKey);
        if (level != null) {
            for (ServerPlayer player : List.copyOf(level.players())) {
                clearRecallScrolls(player);
                recallPlayer(player, server);
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.otherworldinn.expedition.aborted")
                                .withStyle(net.minecraft.ChatFormatting.RED),
                        false);
            }
            clearLevelEntities(level, session);
        }
        Map<ResourceKey<Level>, ServerLevel> levels =
                ((MixinMinecraftServerLevelsAccessor) server).otherworldinn$getLevels();
        levels.remove(dimKey);
        clearDimComponents(dimKey);
        ACTIVE_EXPEDITIONS.remove(dimKey);
        for (UUID playerId : session.activePlayers()) {
            PLAYER_EXPEDITION_MAP.remove(playerId, dimKey);
        }
        if (session == activeSession) {
            activeSession = null;
        }
        restoreTemplateDimension(server);
    }

    public static ServerLevel ensureExpeditionLevel(MinecraftServer server,
            ResourceKey<Level> dimKey, List<String> componentIds, long seed,
            ChartComponentType.DimensionCategory chartDimension, ExpeditionSession session) {

        Map<ResourceKey<Level>, ServerLevel> levels =
                ((MixinMinecraftServerLevelsAccessor) server).otherworldinn$getLevels();

        ServerLevel existing = levels.get(dimKey);
        if (existing != null) return existing;

        ChartComponentType.DimensionCategory category =
                ChartComponentType.determineDimension(componentIds);
        if (category == null) category = chartDimension != null ? chartDimension : ChartComponentType.DimensionCategory.MAIN_WORLD;

        ResourceKey<Level> templateKey = getTemplateKey(category);
        ServerLevel template = server.getLevel(templateKey);
        if (template == null) {
            OtherworldInn.LOGGER.error("Expedition template dimension not loaded! category={}", category);
            return null;
        }

        ChunkGenerator oldGen = template.getChunkSource().getGenerator();
        if (!(oldGen instanceof ExpeditionChunkGenerator expGen)) {
            OtherworldInn.LOGGER.error("Template dimension {} does not use ExpeditionChunkGenerator", templateKey.location());
            return null;
        }

        BlockState stoneReplacement = resolveStoneType(componentIds);

        NoiseBasedChunkGenerator customGen =
                ExpeditionBiomeFactory.createNoiseGenerator(server, componentIds, category, stoneReplacement);
        if (customGen == null) {
            OtherworldInn.LOGGER.error("Failed to create noise generator for expedition");
            return null;
        }

        expGen.setComponentIds(componentIds);
        expGen.setDelegate(customGen);
        expGen.setStructureBoost(
                ExpeditionBiomeFactory.hasStructureBoost(componentIds));
        expGen.setLavaFlood(componentIds.contains("lava_flood"));
        expGen.setDryLand(componentIds.contains("dry_land"));
        expGen.setWaterWorld(componentIds.contains("water_world"));

        ExpeditionSavedData savedData = ExpeditionSavedData.get(server);
        int nextGrid = savedData.incrementAndGet();
        int gridX = nextGrid % 100;
        int gridZ = nextGrid / 100;

        int offsetX = gridX * 2048;
        int offsetZ = gridZ * 2048;
        session.setCenter(offsetX, offsetZ);

        updateChunkSourceState(server, template, templateKey, expGen, seed);

        template.getWorldBorder().setSize(1024.0);
        template.getWorldBorder().setCenter(offsetX, offsetZ);

        boolean needsBoost = ExpeditionBiomeFactory.hasStructureBoost(componentIds);
        if (needsBoost) {
            setStructureBoostActive(true);
        }

        OtherworldInn.LOGGER.info("Expedition level configured: {}",
                ExpeditionBiomeFactory.buildInfoLog(componentIds, category));

        setDimComponents(dimKey, componentIds);

        return template;
    }

    private static BlockState resolveStoneType(List<String> componentIds) {
        for (String id : componentIds) {
            Block block = switch (id) {
                case "deepslate_base" -> Blocks.DEEPSLATE;
                case "granite_base" -> Blocks.GRANITE;
                case "andesite_base" -> Blocks.ANDESITE;
                case "diorite_base" -> Blocks.DIORITE;
                case "sandstone_base" -> Blocks.SANDSTONE;
                case "tuff_base" -> Blocks.TUFF;
                default -> null;
            };
            if (block != null) return block.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    private static void updateChunkSourceState(MinecraftServer server, ServerLevel level,
            ResourceKey<Level> templateKey, ExpeditionChunkGenerator expGen, long seed) {
        try {
            ServerChunkCache cache = level.getChunkSource();
            Object chunkMap = cache.chunkMap;

            NoiseBasedChunkGenerator delegateGen = expGen.getDelegate();
            if (delegateGen == null) {
                OtherworldInn.LOGGER.error("ExpeditionChunkGenerator has no delegate");
                return;
            }

            NoiseGeneratorSettings settings = delegateGen.generatorSettings().value();
            HolderGetter<NormalNoise.NoiseParameters> noiseParams =
                    level.registryAccess().lookupOrThrow(Registries.NOISE);
            RandomState correctState = RandomState.create(settings, noiseParams, seed);

            ChunkGeneratorStructureState correctStructState =
                    delegateGen.createState(
                            level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET),
                            correctState, seed);

            UNSAFE.putObject(chunkMap, CM_RANDOM_STATE_OFFSET, correctState);
            UNSAFE.putObject(chunkMap, CM_STRUCTURE_STATE_OFFSET, correctStructState);

            OtherworldInn.LOGGER.info("Updated chunk map state for expedition: biomeSource={}",
                    delegateGen.getBiomeSource().getClass().getSimpleName());
        } catch (Exception e) {
            OtherworldInn.LOGGER.error("Failed to update chunk map state", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T getPrivate(Object target, String name) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return (T) f.get(target);
        } catch (Exception e) {
            return null;
        }
    }

    private static ResourceKey<Level> getTemplateKey(ChartComponentType.DimensionCategory category) {
        return switch (category) {
            case NETHER -> TownDimensions.EXPEDITION_TEMPLATE_NETHER;
            case END -> TownDimensions.EXPEDITION_TEMPLATE_END;
            default -> TownDimensions.EXPEDITION_TEMPLATE_LEVEL;
        };
    }

    public static void registerLevel(MinecraftServer server, ResourceKey<Level> dimKey,
            ServerLevel level, ExpeditionSession session) {
        Map<ResourceKey<Level>, ServerLevel> levels =
                ((MixinMinecraftServerLevelsAccessor) server).otherworldinn$getLevels();
        levels.put(dimKey, level);
        level.getWorldBorder().setSize(1024.0);
        level.getWorldBorder().setCenter(session.getCenterX(), session.getCenterZ());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (ACTIVE_EXPEDITIONS.isEmpty()) return;

        MinecraftServer server = event.getServer();
        long currentTick = server.getTickCount();

        for (var entry : new ArrayList<>(ACTIVE_EXPEDITIONS.entrySet())) {
            ExpeditionSession session = entry.getValue();
            ResourceKey<Level> dimKey = session.dimensionKey();
            ServerLevel level = server.getLevel(dimKey);

            boolean hasPlayers = level != null && level.players().stream()
                    .anyMatch(p -> !session.departedPlayers().contains(p.getUUID()));

            boolean expired = session.isExpired(currentTick);

            if (!hasPlayers || expired) {
                if (expired && level != null) {
                    for (ServerPlayer player : List.copyOf(level.players())) {
                        clearRecallScrolls(player);
                        recallPlayer(player, server);
                    }
                }
                if (level != null) {
                    clearLevelEntities(level, session);
                }
                ACTIVE_EXPEDITIONS.remove(dimKey);
                for (UUID playerId : session.activePlayers()) {
                    PLAYER_EXPEDITION_MAP.remove(playerId, dimKey);
                }
                if (session == activeSession) {
                    activeSession = null;
                }
                Map<ResourceKey<Level>, ServerLevel> levels =
                        ((MixinMinecraftServerLevelsAccessor) server).otherworldinn$getLevels();
                levels.remove(dimKey);
                clearDimComponents(dimKey);
                restoreTemplateDimension(server);
            } else {
                tickComponentEffects(server, session, level);
            }
        }
    }

    private static void tickComponentEffects(MinecraftServer server,
            ExpeditionSession session, ServerLevel level) {
        if (level == null) return;
        if (level.players().isEmpty()) return;

        List<String> componentIds = session.componentIds();

        if (server.getTickCount() % 20 != 0) return;

        boolean thunderstorm = componentIds.contains("thunderstorm");
        boolean eternalDay = componentIds.contains("eternal_day");
        boolean eternalNight = componentIds.contains("eternal_night");
        boolean eternalRain = componentIds.contains("eternal_rain");
        boolean gravityLow = componentIds.contains("gravity_low");
        boolean lavaFlood = componentIds.contains("lava_flood");
        boolean oneHp = componentIds.contains("one_hp");
        boolean dryLand = componentIds.contains("dry_land");
        boolean waterWorld = componentIds.contains("water_world");
        boolean universalAnger = componentIds.contains("universal_anger");
        boolean insomniacs = componentIds.contains("insomniacs");
        boolean fishOutOfWater = componentIds.contains("fish_out_of_water");

        if (thunderstorm) {
            if (!level.isThundering()) {
                level.setWeatherParameters(0, Integer.MAX_VALUE, true, true);
            }
        } else if (eternalDay) {
            if (level.isRaining() || level.isThundering()) {
                level.setWeatherParameters(Integer.MAX_VALUE, 0, false, false);
            }
            level.setDayTime(6000);
        } else if (eternalNight) {
            if (level.isRaining() || level.isThundering()) {
                level.setWeatherParameters(Integer.MAX_VALUE, 0, false, false);
            }
            level.setDayTime(18000);
        } else if (eternalRain) {
            if (!level.isRaining()) {
                level.setWeatherParameters(0, Integer.MAX_VALUE, false, false);
            }
        }

        for (ServerPlayer player : level.players()) {
            if (thunderstorm) {
                applyPersistentEffect(player, MobEffects.REGENERATION, 0);
                applyPersistentEffect(player, MobEffects.DAMAGE_RESISTANCE, 0);
            }
            if (eternalNight) {
                applyPersistentEffect(player, MobEffects.NIGHT_VISION, 0);
            }
            if (eternalRain) {
                applyPersistentEffect(player, MobEffects.MOVEMENT_SPEED, 0);
                applyPersistentEffect(player, MobEffects.SATURATION, 0);
            }
            if (gravityLow) {
                applyPersistentEffect(player, MobEffects.JUMP, 1);
                applyPersistentEffect(player, MobEffects.SLOW_FALLING, 0);
            }
            if (lavaFlood) {
                applyPersistentEffect(player, MobEffects.FIRE_RESISTANCE, 0);
            }
            if (oneHp) {
                applyPersistentEffect(player, MobEffects.DAMAGE_RESISTANCE, 2);
                applyPersistentEffect(player, MobEffects.REGENERATION, 1);
                player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                        .setBaseValue(2.0);
            }
            if (dryLand) {
                applyPersistentEffect(player, MobEffects.DIG_SPEED, 0);
            }
            if (waterWorld) {
                applyPersistentEffect(player, MobEffects.WATER_BREATHING, 0);
                applyPersistentEffect(player, MobEffects.DOLPHINS_GRACE, 0);
            }
            if (universalAnger) {
                applyPersistentEffect(player, MobEffects.DAMAGE_BOOST, 0);
            }
            if (insomniacs) {
                applyPersistentEffect(player, MobEffects.SLOW_FALLING, 0);
                applyPersistentEffect(player, MobEffects.NIGHT_VISION, 0);
                player.getStats().setValue(player,
                        net.minecraft.stats.Stats.CUSTOM.get(
                                net.minecraft.stats.Stats.TIME_SINCE_REST),
                        72000);
            }
            if (fishOutOfWater) {
                applyPersistentEffect(player, MobEffects.DOLPHINS_GRACE, 1);
                // 只能在水里呼吸：不在水中时逐渐减少空气值，归零后造成溺水伤害
                if (!player.isInWaterOrBubble()) {
                    int air = player.getAirSupply();
                    if (air > -20) {
                        player.setAirSupply(Math.max(air - 20, -20));
                    } else {
                        player.setAirSupply(0);
                        player.hurt(player.damageSources().drown(), 2.0f);
                    }
                } else {
                    player.setAirSupply(player.getMaxAirSupply());
                }
            }
        }
    }

    private static void applyPersistentEffect(ServerPlayer player,
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect,
            int amplifier) {
        if (!player.hasEffect(effect)
                || player.getEffect(effect).getDuration() < 40) {
            player.addEffect(new MobEffectInstance(
                    effect, 100, amplifier,
                    false, false, true));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ExpeditionSession session = getPlayerSession(player.getUUID());
        if (session == null) return;
        ResourceKey<Level> dimKey = session.dimensionKey();
        if (!player.level().dimension().equals(dimKey)) return;

        markDeparted(player.getUUID());
        clearRecallScrolls(player);
        MinecraftServer server = player.getServer();
        if (server != null) {
            recallPlayer(player, server);
        }
    }

    private static void recallPlayer(ServerPlayer player, MinecraftServer server) {
        ServerLevel townLevel = server.getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel == null) return;
        player.teleportTo(townLevel, 10.5, 71.0, 0.5, player.getYRot(), player.getXRot());
    }

    private static void clearRecallScrolls(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.RECALL_SCROLL.get())) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static void restoreTemplateDimension(MinecraftServer server) {
        setStructureBoostActive(false);
        restoreSingleTemplate(server, TownDimensions.EXPEDITION_TEMPLATE_LEVEL);
        restoreSingleTemplate(server, TownDimensions.EXPEDITION_TEMPLATE_NETHER);
        restoreSingleTemplate(server, TownDimensions.EXPEDITION_TEMPLATE_END);
    }

    private static void restoreSingleTemplate(MinecraftServer server, ResourceKey<Level> key) {
        ServerLevel template = server.getLevel(key);
        if (template == null) return;

        ChunkGenerator gen = template.getChunkSource().getGenerator();
        if (gen instanceof ExpeditionChunkGenerator expGen) {
            expGen.setDelegate(null);
            expGen.componentIds().clear();
            expGen.setStructureBoost(false);
            expGen.setLavaFlood(false);
            expGen.setDryLand(false);
            expGen.setWaterWorld(false);
        }

        template.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)
                .set(server.getGameRules().getBoolean(
                        net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY), server);

        template.getWorldBorder().setSize(512.0);
        template.getWorldBorder().setCenter(0.0, 0.0);
    }

    private static void clearLevelEntities(ServerLevel level, ExpeditionSession session) {
        try {
            var snapshot = new ArrayList<net.minecraft.world.entity.Entity>();
            int minX = session.getCenterX() - 1024;
            int maxX = session.getCenterX() + 1024;
            int minZ = session.getCenterZ() - 1024;
            int maxZ = session.getCenterZ() + 1024;

            level.getAllEntities().forEach(entity -> {
                if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
                    if (entity.getX() >= minX && entity.getX() <= maxX &&
                        entity.getZ() >= minZ && entity.getZ() <= maxZ) {
                        snapshot.add(entity);
                    }
                }
            });
            for (var entity : snapshot) {
                entity.discard();
            }
        } catch (Exception ignored) {}
    }

    public static int activeExpeditionCount() {
        return ACTIVE_EXPEDITIONS.size();
    }
}
