package com.otherworldinn.mixin;

import com.otherworldinn.world.husbandry.HusbandryService;
import net.lopymine.patpat.packet.PatPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 把 PatPat 的摸摸动作接到好感系统。
 *
 * <p>PatPat 不是纯客户端：客户端按 Shift+右键后发 C2S 摸摸包，服务端
 * {@code PatPatServerPacketManager#handlePacket} 解析并广播给周围玩家——所以我们在这里挂点，
 * 一次服务端认定的"真的摸到了"对应一次抚摸好感（每世界日一次）。
 *
 * <p>PatPat 是可选前置：{@link Pseudo} 让目标类不存在时跳过本 mixin；
 * {@code require = 0} 让它的版本变化导致方法匹配失败时只记日志、不崩游戏。
 */
@Pseudo
@Mixin(targets = "net.lopymine.patpat.server.packet.PatPatServerPacketManager", remap = false)
public abstract class MixinPatPatServerPacketManager {

    @Inject(method = "handlePacket", at = @At("TAIL"), require = 0, remap = false)
    private static void otherworldinn$onPat(
            ServerPlayer player, PatPacket<ServerLevel, ?> packet, CallbackInfo callback) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }
        Entity patted = packet.getPattedEntity(level);
        if (patted instanceof Animal animal) {
            HusbandryService.tryPet(animal, level);
        }
    }
}
