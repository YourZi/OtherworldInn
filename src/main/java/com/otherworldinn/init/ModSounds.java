package com.otherworldinn.init;

import com.otherworldinn.OtherworldInn;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, OtherworldInn.MODID);

    public static final Supplier<SoundEvent> PAYMENT = registerSoundEvent("payment");
    public static final Supplier<SoundEvent> COIN_PROJECTILE = registerSoundEvent("coin_projectile");
    /** Outer Wilds 唱片的音频事件，供 jukebox_song 数据引用。 */
    public static final Supplier<SoundEvent> OUTER_WILDS = registerSoundEvent("outer_wilds");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
