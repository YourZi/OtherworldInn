package com.otherworldinn.world.dialogue;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public record DialogueEffectDef(
        DialogueEffectType type,
        @Nullable ResourceLocation itemId,
        int count,
        @Nullable String storyFlag,
        int stageValue,
        int minDays,
        int maxDays,
        @Nullable String questId) {
    public static DialogueEffectDef takeItem(ResourceLocation itemId, int count) {
        return new DialogueEffectDef(
                DialogueEffectType.TAKE_ITEM,
                itemId,
                Math.max(1, count),
                null,
                0,
                0,
                0,
                null);
    }

    public static DialogueEffectDef giveItem(ResourceLocation itemId, int count) {
        return new DialogueEffectDef(
                DialogueEffectType.GIVE_ITEM,
                itemId,
                Math.max(1, count),
                null,
                0,
                0,
                0,
                null);
    }

    public static DialogueEffectDef giveCoins(int count) {
        return new DialogueEffectDef(
                DialogueEffectType.GIVE_COINS,
                null,
                Math.max(1, count),
                null,
                0,
                0,
                0,
                null);
    }

    public static DialogueEffectDef setStoryFlag(String storyFlag) {
        return new DialogueEffectDef(
                DialogueEffectType.SET_STORY_FLAG, null, 0, storyFlag, 0, 0, 0, null);
    }

    public static DialogueEffectDef advanceStoryStage(int stageValue) {
        return new DialogueEffectDef(
                DialogueEffectType.ADVANCE_STORY_STAGE, null, 0, null, stageValue, 0, 0, null);
    }

    public static DialogueEffectDef setNextVisitRange(int minDays, int maxDays) {
        return new DialogueEffectDef(
                DialogueEffectType.SET_NEXT_VISIT_RANGE,
                null,
                0,
                null,
                0,
                Math.max(0, minDays),
                Math.max(0, maxDays),
                null);
    }

    /** 点选该对话选项即接取任务（幂等：已接取/已完成的任务不会重复接取）。 */
    public static DialogueEffectDef acceptQuest(String questId) {
        return new DialogueEffectDef(
                DialogueEffectType.ACCEPT_QUEST, null, 0, null, 0, 0, 0, questId);
    }
}
