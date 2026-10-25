package com.otherworldinn.mixin;

import com.otherworldinn.client.control.CameraHandler;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 地图正交视图下不渲染 DS 的自定义粒子。
 *
 * <p>原因：DS 的瀑布水雾/蒸汽/烟/火焰/萤火虫用的是自己的 {@code ParticleRenderType}
 * （{@code PARTICLE_SHEET_MIST/FLAME/FIREFLY}），其 {@code begin()} 会调用 {@code SoftParticles.begin(...)}
 * ——软粒子着色器需要按屏幕空间深度对场景深度纹理做比较。我们的地图模式在 {@code AFTER_SKY}
 * 之后把投影换成正交（{@link CameraHandler}），深度比较失效，粒子会被算成黑色。
 *
 * <p>这里在渲染前把这些粒子从队列里剔除（每帧一次），其它粒子完全不受影响；
 * DS 未安装时反射拿不到渲染类型，集合为空，本 mixin 不产生任何效果。
 */
@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngineMapMode {

    private static final String DS_RENDER_TYPE_CLASS =
            "org.orecruncher.dsurround.effects.particles.DSurroundParticleRenderType";

    @Shadow private Map<ParticleRenderType, Queue<Particle>> particles;

    private static Set<ParticleRenderType> otherworldinn$dsRenderTypes;

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
            at = @At("HEAD"),
            require = 0)
    private void otherworldinn$dropDsParticles(
            LightTexture lightTexture, Camera camera, float partialTick, CallbackInfo callback) {
        otherworldinn$purge();
    }

    @Inject(
            method = "render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",
            at = @At("HEAD"),
            require = 0)
    private void otherworldinn$dropDsParticlesCulled(
            LightTexture lightTexture,
            Camera camera,
            float partialTick,
            net.minecraft.client.renderer.culling.Frustum frustum,
            java.util.function.Predicate<ParticleRenderType> predicate,
            CallbackInfo callback) {
        otherworldinn$purge();
    }

    private void otherworldinn$purge() {
        if (!CameraHandler.isMapMode()) {
            return;
        }
        Set<ParticleRenderType> excluded = otherworldinn$dsRenderTypes();
        if (excluded.isEmpty()) {
            return;
        }
        for (Queue<Particle> queue : this.particles.values()) {
            queue.removeIf(particle -> excluded.contains(particle.getRenderType()));
        }
    }

    private static Set<ParticleRenderType> otherworldinn$dsRenderTypes() {
        if (otherworldinn$dsRenderTypes == null) {
            Set<ParticleRenderType> types = new HashSet<>();
            try {
                Class<?> holder = Class.forName(DS_RENDER_TYPE_CLASS);
                for (String field : new String[] {
                    "PARTICLE_SHEET_MIST", "PARTICLE_SHEET_FLAME", "PARTICLE_SHEET_FIREFLY"
                }) {
                    Object value = holder.getField(field).get(null);
                    if (value instanceof ParticleRenderType type) {
                        types.add(type);
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // DS 未安装或版本变化：保持空集合，功能自动失效
            }
            otherworldinn$dsRenderTypes = types;
        }
        return otherworldinn$dsRenderTypes;
    }
}
