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

public record PatternOrDefaultItemTintSource(int index) implements ItemTintSource {
    public static final MapCodec<PatternOrDefaultItemTintSource> CODEC = Codec.INT.fieldOf("index")
        .xmap(PatternOrDefaultItemTintSource::new, PatternOrDefaultItemTintSource::index);

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

        if (FairyLights.CHRISTMAS.isOccurringNow()) {
            return (index + Util.getMillis() / 2000) % 2 == 0 ? 0xFF993333 : 0xFF7FCC19;
        }

        if (FairyLights.HALLOWEEN.isOccurringNow()) {
            return index % 2 == 0 ? 0xFFf9801d : 0xFF8932b8;
        }

        return 0xFFFFD584;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
