package com.otherworldinn.mixin;

import com.evandev.fieldguide.api.Category;
import com.evandev.fieldguide.server.ServerFieldGuideManager;
import com.evandev.fieldguide.server.progress.FieldGuideProgressManager;
import com.evandev.fieldguide.server.progress.PlayerFieldGuideProgress;
import com.otherworldinn.util.AdvancementUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(
        targets = "com.evandev.fieldguide.server.progress.FieldGuideTriggers$CategoryCompletedTrigger",
        remap = false)
public abstract class MixinFieldGuideCategoryComplete {

    @Inject(method = "trigger", at = @At("TAIL"), remap = false)
    private void onCategoryCompleted(ServerPlayer player, ResourceLocation categoryId, CallbackInfo ci) {
        if (player == null || categoryId == null) {
            return;
        }
        try {
            if (areAllGuideEntriesUnlocked(player)) {
                AdvancementUtils.award(player, AdvancementUtils.FIELD_GUIDE_COMPLETE);
            }
        } catch (Exception ignored) {
            // Field Guide not loaded or API changed — silently ignore
        }
    }

    private static boolean areAllGuideEntriesUnlocked(ServerPlayer player) {
        ServerFieldGuideManager manager = ServerFieldGuideManager.getInstance();
        if (manager == null) {
            return false;
        }
        var categories = manager.getCategories();
        if (categories == null || categories.isEmpty()) {
            return false;
        }
        PlayerFieldGuideProgress progress =
                FieldGuideProgressManager.getInstance().getProgress(player);
        if (progress == null) {
            return false;
        }
        for (Category category : categories.values()) {
            if (category == null) {
                continue;
            }
            var entryIds = category.getEntryIds();
            if (entryIds == null) {
                continue;
            }
            for (ResourceLocation entryId : entryIds) {
                if (!progress.isUnlocked(entryId)) {
                    return false;
                }
            }
        }
        return true;
    }
}
