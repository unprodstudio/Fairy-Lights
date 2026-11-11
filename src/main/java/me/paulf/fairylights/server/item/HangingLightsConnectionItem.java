package me.paulf.fairylights.server.item;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.string.StringType;
import me.paulf.fairylights.util.RegistryObjects;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class HangingLightsConnectionItem extends ConnectionItem {
    public HangingLightsConnectionItem(final Properties properties) {
        super(properties, ConnectionTypes.HANGING_LIGHTS);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        if (stack.has(FLComponents.STRING_TYPE)) {
            final Identifier name = RegistryObjects.getName(FairyLights.STRING_TYPES, stack.get(FLComponents.STRING_TYPE));
            tooltip.accept(Component.translatable(name.toLanguageKey("item")).withStyle(ChatFormatting.GRAY));
        }
        if (stack.has(FLComponents.PATTERN)) {
            var patternStacks = stack.get(FLComponents.PATTERN);
            if (!patternStacks.isEmpty()) {
                tooltip.accept(Component.empty());
            }
            for (ItemStack lightStack : patternStacks) {
                tooltip.accept(lightStack.getHoverName());
                lightStack.getItem().appendHoverText(lightStack, context, display, tooltip, tooltipFlag);
            }
        }
    }
}
