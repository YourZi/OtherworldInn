package com.otherworldinn.world.hud;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.api.OtherworldInnHudSnapshotApi;
import com.otherworldinn.world.commission.CommissionEntry;
import com.otherworldinn.world.commission.TeamCommissionData;
import com.otherworldinn.world.dialogue.DialogueDefinition;
import com.otherworldinn.world.dialogue.DialogueNodeDef;
import com.otherworldinn.world.dialogue.DialogueOptionDef;
import com.otherworldinn.world.dialogue.DialogueRequirementDef;
import com.otherworldinn.world.dialogue.DialogueRequirementType;
import com.otherworldinn.world.photo.PhotoObjective;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import com.otherworldinn.world.photo.StoryGuestPhotoTask;
import com.otherworldinn.world.photo.StoryGuestPhotoTaskRegistry;
import com.otherworldinn.world.storyguest.StoryGuestDefinition;
import com.otherworldinn.world.storyguest.StoryGuestService;
import com.otherworldinn.world.storyguest.StoryGuestTodoRegistry;
import com.otherworldinn.world.team.TeamData;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class TaskHudSnapshotBuilder {
    private static final String KIND_COMMISSION = "commission";
    private static final String KIND_STORY_GUEST = "story_guest";
    private static final String TYPE_ITEM = "item";
    private static final String TYPE_KILL = "kill";
    private static final String TYPE_PHOTO = "photo";
    private static final String TYPE_COIN = "coin";
    private static final String TYPE_NPC_FAVOR = "npc_favor";
    private static final String TOWN_COMMISSION_TODO_TEXT_KEY =
            "todo.otherworldinn.town_commission_pending";

    private TaskHudSnapshotBuilder() {}

    public static CompoundTag buildForPlayer(ServerPlayer player, TeamData team) {
        if (player == null || team == null) {
            return OtherworldInnHudSnapshotApi.createEmptySnapshot();
        }

        CompoundTag snapshot = OtherworldInnHudSnapshotApi.createEmptySnapshot();
        ListTag tasks = new ListTag();
        List<String> todoList = team.getInnData().getTodoList();
        addCommissionTask(tasks, player, team, todoList);
        addStoryGuestTasks(tasks, player.serverLevel(), player, team, todoList);
        snapshot.put("Tasks", tasks);
        return snapshot;
    }

    private static void addCommissionTask(
            ListTag tasks, ServerPlayer player, TeamData team, List<String> todoList) {
        TeamCommissionData data = team.getCommissionData();
        CommissionEntry active = data.getAcceptedEntry();
        if (active == null) {
            return;
        }

        CompoundTag task = createTask(
                "commission:" + active.getId(),
                KIND_COMMISSION,
                active.getDescriptionKey(),
                resolveText(active.getDescriptionKey(), active.getId()),
                active.hasSubmitRequirement(),
                data.isRewardClaimed(),
                commissionAcceptedAt(data, todoList));

        ListTag requirements = new ListTag();
        for (CommissionEntry.ItemRequirement requirement : active.getSubmitRequirements()) {
            int current = countMatchingItems(player, requirement.itemId(), requirement.nbt());
            requirements.add(createRequirement(
                    TYPE_ITEM,
                    requirement.itemId(),
                    itemDisplayKey(requirement.itemId()),
                    itemDisplayText(requirement.itemId()),
                    requirement.count(),
                    current,
                    current >= requirement.count(),
                    requirement.nbt()));
        }
        for (CommissionEntry.KillRequirement requirement : active.getKillRequirements()) {
            int current = data.getKillProgress().getOrDefault(requirement.entityTypeId(), 0);
            requirements.add(createRequirement(
                    TYPE_KILL,
                    requirement.entityTypeId(),
                    entityDisplayKey(requirement.entityTypeId()),
                    entityDisplayText(requirement.entityTypeId()),
                    requirement.count(),
                    current,
                    current >= requirement.count(),
                    null));
        }
        for (CommissionEntry.PhotoRequirement requirement : active.getPhotoRequirements()) {
            boolean complete = data.getPhotoProgress().contains(requirement.objectiveId());
            requirements.add(createPhotoRequirement(requirement.objectiveId(), complete));
        }
        task.put("Requirements", requirements);

        ListTag rewards = new ListTag();
        for (CommissionEntry.ItemReward reward : active.getItemRewards()) {
            rewards.add(createReward(
                    TYPE_ITEM,
                    reward.itemId(),
                    itemDisplayKey(reward.itemId()),
                    itemDisplayText(reward.itemId()),
                    reward.count()));
        }
        if (active.getCoinReward() > 0) {
            String coinId = ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "coin").toString();
            rewards.add(createReward(
                    TYPE_COIN,
                    coinId,
                    itemDisplayKey(coinId),
                    itemDisplayText(coinId),
                    active.getCoinReward()));
        }
        for (CommissionEntry.NpcFavorReward reward : active.getNpcFavorRewards()) {
            rewards.add(createReward(
                    TYPE_NPC_FAVOR,
                    reward.npcEntityTypeId(),
                    entityDisplayKey(reward.npcEntityTypeId()),
                    entityDisplayText(reward.npcEntityTypeId()),
                    reward.favorProgress()));
        }
        task.put("Rewards", rewards);
        tasks.add(task);
    }

    private static void addStoryGuestTasks(
            ListTag tasks, ServerLevel level, ServerPlayer player, TeamData team, List<String> todoList) {
        if (todoList.isEmpty()) {
            return;
        }

        for (StoryGuestTodoRegistry.TodoEntry entry : StoryGuestTodoRegistry.allTodos()) {
            String titleText = entry.resolveText();
            if (!todoList.contains(titleText)) {
                continue;
            }
            if (!isStoryTodoRelevant(level, entry)) {
                continue;
            }

            boolean complete = entry.completionFlag() != null
                    && StoryGuestService.hasStoryFlag(level, entry.storyGuestId(), entry.completionFlag());
            CompoundTag task = createTask(
                    "story_guest:" + entry.storyGuestId() + ":" + entry.todoKey(),
                    KIND_STORY_GUEST,
                    entry.todoKey(),
                    titleText,
                    !entry.isPhotoTask(),
                    complete,
                    todoAcceptedAt(todoList, titleText));

            ListTag requirements = new ListTag();
            if (entry.isPhotoTask()) {
                StoryGuestPhotoTask photoTask = StoryGuestPhotoTaskRegistry.findByCompletionFlag(
                        entry.storyGuestId(), entry.completionFlag());
                if (photoTask != null) {
                    requirements.add(createPhotoRequirement(photoTask.objectiveId().toString(), complete));
                }
            } else {
                addStoryItemRequirements(requirements, player, entry);
            }
            task.put("Requirements", requirements);
            task.put("Rewards", new ListTag());
            tasks.add(task);
        }
    }

    private static void addStoryItemRequirements(
            ListTag requirements, ServerPlayer player, StoryGuestTodoRegistry.TodoEntry entry) {
        String completionOptionId = entry.completionOptionId();
        if (completionOptionId == null || completionOptionId.isBlank()) {
            return;
        }
        StoryGuestDefinition definition = StoryGuestService.getDefinition(entry.storyGuestId());
        if (definition == null) {
            return;
        }
        for (DialogueOptionDef option : findOptions(definition, completionOptionId)) {
            for (DialogueRequirementDef requirement : option.requirements()) {
                if (requirement.type() != DialogueRequirementType.HAS_ITEM
                        || requirement.itemId() == null) {
                    continue;
                }
                String itemId = requirement.itemId().toString();
                int current = countMatchingItems(player, itemId, null);
                requirements.add(createRequirement(
                        TYPE_ITEM,
                        itemId,
                        itemDisplayKey(itemId),
                        itemDisplayText(itemId),
                        requirement.count(),
                        current,
                        current >= requirement.count(),
                        null));
            }
        }
    }

    private static List<DialogueOptionDef> findOptions(
            StoryGuestDefinition definition, String optionId) {
        java.util.ArrayList<DialogueOptionDef> result = new java.util.ArrayList<>();
        for (DialogueDefinition dialogue : definition.dialogues()) {
            for (DialogueNodeDef node : dialogue.nodes().values()) {
                for (DialogueOptionDef option : node.options()) {
                    if (option.id().equals(optionId)) {
                        result.add(option);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isStoryTodoRelevant(ServerLevel level, StoryGuestTodoRegistry.TodoEntry entry) {
        if (entry.isPhotoTask()) {
            StoryGuestPhotoTask photoTask = StoryGuestPhotoTaskRegistry.findByCompletionFlag(
                    entry.storyGuestId(), entry.completionFlag());
            if (photoTask == null) {
                return false;
            }
            return StoryGuestService.hasStoryFlag(level, entry.storyGuestId(), photoTask.activationFlag())
                    && !StoryGuestService.hasStoryFlag(level, entry.storyGuestId(), photoTask.completionFlag());
        }

        StoryGuestDefinition definition = StoryGuestService.getDefinition(entry.storyGuestId());
        if (definition == null) {
            return false;
        }
        String dialogueId = definition.resolveDialogueId(
                StoryGuestService.getStoryStage(level, entry.storyGuestId()));
        for (DialogueDefinition dialogue : definition.dialogues()) {
            if (!dialogue.id().equals(dialogueId)) {
                continue;
            }
            DialogueNodeDef root = dialogue.getNode(dialogue.rootNodeId());
            if (root == null) {
                return false;
            }
            for (DialogueOptionDef option : root.options()) {
                if (option.id().equals(entry.acceptOptionId())
                        || entry.completionOptionId() != null
                        && option.id().equals(entry.completionOptionId())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static CompoundTag createTask(
            String id,
            String kind,
            String titleKey,
            String titleText,
            boolean requiresTurnIn,
            boolean complete,
            long acceptedAt) {
        CompoundTag task = new CompoundTag();
        task.putString("Id", id);
        task.putString("Kind", kind);
        task.putString("TitleKey", titleKey == null ? "" : titleKey);
        task.putString("TitleText", titleText == null ? "" : titleText);
        task.putBoolean("RequiresTurnIn", requiresTurnIn);
        task.putBoolean("Complete", complete);
        task.putLong("AcceptedAt", acceptedAt);
        return task;
    }

    private static CompoundTag createPhotoRequirement(String objectiveId, boolean complete) {
        return createRequirement(
                TYPE_PHOTO,
                objectiveId,
                photoDisplayKey(objectiveId),
                photoDisplayText(objectiveId),
                1,
                complete ? 1 : 0,
                complete,
                null);
    }

    private static CompoundTag createRequirement(
            String type,
            String targetId,
            String displayKey,
            String displayText,
            int required,
            int current,
            boolean complete,
            @Nullable CompoundTag nbt) {
        CompoundTag requirement = new CompoundTag();
        requirement.putString("Id", type + ":" + (targetId == null ? "" : targetId));
        requirement.putString("Type", type);
        requirement.putString("TargetId", targetId == null ? "" : targetId);
        requirement.putString("DisplayKey", displayKey == null ? "" : displayKey);
        requirement.putString("DisplayText", displayText == null ? "" : displayText);
        requirement.putInt("Required", Math.max(1, required));
        requirement.putInt("Current", Math.max(0, current));
        requirement.putBoolean("Complete", complete);
        requirement.put("Nbt", nbt == null ? new CompoundTag() : nbt.copy());
        return requirement;
    }

    private static CompoundTag createReward(
            String type, String targetId, String displayKey, String displayText, int count) {
        CompoundTag reward = new CompoundTag();
        reward.putString("Type", type);
        reward.putString("TargetId", targetId == null ? "" : targetId);
        reward.putString("DisplayKey", displayKey == null ? "" : displayKey);
        reward.putString("DisplayText", displayText == null ? "" : displayText);
        reward.putInt("Count", Math.max(1, count));
        return reward;
    }

    private static long commissionAcceptedAt(TeamCommissionData data, List<String> todoList) {
        String todoText = Component.translatable(TOWN_COMMISSION_TODO_TEXT_KEY).getString();
        long todoOrder = todoAcceptedAt(todoList, todoText);
        if (todoOrder != Long.MAX_VALUE) {
            return todoOrder;
        }
        if (data.getAcceptedOrder() != Long.MAX_VALUE) {
            return data.getAcceptedOrder();
        }
        return data.getAcceptedDay() >= 0L ? data.getAcceptedDay() : Long.MAX_VALUE;
    }

    private static long todoAcceptedAt(List<String> todoList, String todoText) {
        int index = todoList.indexOf(todoText);
        return index < 0 ? Long.MAX_VALUE : index;
    }

    private static int countMatchingItems(
            ServerPlayer player, String itemId, @Nullable CompoundTag requiredNbt) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return 0;
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (item == null) {
            return 0;
        }
        int count = 0;
        Inventory inventory = player.getInventory();
        HolderLookup.Provider provider = player.registryAccess();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!matchesRequirementStack(stack, item, requiredNbt, provider)) {
                continue;
            }
            count += stack.getCount();
        }
        return count;
    }

    private static boolean matchesRequirementStack(
            ItemStack stack, Item item, @Nullable CompoundTag requiredNbt, HolderLookup.Provider provider) {
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

    private static boolean nbtContains(@Nullable Tag actual, @Nullable Tag required) {
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

    private static String itemDisplayKey(String itemId) {
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return "";
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null ? "" : item.getDescriptionId();
    }

    private static String itemDisplayText(String itemId) {
        String key = itemDisplayKey(itemId);
        return resolveText(key, itemId);
    }

    private static String entityDisplayKey(String entityTypeId) {
        ResourceLocation id = ResourceLocation.tryParse(entityTypeId);
        if (id == null) {
            return "";
        }
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(id);
        return type == null ? "" : type.getDescriptionId();
    }

    private static String entityDisplayText(String entityTypeId) {
        String key = entityDisplayKey(entityTypeId);
        return resolveText(key, entityTypeId);
    }

    private static String photoDisplayKey(String objectiveId) {
        ResourceLocation id = ResourceLocation.tryParse(objectiveId);
        if (id == null) {
            return "";
        }
        PhotoObjective objective = PhotoObjectiveRegistry.get(id);
        return objective == null ? "" : objective.translationKey();
    }

    private static String photoDisplayText(String objectiveId) {
        String key = photoDisplayKey(objectiveId);
        return resolveText(key, objectiveId);
    }

    private static String resolveText(String translationKey, String fallback) {
        if (translationKey == null || translationKey.isBlank()) {
            return fallback == null ? "" : fallback;
        }
        String text = Component.translatable(translationKey).getString();
        return text == null || text.isBlank() ? fallback : text;
    }
}
