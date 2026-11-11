package me.paulf.fairylights.server.item;

import jdk.jshell.execution.Util;
import me.paulf.fairylights.server.connection.ConnectionTypes;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.function.Consumer;

public class LetterBuntingConnectionItem extends ConnectionItem {
    public LetterBuntingConnectionItem(final Item.Properties properties) {
        super(properties, ConnectionTypes.LETTER_BUNTING);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (stack.has(FLComponents.STYLED_STRING)) {
            final Component s = stack.get(FLComponents.STYLED_STRING);
            if (!Utils.isEmpty(s)) {
                builder.accept(Component.translatable("format.fairylights.text", s).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
