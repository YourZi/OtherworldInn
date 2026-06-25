package com.otherworldinn.compat.exposure;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.world.commission.CommissionService;
import com.otherworldinn.world.photo.PhotoObjective;
import com.otherworldinn.world.photo.PhotoObjectiveRegistry;
import com.otherworldinn.world.photo.StoryGuestPhotoTask;
import com.otherworldinn.world.photo.StoryGuestPhotoTaskRegistry;
import com.otherworldinn.world.storyguest.StoryGuestDefinition;
import com.otherworldinn.world.storyguest.StoryGuestService;
import io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;

public final class ExposurePhotoTaskService {
    private ExposurePhotoTaskService() {}

    @SubscribeEvent
    public static void onFrameAdded(FrameAddedEvent event) {
        Entity source = event.getCameraHolderEntity();
        if (!(source instanceof ServerPlayer player)) {
            return;
        }

        Frame frame = event.getFrame();
        if (frame == null) {
            return;
        }

        tryCompleteCommissionPhotoObjectives(player, frame);
        tryCompleteStoryPhotoTasks(player, frame);
    }

    private static void tryCompleteCommissionPhotoObjectives(ServerPlayer player, Frame frame) {
        for (var objectiveId : CommissionService.getActivePhotoObjectives(player)) {
            PhotoObjective objective = PhotoObjectiveRegistry.get(objectiveId);
            if (objective == null) {
                continue;
            }
            if (ExposurePhotoMatcher.matches(objective, frame)) {
                CommissionService.onTeamMemberPhotoObjectiveMatched(player, objective.id());
            }
        }
    }

    private static void tryCompleteStoryPhotoTasks(ServerPlayer player, Frame frame) {
        ServerLevel level = player.serverLevel();
        for (StoryGuestPhotoTask task : StoryGuestPhotoTaskRegistry.all()) {
            if (StoryGuestService.getStoryStage(level, task.storyGuestId()) != task.requiredStage()) {
                continue;
            }
            if (!StoryGuestService.hasStoryFlag(level, task.storyGuestId(), task.activationFlag())) {
                continue;
            }
            if (StoryGuestService.hasStoryFlag(level, task.storyGuestId(), task.completionFlag())) {
                continue;
            }

            PhotoObjective objective = PhotoObjectiveRegistry.get(task.objectiveId());
            if (!ExposurePhotoMatcher.matches(objective, frame)) {
                continue;
            }

            if (!StoryGuestService.addStoryFlag(level, task.storyGuestId(), task.completionFlag())) {
                continue;
            }

            StoryGuestDefinition definition = StoryGuestService.getDefinition(task.storyGuestId());
            Component guestName =
                    definition == null
                            ? Component.literal(task.storyGuestId())
                            : Component.translatable(definition.nameKey());
            player.sendSystemMessage(
                    Component.translatable(
                            "message.otherworldinn.story.photo_recorded",
                            guestName,
                            PhotoObjectiveRegistry.getDisplayName(task.objectiveId())));
            OtherworldInn.LOGGER.info(
                    "Player {} completed story photo objective {} for {}",
                    player.getGameProfile().getName(),
                    task.objectiveId(),
                    task.storyGuestId());
        }
    }
}
