package me.paulf.fairylights.server.item.components;

import com.mojang.serialization.Codec;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.string.StringType;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class FLComponents {
    public static final DataComponentType<Integer> COLOR = register("color", DataComponentType.<Integer>builder()
        .persistent(Codec.INT)
        .networkSynchronized(ByteBufCodecs.VAR_INT)
        .build()
    );

    public static final DataComponentType<List<Integer>> COLORS = register("colors", DataComponentType.<List<Integer>>builder()
            .persistent(Codec.INT.listOf())
            .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.VAR_INT))
            .build()
    );

    public static final DataComponentType<List<ItemStack>> PATTERN = register("pattern", DataComponentType.<List<ItemStack>>builder()
            .persistent(ItemStack.CODEC.listOf())
            .networkSynchronized(ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC))
            .build()
    );

    public static final DataComponentType<CompoundTag> CONNECTION_DATA = register("connection_data", DataComponentType.<CompoundTag>builder()
            .persistent(CompoundTag.CODEC)
            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
            .build()
    );

    public static final DataComponentType<StringType> STRING_TYPE = register("string_type", DataComponentType.<StringType>builder()
            .persistent(FairyLights.STRING_TYPES.byNameCodec())
            .networkSynchronized(ByteBufCodecs.registry(FairyLights.STRING_TYPES.key()))
            .build()
    );

    public static final DataComponentType<Component> STYLED_STRING = register("styled_string", DataComponentType.<Component>builder()
            .persistent(ComponentSerialization.CODEC)
            .networkSynchronized(ComponentSerialization.STREAM_CODEC)
            .build()
    );

    public static final DataComponentType<Boolean> TWINKLES = register("twinkles", DataComponentType.<Boolean>builder()
        .persistent(Codec.BOOL)
        .networkSynchronized(ByteBufCodecs.BOOL)
        .build()
    );

    public static final DataComponentType<Block> LIGHT_BLOCK = register("light_block", DataComponentType.<Block>builder()
        .persistent(BuiltInRegistries.BLOCK.byNameCodec())
        .networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK))
        .build()
    );

    private static <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, FairyLights.id(name), type);
    }

    public static void init() {
    }
}
