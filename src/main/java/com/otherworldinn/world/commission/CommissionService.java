package com.otherworldinn.world.commission;

import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.foundation.ModColors;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CCommissionBoardPacket;
import com.otherworldinn.util.AdvancementUtils;
import com.otherworldinn.world.commission.CommissionRegistry.CommissionTemplate;
import com.otherworldinn.world.dimension.TownDimensions;
import com.otherworldinn.world.hud.TaskHudSnapshotSync;
import com.otherworldinn.world.inn.InnTodo;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CommissionService {
    private static final int BOARD_SIZE = 2;
    private static final long UNACCEPTED_REFRESH_INTERVAL_DAYS = 2L;
    private static final long COMPLETED_REFRESH_INTERVAL_DAYS = 1L;
    private static final long FULL_TEMPLATE_POOL_UNLOCK_DAY = 15L;
    private static final String TOWN_COMMISSION_TODO_TEXT_KEY =
            "todo.otherworldinn.town_commission_pending";

    private CommissionService() {}

    public static void openBoard(ServerPlayer player, BlockPos boardPos) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return;
        }
        ensureBoard(player.serverLevel(), team);
        ModMessages.sendToPlayer(
                new S2CCommissionBoardPacket(
                        createBoardViewTag(team, boardPos, currentDay(player.serverLevel())), true),
                player);
    }

    public static void acceptCommission(ServerPlayer player, int index) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return;
        }
        TeamCommissionData data = team.getCommissionData();
        ensureBoard(player.serverLevel(), team);
        if (data.hasAccepted() || index < 0 || index >= data.getBoardEntries().size()) {
            return;
        }
        CommissionEntry entry = data.getBoardEntries().get(index);
        long day = currentDay(player.serverLevel());
        data.setAcceptedIndex(index);
        data.setAcceptedDay(day);
        data.setExpireDay(day + entry.getDurationDays());
        data.setRewardClaimed(false);
        data.getKillProgress().clear();
        data.getPhotoProgress().clear();
        InnTodo todo = addTownCommissionTodo(player.serverLevel(), team);
        data.setAcceptedOrder(team.getInnData().getTodoAcceptedAt(todo));
        notifyTeamCommissionAccepted(player.serverLevel(), team, player, entry.getDurationDays());
        TeamManager.getInstance().syncTeam(team, player.getServer());
        broadcastBoard(team, player.serverLevel(), null);
        TaskHudSnapshotSync.syncTeam(team, player.serverLevel());
    }

    public static boolean handleBoardLeftClickSubmit(ServerPlayer player, BlockPos boardPos) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return true;
        }
        TeamCommissionData data = team.getCommissionData();
        if (!data.hasAccepted()) {
            return true;
        }
        CommissionEntry active = data.getAcceptedEntry();
        if (active == null || data.isRewardClaimed()) {
            return true;
        }
        if (!active.hasSubmitRequirement()) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.commission.submit_not_needed"), true);
            return true;
        }
        if (!isKillRequirementComplete(active, data.getKillProgress())) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.commission.submit_kill_unfinished"),
                    true);
            return true;
        }
        if (!isPhotoRequirementComplete(active, data.getPhotoProgress())) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.commission.submit_photo_unfinished"),
                    true);
            return true;
        }
        if (!hasRequiredItems(player, active.getSubmitRequirements())) {
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.commission.submit_items_missing"), true);
            return true;
        }
        consumeRequiredItems(player, active.getSubmitRequirements());
        completeActiveCommission(player, team);
        return true;
    }

    public static void onTeamMemberMobKilled(ServerPlayer killer, LivingEntity victim) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(killer);
        if (team == null) {
            return;
        }
        TeamCommissionData data = team.getCommissionData();
        if (!data.hasAccepted() || data.isRewardClaimed()) {
            return;
        }
        CommissionEntry active = data.getAcceptedEntry();
        if (active == null || !active.hasKillRequirement()) {
            return;
        }
        ResourceLocation victimTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());
        if (victimTypeId == null) {
            return;
        }
        boolean changed = false;
        String key = victimTypeId.toString();
        for (CommissionEntry.KillRequirement req : active.getKillRequirements()) {
            if (req.entityTypeId().equals(key)) {
                int current = data.getKillProgress().getOrDefault(key, 0);
                int next = Math.min(req.count(), current + 1);
                if (next != current) {
                    data.getKillProgress().put(key, next);
                    changed = true;
                }
            }
        }
        if (!changed) {
            return;
        }
        if (areAllRequirementsComplete(active, data) && !active.hasSubmitRequirement()) {
            completeActiveCommission(killer, team);
            return;
        }
        TeamManager.getInstance().syncTeam(team, killer.getServer());
        broadcastBoard(team, killer.serverLevel(), null);
        TaskHudSnapshotSync.syncTeam(team, killer.serverLevel());
    }

    public static void onTeamMemberPhotoObjectiveMatched(ServerPlayer player, ResourceLocation objectiveId) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return;
        }
        TeamCommissionData data = team.getCommissionData();
        if (!data.hasAccepted() || data.isRewardClaimed()) {
            return;
        }
        CommissionEntry active = data.getAcceptedEntry();
        if (active == null || !active.hasPhotoRequirement()) {
            return;
        }

        boolean changed = false;
        for (CommissionEntry.PhotoRequirement requirement : active.getPhotoRequirements()) {
            if (data.getPhotoProgress().contains(requirement.objectiveId())) {
                continue;
            }
            if (requirement.objectiveId().equals(objectiveId.toString())) {
                data.getPhotoProgress().add(requirement.objectiveId());
                changed = true;
                player.displayClientMessage(
                        Component.translatable(
                                "message.otherworldinn.commission.photo_recorded",
                                PhotoObjectiveRegistry.getDisplayName(objectiveId)),
                        true);
            }
        }
        if (!changed) {
            return;
        }
        if (areAllRequirementsComplete(active, data) && !active.hasSubmitRequirement()) {
            completeActiveCommission(player, team);
            return;
        }
        TeamManager.getInstance().syncTeam(team, player.getServer());
        broadcastBoard(team, player.serverLevel(), null);
        TaskHudSnapshotSync.syncTeam(team, player.serverLevel());
    }

    public static List<ResourceLocation> getActivePhotoObjectives(ServerPlayer player) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return List.of();
        }
        TeamCommissionData data = team.getCommissionData();
        if (!data.hasAccepted() || data.isRewardClaimed()) {
            return List.of();
        }
        CommissionEntry active = data.getAcceptedEntry();
        if (active == null || !active.hasPhotoRequirement()) {
            return List.of();
        }

        LinkedHashSet<ResourceLocation> objectiveIds = new LinkedHashSet<>();
        for (CommissionEntry.PhotoRequirement requirement : active.getPhotoRequirements()) {
            if (data.getPhotoProgress().contains(requirement.objectiveId())) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(requirement.objectiveId());
            if (id != null) {
                objectiveIds.add(id);
            }
        }
        return List.copyOf(objectiveIds);
    }

    public static boolean tick(ServerLevel level, TeamData team) {
        TeamCommissionData data = team.getCommissionData();
        long day = currentDay(level);
        boolean changed = false;
        if (data.getBoardEntries().size() != BOARD_SIZE) {
            refreshBoard(level, team, day);
            return true;
        }
        boolean expired = data.hasAccepted() && !data.isRewardClaimed() && day > data.getExpireDay();
        if (expired) {
            notifyTeamCommissionExpired(level, team);
            refreshBoard(level, team, day);
            changed = true;
        } else {
            boolean canAutoRefresh = !data.hasAccepted() || data.isRewardClaimed();
            if (canAutoRefresh && day >= data.getNextAutoRefreshDay()) {
                refreshBoard(level, team, day);
                changed = true;
            }
        }
        return changed;
    }

    public static void ensureBoard(ServerLevel level, TeamData team) {
        TeamCommissionData data = team.getCommissionData();
        if (data.getBoardEntries().size() != BOARD_SIZE) {
            refreshBoard(level, team, currentDay(level));
            TeamManager.getInstance().syncTeam(team, level.getServer());
        }
    }

    /** 管理员调试：直接完成当前队伍委托（按正常奖励流程发放） */
    public static boolean adminCompleteCurrentCommission(ServerPlayer rewardReceiver) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(rewardReceiver);
        if (team == null) {
            return false;
        }
        ensureBoard(rewardReceiver.serverLevel(), team);
        TeamCommissionData data = team.getCommissionData();
        if (!data.hasAccepted() || data.isRewardClaimed() || data.getAcceptedEntry() == null) {
            return false;
        }
        completeActiveCommission(rewardReceiver, team);
        return true;
    }

    /** 管理员调试：强制刷新当前队伍的委托板（会清空当前接取状态） */
    public static boolean adminRefreshBoard(ServerPlayer player) {
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        refreshBoard(level, team, currentDay(level));
        TeamManager.getInstance().syncTeam(team, player.getServer());
        broadcastBoard(team, level, null);
        TaskHudSnapshotSync.syncTeam(team, level);
        return true;
    }

    private static void completeActiveCommission(ServerPlayer triggerPlayer, TeamData team) {
        TeamCommissionData data = team.getCommissionData();
        CommissionEntry active = data.getAcceptedEntry();
        if (active == null || data.isRewardClaimed()) {
            return;
        }
        grantRewards(triggerPlayer, team, active);
        data.setCompletedCount(Math.max(0, data.getCompletedCount()) + 1);
        data.setRewardClaimed(true);
        data.setNextAutoRefreshDay(
                currentDay(triggerPlayer.serverLevel()) + COMPLETED_REFRESH_INTERVAL_DAYS);
        if (data.getCompletedCount() >= 1) {
            AdvancementUtils.award(triggerPlayer, AdvancementUtils.COMPLETE_1_COMMISSION);
        }
        if (data.getCompletedCount() >= 20) {
            AdvancementUtils.award(triggerPlayer, AdvancementUtils.COMPLETE_20_COMMISSIONS);
        }
        removeTownCommissionTodo(triggerPlayer.serverLevel(), team);
        notifyTeamCommissionCompleted(triggerPlayer.serverLevel(), team, active);
        TeamManager.getInstance().syncTeam(team, triggerPlayer.getServer());
        broadcastBoard(team, triggerPlayer.serverLevel(), null);
        TaskHudSnapshotSync.syncTeam(team, triggerPlayer.serverLevel());
    }

    private static void notifyTeamCommissionCompleted(
            ServerLevel level, TeamData team, CommissionEntry active) {
        playNotifySoundForAllPlayers(level, SoundEvents.PLAYER_LEVELUP, 0.8F, 1.0F);
        for (UUID memberId : team.getMembers()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(
                        Component.translatable("message.otherworldinn.commission.completed_with_rewards")
                                .withStyle(style -> style.withColor(ModColors.SUCCESS)));
                for (MutableComponent rewardLine : buildRewardLines(active)) {
                    member.sendSystemMessage(rewardLine);
                }
            }
        }
    }

    private static void notifyTeamCommissionExpired(ServerLevel level, TeamData team) {
        playNotifySoundForAllPlayers(level, SoundEvents.VILLAGER_NO, 0.8F, 1.0F);
        for (UUID memberId : team.getMembers()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(
                        Component.translatable("message.otherworldinn.commission.expired")
                                .withStyle(style -> style.withColor(ModColors.ERROR)));
            }
        }
    }

    private static void playNotifySoundForAllPlayers(
            ServerLevel level, SoundEvent sound, float volume, float pitch) {
        for (ServerPlayer onlinePlayer : level.getServer().getPlayerList().getPlayers()) {
            onlinePlayer.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
        }
    }

    private static List<MutableComponent> buildRewardLines(CommissionEntry active) {
        List<MutableComponent> lines = new ArrayList<>();
        if (active.getCoinReward() > 0) {
            lines.add(
                    Component.translatable(
                            "message.otherworldinn.commission.reward_line.coin", active.getCoinReward()));
        }
        for (CommissionEntry.ItemReward reward : active.getItemRewards()) {
            ResourceLocation id = ResourceLocation.tryParse(reward.itemId());
            if (id == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) {
                continue;
            }
            lines.add(
                    Component.translatable(
                            "message.otherworldinn.commission.reward_line.item",
                            Component.translatable(item.getDescriptionId()),
                            reward.count()));
        }
        for (CommissionEntry.NpcFavorReward reward : active.getNpcFavorRewards()) {
            ResourceLocation npcId = ResourceLocation.tryParse(reward.npcEntityTypeId());
            if (npcId == null) {
                continue;
            }
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(npcId);
            if (type == null) {
                continue;
            }
            lines.add(
                    Component.translatable(
                            "message.otherworldinn.commission.reward_line.favor",
                            type.getDescription(),
                            reward.favorProgress()));
        }
        return lines;
    }

    private static void grantRewards(ServerPlayer triggerPlayer, TeamData team, CommissionEntry active) {
        for (CommissionEntry.ItemReward reward : active.getItemRewards()) {
            ResourceLocation id = ResourceLocation.tryParse(reward.itemId());
            if (id == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) {
                continue;
            }
            ItemStack stack = new ItemStack(item, reward.count());
            if (!triggerPlayer.getInventory().add(stack)) {
                triggerPlayer.drop(stack, false);
            }
        }
        if (active.getCoinReward() > 0) {
            TeamManager.getInstance().addCoins(team, active.getCoinReward(), triggerPlayer.getServer());
        }
        ServerLevel townLevel = triggerPlayer.getServer().getLevel(TownDimensions.TOWN_LEVEL);
        if (townLevel != null) {
            for (CommissionEntry.NpcFavorReward reward : active.getNpcFavorRewards()) {
                ResourceLocation npcId = ResourceLocation.tryParse(reward.npcEntityTypeId());
                if (npcId == null) {
                    continue;
                }
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(npcId);
                if (type == null) {
                    continue;
                }
                for (StoreEntity storeEntity :
                        townLevel.getEntitiesOfClass(
                                StoreEntity.class, new net.minecraft.world.phys.AABB(-1024, -64, -1024, 1024, 384, 1024))) {
                    if (storeEntity.getType() == type) {
                        storeEntity.addFavorProgress(reward.favorProgress());
                        AdvancementUtils.awardStoreFavorProgress(triggerPlayer, storeEntity, townLevel);
                    }
                }
            }
        }
    }

    private static void refreshBoard(ServerLevel level, TeamData team, long day) {
        TeamCommissionData data = team.getCommissionData();
        data.resetAcceptedState();
        removeTownCommissionTodo(level, team);
        data.getBoardEntries().clear();
        long sequence = data.getRefreshSequence() + 1L;
        data.setRefreshSequence(sequence);
        long seed =
                level.getSeed()
                        ^ team.getTeamId().getMostSignificantBits()
                        ^ team.getTeamId().getLeastSignificantBits()
                        ^ day
                        ^ (sequence * 0x9E3779B97F4A7C15L);
        RandomSource random = RandomSource.create(seed);
        List<String> usedTemplateIds = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            CommissionEntry entry = generateEntry(level, random, day, i, usedTemplateIds);
            if (entry != null) {
                data.getBoardEntries().add(entry);
            }
        }
        data.setNextAutoRefreshDay(day + UNACCEPTED_REFRESH_INTERVAL_DAYS);
    }

    private static CommissionEntry generateEntry(
            ServerLevel level, RandomSource random, long day, int slot, List<String> usedTemplateIds) {
        List<String> excludedTemplateIds = new ArrayList<>(usedTemplateIds);
        int allowedMaxStars = resolveAllowedMaxStarsForDay(day);
        boolean restrictTemplatePool = shouldRestrictTemplatePool(day);
        int maxAttempts = Math.max(1, CommissionRegistry.allTemplates().size());
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            CommissionTemplate template =
                    CommissionRegistry.pickRandomTemplate(
                            random, excludedTemplateIds, allowedMaxStars, restrictTemplatePool);
            if (template == null) {
                return null;
            }

            CommissionEntry entry;
            if (FishingCommissionGenerator.TEMPLATE_ID.equals(template.id())) {
                entry = FishingCommissionGenerator.generate(level, random, day, slot);
            } else {
                int stars =
                        template.minStars() == template.maxStars()
                                ? template.minStars()
                                : template.minStars()
                                        + random.nextInt(template.maxStars() - template.minStars() + 1);
                long durationDays = durationByStars(stars);
                String id = "commission_" + template.id() + "_" + day + "_" + slot;
                entry =
                        new CommissionEntry(
                                id,
                                template.descriptionKey(),
                                stars,
                                durationDays,
                                template.submitRequirements(),
                                template.killRequirements(),
                                template.photoRequirements(),
                                template.itemRewards(),
                                template.coinReward(),
                                template.npcFavorRewards());
            }

            if (entry != null) {
                usedTemplateIds.add(template.id());
                return entry;
            }
            excludedTemplateIds.add(template.id());
        }
        return null;
    }

    private static boolean shouldRestrictTemplatePool(long zeroBasedDay) {
        return toGameplayDay(zeroBasedDay) < FULL_TEMPLATE_POOL_UNLOCK_DAY;
    }

    private static int resolveAllowedMaxStarsForDay(long zeroBasedDay) {
        long gameplayDay = toGameplayDay(zeroBasedDay);
        if (gameplayDay >= FULL_TEMPLATE_POOL_UNLOCK_DAY) {
            return 5;
        }
        return Math.min(4, 2 + (int) (((gameplayDay - 1L) * 3L) / 14L));
    }

    private static long toGameplayDay(long zeroBasedDay) {
        return Math.max(1L, zeroBasedDay + 1L);
    }

    private static long durationByStars(int stars) {
        return switch (Math.max(1, Math.min(5, stars))) {
            case 1 -> 2L;
            case 2 -> 3L;
            case 3 -> 4L;
            case 4 -> 5L;
            default -> 6L;
        };
    }

    private static long currentDay(Level level) {
        return level.getDayTime() / 24000L;
    }

    private static boolean isKillRequirementComplete(
            CommissionEntry entry, Map<String, Integer> killProgress) {
        if (!entry.hasKillRequirement()) {
            return true;
        }
        for (CommissionEntry.KillRequirement req : entry.getKillRequirements()) {
            if (killProgress.getOrDefault(req.entityTypeId(), 0) < req.count()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isPhotoRequirementComplete(
            CommissionEntry entry, java.util.Set<String> photoProgress) {
        if (!entry.hasPhotoRequirement()) {
            return true;
        }
        for (CommissionEntry.PhotoRequirement req : entry.getPhotoRequirements()) {
            if (!photoProgress.contains(req.objectiveId())) {
                return false;
            }
        }
        return true;
    }

    private static boolean areAllRequirementsComplete(CommissionEntry entry, TeamCommissionData data) {
        return isKillRequirementComplete(entry, data.getKillProgress())
                && isPhotoRequirementComplete(entry, data.getPhotoProgress());
    }

    private static boolean hasRequiredItems(
            ServerPlayer player, List<CommissionEntry.ItemRequirement> requirements) {
        Inventory inventory = player.getInventory();
        HolderLookup.Provider provider = player.registryAccess();
        for (CommissionEntry.ItemRequirement req : requirements) {
            ResourceLocation id = ResourceLocation.tryParse(req.itemId());
            if (id == null) {
                return false;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) {
                return false;
            }
            int remain = req.count();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (matchesRequirementStack(stack, item, req.nbt(), provider)) {
                    remain -= stack.getCount();
                    if (remain <= 0) {
                        break;
                    }
                }
            }
            if (remain > 0) {
                return false;
            }
        }
        return true;
    }

    private static void consumeRequiredItems(
            ServerPlayer player, List<CommissionEntry.ItemRequirement> requirements) {
        Inventory inventory = player.getInventory();
        HolderLookup.Provider provider = player.registryAccess();
        for (CommissionEntry.ItemRequirement req : requirements) {
            ResourceLocation id = ResourceLocation.tryParse(req.itemId());
            if (id == null) {
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            if (item == null) {
                continue;
            }
            int remain = req.count();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                if (remain <= 0) {
                    break;
                }
                ItemStack stack = inventory.getItem(i);
                if (!matchesRequirementStack(stack, item, req.nbt(), provider)) {
                    continue;
                }
                int take = Math.min(remain, stack.getCount());
                stack.shrink(take);
                remain -= take;
            }
        }
    }

    private static boolean matchesRequirementStack(
            ItemStack stack, Item item, CompoundTag requiredNbt, HolderLookup.Provider provider) {
        if (stack.isEmpty() || !stack.is(item)) {
            return false;
        }
        if (requiredNbt == null || requiredNbt.isEmpty()) {
            return true;
        }
        Tag saved = stack.save(provider);
        if (!(saved instanceof CompoundTag actual)) {
            return false;
        }
        return nbtContains(actual, requiredNbt);
    }

    /** required 作为子集匹配 actual，支持 Compound/List/基础类型递归比较。 */
    private static boolean nbtContains(Tag actual, Tag required) {
        if (required == null) {
            return true;
        }
        if (actual == null || actual.getId() != required.getId()) {
            return false;
        }
        if (required instanceof CompoundTag requiredCompound) {
            CompoundTag actualCompound = (CompoundTag) actual;
            for (String key : requiredCompound.getAllKeys()) {
                if (!actualCompound.contains(key)) {
                    return false;
                }
                if (!nbtContains(actualCompound.get(key), requiredCompound.get(key))) {
                    return false;
                }
            }
            return true;
        }
        if (required instanceof ListTag requiredList) {
            ListTag actualList = (ListTag) actual;
            if (actualList.size() < requiredList.size()) {
                return false;
            }
            for (int i = 0; i < requiredList.size(); i++) {
                if (!nbtContains(actualList.get(i), requiredList.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return actual.equals(required);
    }

    private static CompoundTag createBoardViewTag(TeamData team, BlockPos boardPos, long currentDay) {
        TeamCommissionData data = team.getCommissionData();
        CompoundTag tag = new CompoundTag();
        tag.putInt("AcceptedIndex", data.getAcceptedIndex());
        tag.putLong("AcceptedDay", data.getAcceptedDay());
        tag.putLong("ExpireDay", data.getExpireDay());
        tag.putLong("NextAutoRefreshDay", data.getNextAutoRefreshDay());
        tag.putBoolean("RewardClaimed", data.isRewardClaimed());
        tag.putLong("CurrentDay", currentDay);
        if (boardPos != null) {
            tag.putLong("BoardPos", boardPos.asLong());
        }
        tag.put("CommissionData", data.save());
        return tag;
    }

    private static void broadcastBoard(TeamData team, ServerLevel level, BlockPos boardPos) {
        CompoundTag payload = createBoardViewTag(team, boardPos, currentDay(level));
        TeamManager manager = TeamManager.getInstance();
        List<UUID> members =
                team.getMembers().stream().sorted(Comparator.comparing(UUID::toString)).toList();
        for (UUID memberId : members) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                ModMessages.sendToPlayer(new S2CCommissionBoardPacket(payload.copy(), false), member);
            }
        }
    }

    private static void notifyTeamCommissionAccepted(
            ServerLevel level, TeamData team, ServerPlayer acceptPlayer, long durationDays) {
        Component message =
                Component.translatable(
                                "message.otherworldinn.commission.accepted_team_broadcast",
                                acceptPlayer.getDisplayName(),
                                durationDays)
                        .withStyle(style -> style.withColor(ModColors.INFO));
        for (UUID memberId : team.getMembers()) {
            ServerPlayer member = level.getServer().getPlayerList().getPlayer(memberId);
            if (member != null) {
                member.sendSystemMessage(message);
            }
        }
    }

    private static InnTodo townCommissionTodo() {
        return InnTodo.translatable("town_commission", TOWN_COMMISSION_TODO_TEXT_KEY);
    }

    private static InnTodo addTownCommissionTodo(ServerLevel level, TeamData team) {
        InnTodo todo = townCommissionTodo();
        team.getInnData().addTodo(level, team, todo);
        return todo;
    }

    private static void removeTownCommissionTodo(ServerLevel level, TeamData team) {
        team.getInnData().removeTodo(level, team, townCommissionTodo());
    }
}
