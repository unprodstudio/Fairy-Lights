package me.paulf.fairylights.server.sound;

import me.paulf.fairylights.FairyLights;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

public final class FLSounds {
    private FLSounds() {}

    public static final SoundEvent CORD_STRETCH = create("cord.stretch");

    public static final SoundEvent CORD_CONNECT = create("cord.connect");

    public static final SoundEvent CORD_DISCONNECT = create("cord.disconnect");

    public static final SoundEvent CORD_SNAP = create("cord.snap");

    public static final SoundEvent JINGLE_BELL = create("jingle_bell");

    public static final SoundEvent FEATURE_COLOR_CHANGE = create("feature.color_change");

    public static final SoundEvent FEATURE_LIGHT_TURNON = create("feature.light_turnon");

    public static final SoundEvent FEATURE_LIGHT_TURNOFF = create("feature.light_turnoff");

    public static void register() {}

    private static SoundEvent create(final String name) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, FairyLights.id(name), SoundEvent.createVariableRangeEvent(FairyLights.id(name)));
    }
}
