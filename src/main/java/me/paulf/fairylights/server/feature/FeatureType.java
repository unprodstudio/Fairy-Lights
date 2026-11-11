package me.paulf.fairylights.server.feature;

import com.mojang.serialization.Lifecycle;
import io.netty.buffer.ByteBuf;
import me.paulf.fairylights.FairyLights;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public final class FeatureType {
    private static final DefaultedRegistry<FeatureType> REGISTRY = new DefaultedMappedRegistry<>(
        "default",
        ResourceKey.createRegistryKey(FairyLights.id("feature")),
        Lifecycle.experimental(),
        false
    );

    public static final StreamCodec<ByteBuf, FeatureType> STREAM_CODEC = ByteBufCodecs.idMapper(REGISTRY);

    public static final FeatureType DEFAULT = register("default");

    private FeatureType() {}

    public int getId() {
        return REGISTRY.getId(this);
    }

    public static FeatureType register(final String name) {
        return Registry.register(REGISTRY, Identifier.parse(name), new FeatureType());
    }

    public static FeatureType fromId(final int id) {
        return REGISTRY.byId(id);
    }
}
