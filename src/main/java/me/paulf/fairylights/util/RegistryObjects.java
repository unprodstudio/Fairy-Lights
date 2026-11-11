package me.paulf.fairylights.util;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public final class RegistryObjects {
    private RegistryObjects() {}

    public static <T> Identifier getName(@NonNull Registry<T> registry, final T type) {
        final Identifier name = registry.getKey(type);
        if (name == null) {
            throw new NullPointerException("Missing registry name: " + type);
        }
        return name;
    }
}
