package me.paulf.fairylights.client.color;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record StyledStringItemTintSource(int index) implements ItemTintSource {
    public static final MapCodec<StyledStringItemTintSource> CODEC = Codec.INT.fieldOf("index")
        .xmap(StyledStringItemTintSource::new, StyledStringItemTintSource::index);

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        if (stack.has(FLComponents.STYLED_STRING)) {
            final Component str = stack.get(FLComponents.STYLED_STRING);
            final int length = str.getString().length();
            if (length > 0) {
                TextColor lastColor = null, color = null;
                int n = (index - 1) % length;
                for (int i = 0; i < length; lastColor = color, i++) {
                    color = Utils.styleAt(str, i).getColor();
                    if (lastColor != color && (n-- == 0)) {
                        break;
                    }
                }

                if (lastColor == null)
                    return 0xFFFFFFFF;
                return ARGB.opaque(lastColor.getValue());
            }
        }

        return 0xFFFFFFFF;
    }

    @Override
    public MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
