package me.paulf.fairylights.server.connection;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.FLItems;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class ConnectionTypes {
    private ConnectionTypes() {}

    public static final ConnectionType<HangingLightsConnection> HANGING_LIGHTS = register("hanging_lights",
        () -> ConnectionType.Builder.create(HangingLightsConnection::new).item(() -> FLItems.HANGING_LIGHTS).build()
    );

    public static final ConnectionType<GarlandVineConnection> VINE_GARLAND = register("vine_garland",
        () -> ConnectionType.Builder.create(GarlandVineConnection::new).item(() -> FLItems.GARLAND).build()
    );

    public static final ConnectionType<GarlandTinselConnection> TINSEL_GARLAND = register("tinsel_garland",
        () -> ConnectionType.Builder.create(GarlandTinselConnection::new).item(() -> FLItems.TINSEL).build()
    );

    public static final ConnectionType<PennantBuntingConnection> PENNANT_BUNTING = register("pennant_bunting",
        () -> ConnectionType.Builder.create(PennantBuntingConnection::new).item(() -> FLItems.PENNANT_BUNTING).build()
    );

    public static final ConnectionType<LetterBuntingConnection> LETTER_BUNTING = register("letter_bunting",
        () -> ConnectionType.Builder.create(LetterBuntingConnection::new).item(() -> FLItems.LETTER_BUNTING).build()
    );

    public static void register() {}

    private static <T extends Connection> ConnectionType<T> register(String name, Supplier<ConnectionType<T>> register) {
        return Registry.register(FairyLights.CONNECTION_TYPES, FairyLights.id(name), register.get());
    }
}
