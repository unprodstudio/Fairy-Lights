package me.paulf.fairylights.client.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.feature.light.ColorChangingBehavior;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.components.FLComponents;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record PatternItemTintSource(int index) implements ItemTintSource {
    public static final MapCodec<PatternItemTintSource> CODEC = Codec.INT.fieldOf("index")
        .xmap(PatternItemTintSource::new, PatternItemTintSource::index);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (stack.has(FLComponents.PATTERN)) {
            var pattern = stack.get(FLComponents.PATTERN);
            if (!pattern.isEmpty()) {
                var item = pattern.get((index - 1) % pattern.size());
                if (ColorChangingBehavior.exists(item)) {
                    return ColorChangingBehavior.animate(item);
                }
                return DyeableItem.getColor(item);
            }
        }

        return 0xFFFFFFFF;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
