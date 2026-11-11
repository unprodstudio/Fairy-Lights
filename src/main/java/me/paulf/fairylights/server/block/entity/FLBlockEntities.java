package me.paulf.fairylights.server.block.entity;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.block.FLBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public final class FLBlockEntities {
    private FLBlockEntities() {}

    public static final BlockEntityType<FastenerBlockEntity> FASTENER = register("fastener", () -> FabricBlockEntityTypeBuilder.create(FastenerBlockEntity::new, FLBlocks.FASTENER).build(null));

    public static final BlockEntityType<LightBlockEntity> LIGHT = register("light", () -> FabricBlockEntityTypeBuilder.create(LightBlockEntity::new,
        FLBlocks.FAIRY_LIGHT,
        FLBlocks.PAPER_LANTERN,
        FLBlocks.ORB_LANTERN,
        FLBlocks.FLOWER_LIGHT,
        FLBlocks.CANDLE_LANTERN_LIGHT,
        FLBlocks.OIL_LANTERN_LIGHT,
        FLBlocks.JACK_O_LANTERN,
        FLBlocks.SKULL_LIGHT,
        FLBlocks.GHOST_LIGHT,
        FLBlocks.SPIDER_LIGHT,
        FLBlocks.WITCH_LIGHT,
        FLBlocks.SNOWFLAKE_LIGHT,
        FLBlocks.HEART_LIGHT,
        FLBlocks.MOON_LIGHT,
        FLBlocks.STAR_LIGHT,
        FLBlocks.ICICLE_LIGHTS,
        FLBlocks.METEOR_LIGHT,
        FLBlocks.OIL_LANTERN,
        FLBlocks.CANDLE_LANTERN,
        FLBlocks.INCANDESCENT_LIGHT
    ).build(null));

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, Supplier<BlockEntityType<T>> register) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, FairyLights.id(name), register.get());
    }

    public static void register() {}
}
