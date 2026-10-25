package com.otherworldinn.client.event.listener;

import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.gui.overlay.HusbandryHeartsOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** 客户端：右键任意动物时刷新好感度心形 HUD，并补上原版不会给的挥臂动画。 */
@EventBusSubscriber(modid = OtherworldInn.MODID, value = Dist.CLIENT)
public final class HusbandryHeartsClientHandler {
    private HusbandryHeartsClientHandler() {}

    /**
     * 空手、不潜行时，原版是否会自己挥手（即 {@code mobInteract} 返回 {@code shouldSwing()} 的结果）。
     *
     * <p>按 1.21.1 原版代码判定：已驯服的狼/猫空手点击会切换坐下（返回 SUCCESS），
     * 上鞍且没被骑的猪会让人骑上去（返回 SUCCESS）；其余动物的 {@code Animal#mobInteract}
     * 在空手时都返回 PASS，不会挥手，所以由我们补。
     */
    private static boolean vanillaSwingsOnEmptyHand(Animal animal) {
        if (animal instanceof TamableAnimal tamable && tamable.isTame()) {
            return true;
        }
        return animal instanceof Pig pig && pig.isSaddled() && !pig.isVehicle();
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        if (event.getEntity() != Minecraft.getInstance().player) {
            return;
        }
        if (!(event.getTarget() instanceof Animal animal)) {
            return;
        }
        HusbandryHeartsOverlay.show(animal);

        // 空手右键：原版多数动物没有交互返回值，不会挥手，这里补一次。
        // 原版自己会挥的情况（已驯服的狼/猫切换坐下、上鞍的猪被骑）不补，避免挥两次。
        if (event.getItemStack().isEmpty()
                && event.getEntity() instanceof LocalPlayer player
                && !vanillaSwingsOnEmptyHand(animal)
                && player.swingTime <= 0) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
