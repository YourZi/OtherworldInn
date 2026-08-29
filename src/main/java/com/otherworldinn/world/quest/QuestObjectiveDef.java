package com.otherworldinn.world.quest;

import com.otherworldinn.world.dialogue.LocalizedText;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 单个任务目标定义（JSON 解析产物）。
 *
 * <p>字段按 {@link #type} 取用，互斥字段组由 {@link QuestRegistry} 在加载时校验：
 * interact_entity 的 entityType / entityUuid 二选一；interact_block 的 block / pos 二选一。
 */
public record QuestObjectiveDef(
        String id,
        QuestObjectiveType type,
        @Nullable LocalizedText label,
        @Nullable ResourceLocation itemId,
        int count,
        @Nullable ResourceLocation entityType,
        @Nullable UUID entityUuid,
        @Nullable ResourceLocation blockId,
        @Nullable TagKey<Block> blockTag,
        @Nullable BlockPos blockPos,
        @Nullable ResourceLocation advancementId,
        @Nullable BlockPos regionMin,
        @Nullable BlockPos regionMax,
        boolean relative) {

    public boolean matchesBlock(BlockState state, BlockPos pos) {
        if (type != QuestObjectiveType.INTERACT_BLOCK) {
            return false;
        }
        if (blockId != null) {
            return blockId.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
        }
        if (blockTag != null) {
            return state.is(blockTag);
        }
        return blockPos != null && blockPos.equals(pos);
    }

    public boolean matchesEntity(@Nullable ResourceLocation actualEntityType, @Nullable UUID actualEntityUuid) {
        if (type != QuestObjectiveType.INTERACT_ENTITY) {
            return false;
        }
        if (entityUuid != null) {
            return entityUuid.equals(actualEntityUuid);
        }
        return entityType != null && entityType.equals(actualEntityType);
    }
}
