package com.otherworldinn.client.ponder;

import com.otherworldinn.OtherworldInn;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class OtherworldInnPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return OtherworldInn.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        OtherworldInnPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        OtherworldInnPonderTags.register(helper);
    }
}
