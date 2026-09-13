package com.otherworldinn.world.event.listener;

import com.otherworldinn.OtherworldInn;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** 铁砧免疫漂浮：背包内持有铁砧类物品时，免疫原版漂浮效果。 */
@EventBusSubscriber(modid = OtherworldInn.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AnvilLevitationHandler {

    private AnvilLevitationHandler() {}

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (!event.getEffectInstance().is(MobEffects.LEVITATION)) {
            return;
        }
        if (event.getEntity() instanceof Player player
                && player.getInventory().contains(ItemTags.ANVIL)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!player.hasEffect(MobEffects.LEVITATION)) {
            return;
        }
        if (player.getInventory().contains(ItemTags.ANVIL)) {
            player.removeEffect(MobEffects.LEVITATION);
        }
    }
}
