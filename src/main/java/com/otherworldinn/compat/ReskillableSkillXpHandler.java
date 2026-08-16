package com.otherworldinn.compat;

import com.otherworldinn.OtherworldInn;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class ReskillableSkillXpHandler {
    private static final int BUILDING_XP_NUMERATOR = 1;
    private static final int BUILDING_XP_DENOMINATOR = 50;
    private static final int MINING_XP_NUMERATOR = 1;
    private static final int MINING_XP_DENOMINATOR = 50;
    private static final int FARMING_XP_PER_CROP_ACTION = 2;
    private static final double AGILITY_DISTANCE_PER_XP = 6.0D;
    private static final int ORE_BONUS_XP_TIER_1 = 1;
    private static final int ORE_BONUS_XP_TIER_2 = 2;
    private static final int ORE_BONUS_XP_TIER_3 = 3;
    private static final int ORE_BONUS_XP_TIER_4 = 4;
    private static final int ORE_BONUS_XP_TIER_5 = 5;
    private static final int ORE_BONUS_XP_TIER_6 = 6;
    private static final Map<UUID, Integer> LAST_SPRINT_STATS = new ConcurrentHashMap<>();
    private static final Map<UUID, Double> SPRINT_DISTANCE_PROGRESS = new ConcurrentHashMap<>();

    private ReskillableSkillXpHandler() {}

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!ReskillableCompat.isLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockState placed = event.getState();
        if (isMatureCrop(placed)) {
            ReskillableCompat.addSkillExperience(player, "farming", FARMING_XP_PER_CROP_ACTION);
            return;
        }
        awardScaledExperience(
                player, "building", BUILDING_XP_NUMERATOR, BUILDING_XP_DENOMINATOR);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!ReskillableCompat.isLoaded() || event.isCanceled()) {
            return;
        }
        Player rawPlayer = event.getPlayer();
        if (!(rawPlayer instanceof ServerPlayer player)) {
            return;
        }
        BlockState broken = event.getState();
        if (isMatureCrop(broken)) {
            ReskillableCompat.addSkillExperience(player, "farming", FARMING_XP_PER_CROP_ACTION);
            return;
        }
        int oreBonus = getOreBonusMiningXp(broken);
        if (oreBonus > 0) {
            ReskillableCompat.addSkillExperience(player, "mining", oreBonus);
        }
        awardScaledExperience(player, "mining", MINING_XP_NUMERATOR, MINING_XP_DENOMINATOR);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (!ReskillableCompat.isLoaded()) {
            return;
        }
        float amount = event.getNewDamage();
        if (amount <= 0.0F) {
            return;
        }
        float cappedAmount = Math.min(amount, 20.0F);
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            int attackXp = Math.max(1, Mth.floor(cappedAmount));
            ReskillableCompat.addSkillExperience(attacker, "attack", attackXp);
        }
        if (event.getEntity() instanceof ServerPlayer defender) {
            int defenseXp = Math.max(1, Mth.floor(cappedAmount));
            ReskillableCompat.addSkillExperience(defender, "defense", defenseXp);
        }
    }

    @SubscribeEvent
    public static void onPlayerXpChange(PlayerXpEvent.XpChange event) {
        if (!ReskillableCompat.isLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        int amount = event.getAmount();
        if (amount <= 0) {
            return;
        }
        ReskillableCompat.addSkillExperience(player, "gathering", amount);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ReskillableCompat.isLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        UUID playerId = player.getUUID();
        int currentSprintCm = player.getStats().getValue(Stats.CUSTOM, Stats.SPRINT_ONE_CM);
        Integer lastSprintCm = LAST_SPRINT_STATS.put(playerId, currentSprintCm);
        if (lastSprintCm == null) {
            return;
        }
        int deltaCm = currentSprintCm - lastSprintCm;
        if (deltaCm <= 0) {
            return;
        }
        double moved = deltaCm / 100.0D;
        double progressed = SPRINT_DISTANCE_PROGRESS.getOrDefault(playerId, 0.0D) + moved;
        int gained = (int) (progressed / AGILITY_DISTANCE_PER_XP);
        double remain = progressed - gained * AGILITY_DISTANCE_PER_XP;
        if (remain <= 0.000001D) {
            SPRINT_DISTANCE_PROGRESS.remove(playerId);
        } else {
            SPRINT_DISTANCE_PROGRESS.put(playerId, remain);
        }
        if (gained > 0) {
            ReskillableCompat.addSkillExperience(player, "agility", gained);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUUID();
        LAST_SPRINT_STATS.remove(playerId);
        SPRINT_DISTANCE_PROGRESS.remove(playerId);
    }

    /**
     * Reskillable 的生命加成是以瞬态属性修饰符在登录/重生后重挂载的，
     * 而 Minecraft 在实体反序列化时会先按基础上限(20)钳制血量。
     * 这里用 LOW 优先级保证在 Reskillable 挂载完修饰符后再补满，避免出现 20/25 不满血。
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ReskillableCompat.isLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.setHealth(player.getMaxHealth());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!ReskillableCompat.isLoaded()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        player.setHealth(player.getMaxHealth());
    }

    private static boolean isMatureCrop(BlockState state) {
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.is(BlockTags.CROPS)) {
            for (net.minecraft.world.level.block.state.properties.Property<?> prop : state.getProperties()) {
                if (prop.getName().equals("age") && prop instanceof net.minecraft.world.level.block.state.properties.IntegerProperty ageProp) {
                    int maxAge = ageProp.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(0);
                    return maxAge > 0 && state.getValue(ageProp) == maxAge;
                }
            }
        }
        return false;
    }

    private static int getOreBonusMiningXp(BlockState state) {
        if (state.is(BlockTags.COAL_ORES) || state.is(BlockTags.COPPER_ORES)) {
            return ORE_BONUS_XP_TIER_1;
        }
        if (state.is(BlockTags.IRON_ORES) || state.is(Blocks.NETHER_QUARTZ_ORE)) {
            return ORE_BONUS_XP_TIER_2;
        }
        if (state.is(BlockTags.LAPIS_ORES) || state.is(BlockTags.REDSTONE_ORES)) {
            return ORE_BONUS_XP_TIER_3;
        }
        if (state.is(BlockTags.GOLD_ORES)) {
            return ORE_BONUS_XP_TIER_4;
        }
        if (state.is(BlockTags.DIAMOND_ORES) || state.is(BlockTags.EMERALD_ORES)) {
            return ORE_BONUS_XP_TIER_5;
        }
        if (state.is(Blocks.ANCIENT_DEBRIS)) {
            return ORE_BONUS_XP_TIER_6;
        }
        return 0;
    }

    private static void awardScaledExperience(
            ServerPlayer player, String skillId, int numerator, int denominator) {
        if (numerator <= 0 || denominator <= 0) {
            return;
        }
        int guaranteed = numerator / denominator;
        int remainder = numerator % denominator;
        int total = guaranteed;
        if (remainder > 0 && player.getRandom().nextInt(denominator) < remainder) {
            total += 1;
        }
        if (total > 0) {
            ReskillableCompat.addSkillExperience(player, skillId, total);
        }
    }

}
