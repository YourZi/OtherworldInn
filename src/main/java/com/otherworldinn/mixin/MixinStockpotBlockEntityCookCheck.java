package com.otherworldinn.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.otherworldinn.init.ModGameRules;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StockpotBlockEntity.class)
public class MixinStockpotBlockEntityCookCheck {

    @Shadow
    private ItemStack result;

    @Inject(method = "takeOutProduct", at = @At("HEAD"))
    private void otherworldinn$checkRecipeUnlock(Level level, LivingEntity entity,
            ItemStack tool, CallbackInfoReturnable<Boolean> cir) {
        if (level.isClientSide) return;
        if (result.isEmpty()) return;
        if (!(entity instanceof ServerPlayer sp)) return;

        if (!level.getGameRules().getBoolean(ModGameRules.RULE_ENABLE_RECIPE_UNLOCK)) return;

        ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(result.getItem());
        String recipeId = foodId.toString();

        TeamData team = TeamManager.getInstance().getPlayerTeam(sp);
        if (team == null) return;
        if (team.isCookRecipeUnlocked(recipeId)) return;

        result = Items.SUSPICIOUS_STEW.getDefaultInstance();
    }
}
