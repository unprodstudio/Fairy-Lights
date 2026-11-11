package me.paulf.fairylights.server.sound;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.paulf.fairylights.FairyLights;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class FLSounds {
    private FLSounds() {}

    public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(FairyLights.ID, Registries.SOUND_EVENT);

    public static final RegistrySupplier<SoundEvent> CORD_STRETCH = create("cord.stretch");

    public static final RegistrySupplier<SoundEvent> CORD_CONNECT = create("cord.connect");

    public static final RegistrySupplier<SoundEvent> CORD_DISCONNECT = create("cord.disconnect");

    public static final RegistrySupplier<SoundEvent> CORD_SNAP = create("cord.snap");

    public static final RegistrySupplier<SoundEvent> JINGLE_BELL = create("jingle_bell");

    public static final RegistrySupplier<SoundEvent> FEATURE_COLOR_CHANGE = create("feature.color_change");

    public static final RegistrySupplier<SoundEvent> FEATURE_LIGHT_TURNON = create("feature.light_turnon");

    public static final RegistrySupplier<SoundEvent> FEATURE_LIGHT_TURNOFF = create("feature.light_turnoff");

    private static RegistrySupplier<SoundEvent> create(final String name) {
        return REG.register(name, () -> SoundEvent.createVariableRangeEvent(FairyLights.id(name)));
    }
}
