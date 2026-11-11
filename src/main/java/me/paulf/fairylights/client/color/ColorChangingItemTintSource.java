package me.paulf.fairylights.client.color;

import com.mojang.serialization.MapCodec;
import me.paulf.fairylights.server.feature.light.ColorChangingBehavior;
import me.paulf.fairylights.server.item.DyeableItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record ColorChangingItemTintSource() implements ItemTintSource {
    public static final MapCodec<ColorChangingItemTintSource> CODEC = MapCodec.unit(ColorChangingItemTintSource::new);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (ColorChangingBehavior.exists(stack)) {
            return ColorChangingBehavior.animate(stack);
        }

        return DyeableItem.getColor(stack);
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
