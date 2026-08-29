package com.otherworldinn.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface MixinGameRendererAccessor {

    @Invoker("getFov")
    double otherworldinn$invokeGetFov(Camera camera, float partialTick, boolean useFovSetting);
}
