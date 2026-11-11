package me.paulf.fairylights.server.connection;

import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.components.FLComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public final class GarlandTinselConnection extends Connection {
    private int color;

    public GarlandTinselConnection(final ConnectionType<? extends GarlandTinselConnection> type, final Level world, final Fastener<?> fastener, final UUID uuid) {
        super(type, world, fastener, uuid);
        this.color = DyeableItem.getColor(DyeColor.LIGHT_GRAY);
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public float getRadius() {
        return 0.125F;
    }

    @Override
    public void serializeLogic(ValueOutput output) {
        super.serializeLogic(output);
        DyeableItem.setColor(output, this.color);
    }

    @Override
    public DataComponentMap serializeItem() {
        return DataComponentMap.composite(super.serializeItem(),
            DataComponentMap.builder()
                .set(FLComponents.COLOR, this.color)
                .build()
        );
    }

    @Override
    public void deserializeLogic(final ValueInput input) {
        super.deserializeLogic(input);
        this.color = DyeableItem.getColor(input);
    }

    @Override
    public void deserializeLogic(DataComponentMap components) {
        super.deserializeLogic(components);
        this.color = components.getOrDefault(FLComponents.COLOR, 0xFFFFFFFF);
    }
}
