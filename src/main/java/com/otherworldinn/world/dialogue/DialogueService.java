package com.otherworldinn.world.dialogue;

import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.entity.guest.StoryGuestEntity;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CDialogueClosePacket;
import com.otherworldinn.network.packet.S2CDialogueNodePacket;
import com.otherworldinn.world.storyguest.StoryGuestService;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

public final class DialogueService {
    private static final double MAX_DIALOGUE_DISTANCE_SQR = 100.0D;
    private static final ResourceLocation NATURES_COMPASS_ID =
            ResourceLocation.fromNamespaceAndPath("naturescompass", "naturescompass");
    private static final ResourceLocation MINSTREL_DISC_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "music_disc_mellohi");
    private static final ResourceLocation CHEF_LUNCH_BAG_ID =
            ResourceLocation.fromNamespaceAndPath(
                    "kaleidoscope_cookery", "transmutation_lunch_bag");
    private static final ResourceLocation NOBLE_CLOCK_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "clock");
    private static final ResourceLocation ALCHEMIST_POTION_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "potion");
    private static final ResourceLocation ARCHAEOLOGIST_BRUSH_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "brush");
    private static final ResourceLocation GEM_MERCHANT_STAR_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "nether_star");
    private static final ResourceLocation OLD_KNIGHT_SHIELD_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "shield");
    private static final ResourceLocation CURSED_ADVENTURER_APPLE_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "golden_apple");
    private static final ResourceLocation OLD_ANGLER_ROD_ID =
            ResourceLocation.fromNamespaceAndPath("minecraft", "fishing_rod");
    private static final Map<UUID, DialogueSession> SESSIONS = new HashMap<>();

    private DialogueService() {}

    public static boolean tryStartDialogue(ServerPlayer player, Entity entity) {
        DialogueDefinition definition = DialogueRegistry.resolve(entity);
        if (definition == null) {
            return false;
        }
        DialogueNodeDef root = definition.getNode(definition.rootNodeId());
        if (root == null) {
            return false;
        }
        DialogueSession session =
                new DialogueSession(player.getUUID(), entity.getId(), definition, root.id(), entity.getUUID());
        SESSIONS.put(player.getUUID(), session);
        sendNodeToPlayer(player, session, root);
        return true;
    }

    public static void selectOption(ServerPlayer player, int entityId, String optionId) {
        DialogueSession session = SESSIONS.get(player.getUUID());
        if (session == null || session.entityId() != entityId) {
            return;
        }
        Entity entity = player.level().getEntity(entityId);
        if (entity == null || !entity.isAlive()) {
            closeDialogue(player, true);
            return;
        }
        if (!entity.getUUID().equals(session.entityUuid())
                || player.distanceToSqr(entity) > MAX_DIALOGUE_DISTANCE_SQR) {
            closeDialogue(player, true);
            return;
        }
        if (!storySessionStillCurrent(entity, session)) {
            closeDialogue(player, true);
            return;
        }
        DialogueNodeDef currentNode = session.definition().getNode(session.currentNodeId());
        if (currentNode == null) {
            closeDialogue(player, true);
            return;
        }
        DialogueOptionDef selected = null;
        for (DialogueOptionDef option : currentNode.options()) {
            if (option.id().equals(optionId)) {
                selected = option;
                break;
            }
        }
        if (selected == null) {
            return;
        }

        if (!requirementsMet(player, entity, selected)) {
            return;
        }
        if (!applyEffects(player, entity, selected)) {
            return;
        }
        if (entity instanceof StoryGuestEntity storyGuest && advancesStoryStage(selected)) {
            storyGuest.markVisitStageConsumed(player.serverLevel());
        }
        if (entity instanceof StoryGuestEntity && invalidatesStorySessions(selected)) {
            closeOtherSessionsForEntity(player, entity);
        }

        if (selected.type() == DialogueOptionType.FUNCTION) {
            if (DialogueRegistry.FUNCTION_OPEN_STORE.equals(selected.functionId())
                    && entity instanceof StoreEntity storeEntity) {
                storeEntity.playOpenStoreSound();
                storeEntity.openStoreForPlayer(player);
            } else if (DialogueRegistry.FUNCTION_OPEN_VIRTUAL_ANVIL.equals(selected.functionId())) {
                openVirtualAnvil(player);
            } else if (DialogueRegistry.FUNCTION_OPEN_RECYCLE.equals(selected.functionId())
                    && entity instanceof WanderingTraderEntity trader) {
                trader.tryOpenRecycleMenu(player);
            }
            closeDialogue(player, true);
            return;
        }

        if (selected.nextNodeId() == null || selected.nextNodeId().isBlank()) {
            closeDialogue(player, true);
            return;
        }
        DialogueNodeDef nextNode = session.definition().getNode(selected.nextNodeId());
        if (nextNode == null) {
            closeDialogue(player, true);
            return;
        }
        DialogueSession nextSession =
                new DialogueSession(
                        session.playerUuid(),
                        session.entityId(),
                        session.definition(),
                        nextNode.id(),
                        session.entityUuid());
        SESSIONS.put(player.getUUID(), nextSession);
        sendNodeToPlayer(player, nextSession, nextNode);
    }

    public static void closeDialogue(ServerPlayer player, boolean notifyClient) {
        SESSIONS.remove(player.getUUID());
        if (notifyClient) {
            ModMessages.sendToPlayer(new S2CDialogueClosePacket(), player);
        }
    }

    private static boolean storySessionStillCurrent(Entity entity, DialogueSession session) {
        if (!(entity instanceof StoryGuestEntity)) {
            return true;
        }
        DialogueDefinition currentDefinition = DialogueRegistry.resolve(entity);
        return currentDefinition != null && currentDefinition.id().equals(session.definition().id());
    }

    private static void closeOtherSessionsForEntity(ServerPlayer currentPlayer, Entity entity) {
        UUID entityUuid = entity.getUUID();
        Iterator<Map.Entry<UUID, DialogueSession>> iterator = SESSIONS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, DialogueSession> entry = iterator.next();
            UUID playerUuid = entry.getKey();
            if (playerUuid.equals(currentPlayer.getUUID())) {
                continue;
            }
            DialogueSession session = entry.getValue();
            if (!entityUuid.equals(session.entityUuid())) {
                continue;
            }
            iterator.remove();
            ServerPlayer player = currentPlayer.getServer().getPlayerList().getPlayer(playerUuid);
            if (player != null) {
                ModMessages.sendToPlayer(new S2CDialogueClosePacket(), player);
            }
        }
    }

    private static void sendNodeToPlayer(
            ServerPlayer player, DialogueSession session, DialogueNodeDef node) {
        String nodeTextKey = resolveNodeTextKey(player, session, node);
        DialogueNodeView nodeView =
                new DialogueNodeView(
                        session.entityId(),
                        session.definition().id(),
                        node.id(),
                        nodeTextKey,
                        node.options().stream()
                                .map(
                                        o ->
                                                new DialogueOptionView(
                                                        o.id(),
                                                        session.definition()
                                                                .optionTextKey(node.id(), o.id()),
                                                        o.type()))
                                .toList());
        ModMessages.sendToPlayer(new S2CDialogueNodePacket(nodeView), player);
    }

    private static String resolveNodeTextKey(
            ServerPlayer player, DialogueSession session, DialogueNodeDef node) {
        DialogueNodeConditionalText conditionalText = node.conditionalText();
        if (conditionalText == null) {
            return session.definition().nodeTextKey(node.id());
        }
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        boolean repaired =
                team != null && team.getInnData().getFacilityLevel(conditionalText.facilityId()) > 0;
        return session.definition().nodeConditionalTextKey(node.id(), repaired);
    }

    private static void openVirtualAnvil(ServerPlayer player) {
        MenuProvider menuProvider =
                new SimpleMenuProvider(
                        (id, inventory, ignoredPlayer) ->
                                new AnvilMenu(id, inventory, ContainerLevelAccess.NULL) {
                                    @Override
                                    public boolean stillValid(Player playerEntity) {
                                        return true;
                                    }
                                },
                        Component.translatable("container.repair"));
        player.openMenu(menuProvider);
    }

    private static boolean requirementsMet(
            ServerPlayer player, Entity entity, DialogueOptionDef selected) {
        for (DialogueRequirementDef requirement : selected.requirements()) {
            if (meetsRequirement(player, entity, requirement)) {
                continue;
            }
            if (requirement.type() == DialogueRequirementType.HAS_ITEM) {
                player.displayClientMessage(
                        Component.translatable("message.otherworldinn.dialogue.requirement_items_missing"),
                        false);
            } else {
                player.displayClientMessage(
                        Component.translatable("message.otherworldinn.dialogue.option_unavailable"),
                        false);
            }
            return false;
        }
        return true;
    }

    private static boolean meetsRequirement(
            ServerPlayer player, Entity entity, DialogueRequirementDef requirement) {
        return switch (requirement.type()) {
            case HAS_ITEM -> hasRequiredItem(player, requirement.itemId(), requirement.count());
            case STORY_FLAG_PRESENT -> hasStoryFlag(entity, player.serverLevel(), requirement.storyFlag(), true);
            case STORY_FLAG_ABSENT -> hasStoryFlag(entity, player.serverLevel(), requirement.storyFlag(), false);
            case STORY_STAGE_EQUALS -> hasStoryStage(entity, player.serverLevel(), requirement.stageValue());
        };
    }

    private static boolean applyEffects(ServerPlayer player, Entity entity, DialogueOptionDef selected) {
        for (DialogueEffectDef effect : selected.effects()) {
            if (applyEffect(player, entity, effect)) {
                continue;
            }
            player.displayClientMessage(
                    Component.translatable("message.otherworldinn.dialogue.option_unavailable"),
                    false);
            return false;
        }
        return true;
    }

    private static boolean applyEffect(ServerPlayer player, Entity entity, DialogueEffectDef effect) {
        ServerLevel level = player.serverLevel();
        return switch (effect.type()) {
            case TAKE_ITEM -> consumeItem(player, effect.itemId(), effect.count());
            case GIVE_ITEM -> giveItem(player, effect.itemId(), effect.count());
            case GIVE_COINS -> giveCoins(player, effect.count());
            case SET_STORY_FLAG ->
                    entity instanceof StoryGuestEntity storyGuest
                            && applyStoryFlag(storyGuest, level, effect.storyFlag());
            case ADVANCE_STORY_STAGE ->
                    entity instanceof StoryGuestEntity storyGuest
                            && applyStoryStage(storyGuest, level, effect.stageValue());
            case SET_NEXT_VISIT_RANGE ->
                    entity instanceof StoryGuestEntity storyGuest
                            && applyNextVisitRange(
                                    storyGuest, level, effect.minDays(), effect.maxDays());
        };
    }

    private static boolean giveCoins(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return false;
        }
        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) {
            return false;
        }
        TeamManager.getInstance().addCoins(team, amount, player.getServer());
        return true;
    }

    private static boolean applyStoryFlag(
            StoryGuestEntity storyGuest, ServerLevel level, @Nullable String storyFlag) {
        if (storyFlag == null || storyFlag.isBlank()) {
            return false;
        }
        StoryGuestService.addStoryFlag(storyGuest, level, storyFlag);
        return true;
    }

    private static boolean applyStoryStage(
            StoryGuestEntity storyGuest, ServerLevel level, int stageValue) {
        StoryGuestService.setStoryStage(storyGuest, level, stageValue);
        return true;
    }

    private static boolean applyNextVisitRange(
            StoryGuestEntity storyGuest, ServerLevel level, int minDays, int maxDays) {
        StoryGuestService.setPendingReturnRange(storyGuest, level, minDays, maxDays);
        return true;
    }

    private static boolean advancesStoryStage(DialogueOptionDef selected) {
        for (DialogueEffectDef effect : selected.effects()) {
            if (effect.type() == DialogueEffectType.ADVANCE_STORY_STAGE) {
                return true;
            }
        }
        return false;
    }

    private static boolean invalidatesStorySessions(DialogueOptionDef selected) {
        for (DialogueEffectDef effect : selected.effects()) {
            if (effect.type() == DialogueEffectType.SET_STORY_FLAG
                    || effect.type() == DialogueEffectType.ADVANCE_STORY_STAGE
                    || effect.type() == DialogueEffectType.SET_NEXT_VISIT_RANGE) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRequiredItem(
            ServerPlayer player, @Nullable net.minecraft.resources.ResourceLocation itemId, int count) {
        Item item = resolveItem(itemId);
        if (item == null || count <= 0) {
            return false;
        }
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                remaining -= stack.getCount();
                if (remaining <= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean consumeItem(
            ServerPlayer player, @Nullable net.minecraft.resources.ResourceLocation itemId, int count) {
        Item item = resolveItem(itemId);
        if (item == null || count <= 0) {
            return false;
        }
        int remaining = count;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (remaining <= 0) {
                break;
            }
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(item)) {
                continue;
            }
            int taken = Math.min(remaining, stack.getCount());
            stack.shrink(taken);
            remaining -= taken;
        }
        player.getInventory().setChanged();
        return remaining <= 0;
    }

    private static boolean giveItem(
            ServerPlayer player, @Nullable net.minecraft.resources.ResourceLocation itemId, int count) {
        Item item = resolveItem(itemId);
        if (item == null || count <= 0) {
            return false;
        }
        ItemStack reward = createRewardStack(player, itemId, item, count);
        if (player.getInventory().add(reward)) {
            player.getInventory().setChanged();
            return true;
        }
        ItemEntity itemEntity =
                new ItemEntity(
                        player.level(),
                        player.getX(),
                        player.getY() + 0.5D,
                        player.getZ(),
                        reward.copy());
        itemEntity.setPickUpDelay(0);
        player.level().addFreshEntity(itemEntity);
        return true;
    }

    private static ItemStack createRewardStack(
            ServerPlayer player,
            @Nullable net.minecraft.resources.ResourceLocation itemId,
            Item item,
            int count) {
        ItemStack reward = new ItemStack(item, count);
        String storyGuestId = resolveDialogueStoryGuestId(player);
        if (storyGuestId == null || itemId == null) {
            return reward;
        }
        if ("wandering_cartographer".equals(storyGuestId) && NATURES_COMPASS_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_cartographer_compass")
                            .withStyle(style -> style.withColor(ChatFormatting.GOLD).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_cartographer_compass")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            reward.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            return reward;
        }
        if ("wandering_minstrel".equals(storyGuestId) && MINSTREL_DISC_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_minstrel_disc")
                            .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_minstrel_disc")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            return reward;
        }
        if ("wandering_chef".equals(storyGuestId) && CHEF_LUNCH_BAG_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_chef_lunch_bag")
                            .withStyle(style -> style.withColor(ChatFormatting.GOLD).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_chef_lunch_bag")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            return reward;
        }
        if ("fallen_noble".equals(storyGuestId) && NOBLE_CLOCK_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_noble_clock")
                            .withStyle(style -> style.withColor(ChatFormatting.GOLD).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_noble_clock")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            return reward;
        }
        if ("wandering_alchemist".equals(storyGuestId) && ALCHEMIST_POTION_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_alchemist_potion")
                            .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_alchemist_potion")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            reward.set(
                    DataComponents.POTION_CONTENTS,
                    new PotionContents(
                            Optional.empty(),
                            Optional.of(0xA95CFF),
                            List.of(
                                    new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60 * 8, 0),
                                    new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 60 * 3, 0),
                                    new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 60 * 8, 0),
                                    new MobEffectInstance(MobEffects.REGENERATION, 20 * 45, 1))));
            return reward;
        }
        if ("archaeologist".equals(storyGuestId) && ARCHAEOLOGIST_BRUSH_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_archaeologist_brush")
                            .withStyle(style -> style.withColor(ChatFormatting.GOLD).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_archaeologist_brush")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            reward.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            return reward;
        }
        if ("gem_merchant".equals(storyGuestId) && GEM_MERCHANT_STAR_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_gem_merchant_star")
                            .withStyle(style -> style.withColor(ChatFormatting.AQUA).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_gem_merchant_star")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            return reward;
        }
        if ("old_knight".equals(storyGuestId) && OLD_KNIGHT_SHIELD_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_old_knight_shield")
                            .withStyle(style -> style.withColor(ChatFormatting.GOLD).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_old_knight_shield")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            reward.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
            return reward;
        }
        if ("cursed_adventurer".equals(storyGuestId)
                && CURSED_ADVENTURER_APPLE_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_cursed_adventurer_apple")
                            .withStyle(style -> style.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable(
                                                    "tooltip.otherworldinn.story_cursed_adventurer_apple")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            return reward;
        }
        if ("old_angler".equals(storyGuestId) && OLD_ANGLER_ROD_ID.equals(itemId)) {
            reward.set(
                    DataComponents.CUSTOM_NAME,
                    Component.translatable("item.otherworldinn.story_old_angler_rod")
                            .withStyle(style -> style.withColor(ChatFormatting.AQUA).withItalic(false)));
            reward.set(
                    DataComponents.LORE,
                    new ItemLore(
                            List.of(
                                    Component.translatable("tooltip.otherworldinn.story_old_angler_rod")
                                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC))));
            reward.set(DataComponents.UNBREAKABLE, new Unbreakable(true));
        }
        return reward;
    }

    @Nullable
    private static String resolveDialogueStoryGuestId(ServerPlayer player) {
        DialogueSession session = SESSIONS.get(player.getUUID());
        if (session == null) {
            return null;
        }
        Entity entity = player.level().getEntity(session.entityId());
        if (!(entity instanceof StoryGuestEntity storyGuest)) {
            return null;
        }
        return storyGuest.getStoryGuestId();
    }

    @Nullable
    private static Item resolveItem(@Nullable net.minecraft.resources.ResourceLocation itemId) {
        if (itemId == null) {
            return null;
        }
        return BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
    }

    private static boolean hasStoryFlag(
            Entity entity, ServerLevel level, @Nullable String storyFlag, boolean expected) {
        if (!(entity instanceof StoryGuestEntity storyGuest) || storyFlag == null || storyFlag.isBlank()) {
            return false;
        }
        return StoryGuestService.hasStoryFlag(storyGuest, level, storyFlag) == expected;
    }

    private static boolean hasStoryStage(Entity entity, ServerLevel level, int stageValue) {
        if (!(entity instanceof StoryGuestEntity storyGuest)) {
            return false;
        }
        return StoryGuestService.getStoryStage(storyGuest, level) == stageValue;
    }

    private record DialogueSession(
            UUID playerUuid,
            int entityId,
            DialogueDefinition definition,
            String currentNodeId,
            @Nullable UUID entityUuid) {}
}
