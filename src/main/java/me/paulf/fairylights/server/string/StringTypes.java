package me.paulf.fairylights.server.string;

import me.paulf.fairylights.FairyLights;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class StringTypes {
    private StringTypes() {}

    public static final StringType BLACK_STRING = register("black_string", () -> new StringType(0x323232));

    public static final StringType WHITE_STRING = register("white_string", () -> new StringType(0xF0F0F0));

    public static void register() {}

    private static StringType register(String name, Supplier<StringType> register) {
        return Registry.register(FairyLights.STRING_TYPES, FairyLights.id(name), register.get());
    }
}
