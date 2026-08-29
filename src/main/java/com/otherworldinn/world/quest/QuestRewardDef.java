package com.otherworldinn.world.quest;

import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;

/** 任务奖励：物品（发给每位在线队员，满则原地掉落）或金币（整笔入旅社余额）。 */
public record QuestRewardDef(QuestRewardType type, @Nullable ResourceLocation itemId, int count) {

    public enum QuestRewardType {
        ITEM,
        COINS
    }
}
