package me.paulf.fairylights.server.item;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.LightBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FLItems {
    private FLItems() {}

    private static final List<LightItem> LIGHTS = new ArrayList<>();

    public static final ConnectionItem HANGING_LIGHTS = register("hanging_lights", key -> new HangingLightsConnectionItem(defaultProperties(key)));

    public static final ConnectionItem PENNANT_BUNTING = register("pennant_bunting", key -> new PennantBuntingConnectionItem(defaultProperties(key)));

    public static final ConnectionItem TINSEL = register("tinsel", key -> new TinselConnectionItem(defaultProperties(key)));

    public static final ConnectionItem LETTER_BUNTING = register("letter_bunting", key -> new LetterBuntingConnectionItem(defaultProperties(key)));

    public static final ConnectionItem GARLAND = register("garland", key -> new GarlandConnectionItem(defaultProperties(key)));

    public static final LightItem FAIRY_LIGHT = register("fairy_light", FLItems.createColorLight(FLBlocks.FAIRY_LIGHT));

    public static final LightItem PAPER_LANTERN = register("paper_lantern", FLItems.createColorLight(FLBlocks.PAPER_LANTERN));

    public static final LightItem ORB_LANTERN = register("orb_lantern", FLItems.createColorLight(FLBlocks.ORB_LANTERN));

    public static final LightItem FLOWER_LIGHT = register("flower_light", FLItems.createColorLight(FLBlocks.FLOWER_LIGHT));

    public static final LightItem CANDLE_LANTERN_LIGHT = register("candle_lantern_light", FLItems.createColorLight(FLBlocks.CANDLE_LANTERN_LIGHT));

    public static final LightItem OIL_LANTERN_LIGHT = register("oil_lantern_light", FLItems.createColorLight(FLBlocks.OIL_LANTERN_LIGHT));

    public static final LightItem JACK_O_LANTERN = register("jack_o_lantern", FLItems.createColorLight(FLBlocks.JACK_O_LANTERN));

    public static final LightItem SKULL_LIGHT = register("skull_light", FLItems.createColorLight(FLBlocks.SKULL_LIGHT));

    public static final LightItem GHOST_LIGHT = register("ghost_light", FLItems.createColorLight(FLBlocks.GHOST_LIGHT));

    public static final LightItem SPIDER_LIGHT = register("spider_light", FLItems.createColorLight(FLBlocks.SPIDER_LIGHT));

    public static final LightItem WITCH_LIGHT = register("witch_light", FLItems.createColorLight(FLBlocks.WITCH_LIGHT));

    public static final LightItem SNOWFLAKE_LIGHT = register("snowflake_light", FLItems.createColorLight(FLBlocks.SNOWFLAKE_LIGHT));

    public static final LightItem HEART_LIGHT = register("heart_light", FLItems.createColorLight(FLBlocks.HEART_LIGHT));

    public static final LightItem MOON_LIGHT = register("moon_light", FLItems.createColorLight(FLBlocks.MOON_LIGHT));

    public static final LightItem STAR_LIGHT = register("star_light", FLItems.createColorLight(FLBlocks.STAR_LIGHT));

    public static final LightItem ICICLE_LIGHTS = register("icicle_lights", FLItems.createColorLight(FLBlocks.ICICLE_LIGHTS));

    public static final LightItem METEOR_LIGHT = register("meteor_light", FLItems.createColorLight(FLBlocks.METEOR_LIGHT));

    public static final LightItem OIL_LANTERN = register("oil_lantern", FLItems.createLight(FLBlocks.OIL_LANTERN, LightItem::new));

    public static final LightItem CANDLE_LANTERN = register("candle_lantern", FLItems.createLight(FLBlocks.CANDLE_LANTERN, LightItem::new));

    public static final LightItem INCANDESCENT_LIGHT = register("incandescent_light", FLItems.createLight(FLBlocks.INCANDESCENT_LIGHT, LightItem::new));

    public static final Item TRIANGLE_PENNANT = register("triangle_pennant", key -> new PennantItem(defaultProperties(key)));

    public static final Item SPEARHEAD_PENNANT = register("spearhead_pennant", key -> new PennantItem(defaultProperties(key)));

    public static final Item SWALLOWTAIL_PENNANT = register("swallowtail_pennant", key -> new PennantItem(defaultProperties(key)));

    public static final Item SQUARE_PENNANT = register("square_pennant", key -> new PennantItem(defaultProperties(key)));

    private static Item.Properties defaultProperties(ResourceKey<Item> key) {
        return new Item.Properties().setId(key);
    }

    private static <T extends Item> T register(String name, Function<ResourceKey<Item>, T> register) {
        var key = ResourceKey.create(Registries.ITEM, FairyLights.id(name));
        var item = Registry.register(BuiltInRegistries.ITEM, key, register.apply(key));

        if (item instanceof LightItem lightItem)
            LIGHTS.add(lightItem);

        return item;
    }

    public static void register() {}

    private static Function<ResourceKey<Item>, LightItem> createLight(final LightBlock block, final BiFunction<LightBlock, Item.Properties, LightItem> factory) {
        return key -> factory.apply(block, defaultProperties(key).stacksTo(16));
    }

    private static Function<ResourceKey<Item>, LightItem> createColorLight(final LightBlock block) {
        return createLight(block, ColorLightItem::new);
    }

    public static Stream<LightItem> lights() {
        return LIGHTS.stream();
    }
}
