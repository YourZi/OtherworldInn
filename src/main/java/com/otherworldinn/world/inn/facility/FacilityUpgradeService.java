package com.otherworldinn.world.inn.facility;

import com.otherworldinn.world.event.TownStructurePlacer;
import com.otherworldinn.util.AdvancementUtils;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public final class FacilityUpgradeService {
    private static final int TOOL_COOLDOWN_TICKS = 100;

    private FacilityUpgradeService() {}

    public static FacilityContext findContext(ServerPlayer player) {
        return findContext(player, player == null ? null : player.blockPosition());
    }

    public static FacilityContext findContext(ServerPlayer player, BlockPos interactionPos) {
        if (player == null) {
            return null;
        }
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return null;
        }
        FacilityRegistry.FacilityDefinition facility =
                FacilityRegistry.findFacilityInRange(interactionPos).orElse(null);
        if (facility == null) {
            return null;
        }
        int currentLevel = team.getInnData().getFacilityLevel(facility.id());
        int nextLevel = currentLevel + 1;
        FacilityRegistry.FacilityLevelDefinition nextLevelDefinition =
                nextLevel <= facility.maxLevel() ? facility.getLevel(nextLevel) : null;
        return new FacilityContext(team, facility, currentLevel, nextLevelDefinition);
    }

    public static boolean tryUpgrade(ServerPlayer player, InteractionHand hand) {
        return tryUpgrade(player, hand, player == null ? null : player.blockPosition());
    }

    public static boolean tryUpgrade(ServerPlayer player, InteractionHand hand, BlockPos interactionPos) {
        if (player == null) {
            return false;
        }
        ItemStack toolStack = player.getItemInHand(hand);
        if (!FacilityRegistry.isToolStack(toolStack)) {
            return false;
        }
        FacilityContext context = findContext(player, interactionPos);
        if (context == null) {
            return false;
        }
        if (context.nextLevelDefinition() == null) {
            if (context.currentLevel() >= context.facility().maxLevel()) {
                awardFacilityMaxLevel(player, context.facility().id());
                if (isAllFacilitiesAtMaxLevel(context.team())) {
                    AdvancementUtils.award(player, AdvancementUtils.ALL_FACILITY_MAX_LEVEL);
                }
            }
            player.displayClientMessage(
                    Component.translatable("facility.otherworldinn.upgrade_max_level"),
                    true);
            applyToolCooldown(player, toolStack);
            return true;
        }

        TeamData team = context.team();
        FacilityRegistry.FacilityLevelDefinition next = context.nextLevelDefinition();

        if (!player.getAbilities().instabuild) {
            if (team.getCoins() < next.requiredCoins()) {
                player.displayClientMessage(
                        Component.translatable(
                                "facility.otherworldinn.upgrade_fail_coins",
                                next.requiredCoins(),
                                team.getCoins()),
                        true);
                applyToolCooldown(player, toolStack);
                return true;
            }
            if (!hasRequiredItems(player.getInventory(), next.requiredItems())) {
                player.displayClientMessage(
                        Component.translatable("facility.otherworldinn.upgrade_fail_items"),
                        true);
                applyToolCooldown(player, toolStack);
                return true;
            }
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            applyToolCooldown(player, toolStack);
            return true;
        }
        boolean placed =
                TownStructurePlacer.placeStructureTemplate(
                        serverLevel, next.structureId(), context.facility().centerPos());
        if (!placed) {
            player.displayClientMessage(
                    Component.translatable("facility.otherworldinn.upgrade_fail_structure"),
                    true);
            applyToolCooldown(player, toolStack);
            return true;
        }

        if (!player.getAbilities().instabuild) {
            player.getItemInHand(hand).shrink(1);
            removeRequiredItems(player.getInventory(), next.requiredItems());
            TeamManager.getInstance().removeCoins(team, next.requiredCoins(), player.getServer());
        }

        boolean isRepair = context.currentLevel() == 0;
        playUpgradeEffects(
                player,
                serverLevel,
                interactionPos == null ? context.facility().centerPos() : interactionPos,
                hand,
                isRepair);
        team.getInnData().setFacilityLevel(context.facility().id(), next.level());
        // 矿井结构已重新放置，重新定位一次木桶并刷新缓存
        if ("mine".equals(context.facility().id())) {
            MineService.refreshBarrelCache(serverLevel, context.facility());
        }
        if (next.level() >= context.facility().maxLevel()) {
            awardFacilityMaxLevel(player, context.facility().id());
        }
        if (isAllFacilitiesAtMaxLevel(team)) {
            AdvancementUtils.award(player, AdvancementUtils.ALL_FACILITY_MAX_LEVEL);
        }
        if (isRepair) {
            TeamManager.getInstance()
                    .unlockMapPoint(team, context.facility().mapPointId(), player.getServer());
            if ("boiler_room".equals(context.facility().id())) {
                AdvancementUtils.award(player, AdvancementUtils.REPAIR_BOILER_ROOM);
            } else if ("greenhouse".equals(context.facility().id())) {
                AdvancementUtils.award(player, AdvancementUtils.REPAIR_GREENHOUSE);
            }
        }
        TeamManager.getInstance().syncTeam(team, player.getServer());
        String resultKey =
                isRepair
                        ? "facility.otherworldinn.repair_success"
                        : "facility.otherworldinn.upgrade_success";
        player.displayClientMessage(
                Component.translatable(
                        resultKey,
                        Component.translatable(context.facility().translationKey()),
                        next.level()),
                true);
        applyToolCooldown(player, toolStack);
        return true;
    }

    private static boolean isAllFacilitiesAtMaxLevel(TeamData team) {
        if (team == null) {
            return false;
        }
        for (FacilityRegistry.FacilityDefinition definition : FacilityRegistry.getAll()) {
            if (team.getInnData().getFacilityLevel(definition.id()) < definition.maxLevel()) {
                return false;
            }
        }
        return true;
    }

    private static void awardFacilityMaxLevel(ServerPlayer player, String facilityId) {
        if ("boiler_room".equals(facilityId)) {
            AdvancementUtils.award(player, AdvancementUtils.BOILER_ROOM_MAX_LEVEL);
        } else if ("greenhouse".equals(facilityId)) {
            AdvancementUtils.award(player, AdvancementUtils.GREENHOUSE_MAX_LEVEL);
        } else if ("mine".equals(facilityId)) {
            AdvancementUtils.award(player, AdvancementUtils.MINE_MAX_LEVEL);
        }
    }

    private static void applyToolCooldown(ServerPlayer player, ItemStack toolStack) {
        if (toolStack.isEmpty()) {
            return;
        }
        player.getCooldowns().addCooldown(toolStack.getItem(), TOOL_COOLDOWN_TICKS);
    }

    private static void playUpgradeEffects(
            ServerPlayer player,
            ServerLevel level,
            BlockPos centerPos,
            InteractionHand hand,
            boolean isRepair) {
        player.swing(hand, true);
        double x = centerPos.getX() + 0.5D;
        double y = centerPos.getY() + 1.0D;
        double z = centerPos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 24, 0.9D, 0.6D, 0.9D, 0.02D);
        level.playSound(
                null,
                centerPos,
                isRepair ? SoundEvents.ANVIL_USE : SoundEvents.PLAYER_LEVELUP,
                SoundSource.PLAYERS,
                1.0F,
                isRepair ? 1.0F : 1.1F);
    }

    private static boolean hasRequiredItems(Inventory inventory, List<ItemStack> requiredItems) {
        for (ItemStack required : requiredItems) {
            if (required.isEmpty()) {
                continue;
            }
            if (countMatchingItems(inventory, required) < required.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static int countMatchingItems(Inventory inventory, ItemStack required) {
        int total = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack slot = inventory.getItem(i);
            if (ItemStack.isSameItemSameComponents(slot, required)) {
                total += slot.getCount();
            }
        }
        return total;
    }

    private static void removeRequiredItems(Inventory inventory, List<ItemStack> requiredItems) {
        for (ItemStack required : requiredItems) {
            int remaining = required.getCount();
            if (remaining <= 0) {
                continue;
            }
            for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
                ItemStack slot = inventory.getItem(i);
                if (!ItemStack.isSameItemSameComponents(slot, required)) {
                    continue;
                }
                int removed = Math.min(remaining, slot.getCount());
                slot.shrink(removed);
                remaining -= removed;
                if (slot.isEmpty()) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        inventory.setChanged();
    }

    public record FacilityContext(
            TeamData team,
            FacilityRegistry.FacilityDefinition facility,
            int currentLevel,
            FacilityRegistry.FacilityLevelDefinition nextLevelDefinition) {}
}
