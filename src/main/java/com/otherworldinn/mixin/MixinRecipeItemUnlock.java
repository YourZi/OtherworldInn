package com.otherworldinn.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.otherworldinn.init.ModGameRules;
import com.otherworldinn.world.team.TeamData;
import com.otherworldinn.world.team.service.TeamManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeItem.class)
public class MixinRecipeItemUnlock {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void otherworldinn$handleRecipeUnlock(UseOnContext context,
            CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        if (level.isClientSide) return;
        if (!level.getGameRules().getBoolean(ModGameRules.RULE_ENABLE_RECIPE_UNLOCK)) return;

        Player player = context.getPlayer();
        if (player == null) return;

        ItemStack stack = context.getItemInHand();
        RecipeItem.RecipeRecord record = RecipeItem.getRecipe(stack);
        if (record == null) return;

        ItemStack output = record.output();
        if (output.isEmpty()) return;

        ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(output.getItem());
        String recipeId = foodId.toString();

        TeamData team = TeamManager.getInstance().getPlayerTeam(player);
        if (team == null) return;

        if (team.isCookRecipeUnlocked(recipeId)) return;

        team.unlockCookRecipe(recipeId);
        if (player instanceof ServerPlayer sp) {
            TeamManager.getInstance().syncTeam(team, sp.getServer());
        }

        player.displayClientMessage(
                Component.translatable("message.otherworldinn.recipe_book.unlocked",
                        output.getHoverName()).withStyle(ChatFormatting.GREEN),
                true);

        cir.setReturnValue(InteractionResult.SUCCESS);
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void otherworldinn$prependUnlockedPrefix(ItemStack stack,
            CallbackInfoReturnable<Component> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!mc.level.getGameRules().getBoolean(ModGameRules.RULE_ENABLE_RECIPE_UNLOCK)) return;

        RecipeItem.RecipeRecord record = RecipeItem.getRecipe(stack);
        if (record == null) return;

        ItemStack output = record.output();
        if (output.isEmpty()) return;

        ResourceLocation foodId = BuiltInRegistries.ITEM.getKey(output.getItem());
        String recipeId = foodId.toString();

        TeamData team = TeamManager.getInstance().getPlayerTeam(mc.player);
        if (team == null) return;

        if (team.isCookRecipeUnlocked(recipeId)) return;

        cir.setReturnValue(Component.translatable(
                "message.otherworldinn.recipe_book.locked_prefix",
                cir.getReturnValue()).withStyle(ChatFormatting.RED));
    }
}
