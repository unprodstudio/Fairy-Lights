package me.paulf.fairylights.server.entity;

import me.paulf.fairylights.FairyLights;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class FLEntityDataSerializers {
    public static final EntityDataSerializer<Optional<CompoundTag>> COMPOUND_TAG = EntityDataSerializer.forValueType(ByteBufCodecs.OPTIONAL_COMPOUND_TAG);

    public static void init() {
        FabricEntityDataRegistry.register(FairyLights.id("compound_tag"), COMPOUND_TAG);
    }
}
