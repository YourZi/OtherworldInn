package com.otherworldinn.world.dialogue;

import com.otherworldinn.entity.base.StoreEntity;
import com.otherworldinn.entity.store.WanderingTraderEntity;
import com.otherworldinn.network.ModMessages;
import com.otherworldinn.network.packet.S2CDialogueClosePacket;
import com.otherworldinn.network.packet.S2CDialogueNodePacket;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import org.jetbrains.annotations.Nullable;

public final class DialogueService {
    private static final double MAX_DIALOGUE_DISTANCE_SQR = 100.0D;
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

    private record DialogueSession(
            UUID playerUuid,
            int entityId,
            DialogueDefinition definition,
            String currentNodeId,
            @Nullable UUID entityUuid) {}
}
