package me.paulf.fairylights.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface NBTSerializable {
    void serialize(ValueOutput output);

    void deserialize(ValueInput input);
}
