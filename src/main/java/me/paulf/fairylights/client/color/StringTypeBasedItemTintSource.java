package me.paulf.fairylights.client.color;

import com.mojang.serialization.MapCodec;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.string.StringTypes;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record StringTypeBasedItemTintSource() implements ItemTintSource {
    public static final MapCodec<StringTypeBasedItemTintSource> CODEC = MapCodec.unit(StringTypeBasedItemTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (stack.has(FLComponents.STRING_TYPE)) {
            return 0xFF000000 | stack.get(FLComponents.STRING_TYPE).getColor();
        }

        return 0xFF000000 | StringTypes.BLACK_STRING.getColor();
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
