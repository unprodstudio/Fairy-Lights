package me.paulf.fairylights.server.block;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.LightVariant;
import me.paulf.fairylights.server.item.SimpleLightVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class FLBlocks {
    private FLBlocks() {}

    public static final FastenerBlock FASTENER = register("fastener", key -> new FastenerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN).setId(key)));

    public static final LightBlock FAIRY_LIGHT = register("fairy_light", FLBlocks.createLight(SimpleLightVariant.FAIRY_LIGHT));

    public static final LightBlock PAPER_LANTERN = register("paper_lantern", FLBlocks.createLight(SimpleLightVariant.PAPER_LANTERN));

    public static final LightBlock ORB_LANTERN = register("orb_lantern", FLBlocks.createLight(SimpleLightVariant.ORB_LANTERN));

    public static final LightBlock FLOWER_LIGHT = register("flower_light", FLBlocks.createLight(SimpleLightVariant.FLOWER_LIGHT));

    public static final LightBlock CANDLE_LANTERN_LIGHT = register("candle_lantern_light", FLBlocks.createLight(SimpleLightVariant.CANDLE_LANTERN_LIGHT));

    public static final LightBlock OIL_LANTERN_LIGHT = register("oil_lantern_light", FLBlocks.createLight(SimpleLightVariant.OIL_LANTERN_LIGHT));

    public static final LightBlock JACK_O_LANTERN = register("jack_o_lantern", FLBlocks.createLight(SimpleLightVariant.JACK_O_LANTERN));

    public static final LightBlock SKULL_LIGHT = register("skull_light", FLBlocks.createLight(SimpleLightVariant.SKULL_LIGHT));

    public static final LightBlock GHOST_LIGHT = register("ghost_light", FLBlocks.createLight(SimpleLightVariant.GHOST_LIGHT));

    public static final LightBlock SPIDER_LIGHT = register("spider_light", FLBlocks.createLight(SimpleLightVariant.SPIDER_LIGHT));

    public static final LightBlock WITCH_LIGHT = register("witch_light", FLBlocks.createLight(SimpleLightVariant.WITCH_LIGHT));

    public static final LightBlock SNOWFLAKE_LIGHT = register("snowflake_light", FLBlocks.createLight(SimpleLightVariant.SNOWFLAKE_LIGHT));

    public static final LightBlock HEART_LIGHT = register("heart_light", FLBlocks.createLight(SimpleLightVariant.HEART_LIGHT));

    public static final LightBlock MOON_LIGHT = register("moon_light", FLBlocks.createLight(SimpleLightVariant.MOON_LIGHT));

    public static final LightBlock STAR_LIGHT = register("star_light", FLBlocks.createLight(SimpleLightVariant.STAR_LIGHT));

    public static final LightBlock ICICLE_LIGHTS = register("icicle_lights", FLBlocks.createLight(SimpleLightVariant.ICICLE_LIGHTS));

    public static final LightBlock METEOR_LIGHT = register("meteor_light", FLBlocks.createLight(SimpleLightVariant.METEOR_LIGHT));

    public static final LightBlock OIL_LANTERN = register("oil_lantern", FLBlocks.createLight(SimpleLightVariant.OIL_LANTERN));

    public static final LightBlock CANDLE_LANTERN = register("candle_lantern", FLBlocks.createLight(SimpleLightVariant.CANDLE_LANTERN));

    public static final LightBlock INCANDESCENT_LIGHT = register("incandescent_light", FLBlocks.createLight(SimpleLightVariant.INCANDESCENT_LIGHT));

    private static <T extends Block> T register(String name, Function<ResourceKey<Block>, T> register) {
        var key = ResourceKey.create(Registries.BLOCK, FairyLights.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, register.apply(key));
    }

    public static void register() {}

    private static Function<ResourceKey<Block>, LightBlock> createLight(final LightVariant<?> variant) {
        return createLight(variant, LightBlock::new);
    }

    private static Function<ResourceKey<Block>, LightBlock> createLight(final LightVariant<?> variant, final BiFunction<Block.Properties, LightVariant<?>, LightBlock> factory) {
        return key -> factory.apply(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).forceSolidOn().strength(3.5F).sound(SoundType.LANTERN).lightLevel(state -> state.getValue(LightBlock.LIT) ? 15 : 0).noCollision().setId(key), variant);
    }
}
