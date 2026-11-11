package me.paulf.fairylights.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.Unit;
import org.apache.commons.lang3.IntegerRange;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

import java.util.*;

public final class Utils {
    private Utils() {}

    public static <E extends Enum<E>> E getEnumValue(final Class<E> clazz, final int ordinal) {
        final E[] values = Objects.requireNonNull(clazz, "clazz").getEnumConstants();
        return values[ordinal < 0 || ordinal >= values.length ? 0 : ordinal];
    }

    public static Component formatRecipeTooltip(final String key) {
        return formatRecipeTooltipValue(Language.getInstance().getOrDefault(key));
    }

    private static Component formatRecipeTooltipValue(final String value) {
        return Component.translatable("recipe.ingredient.tooltip", value);
    }

    public static boolean impliesNbt(@Nullable Tag antecedent, @Nullable Tag consequent) {
        if (antecedent == consequent) return true;
        if ((antecedent == null) != (consequent == null)) return false;
        if (!antecedent.getClass().equals(consequent.getClass())) return false;
        if (antecedent instanceof CompoundTag c) {
            for (String key : c.keySet()) {
                if (!impliesNbt(c.get(key), c.get(key))) {
                    return false;
                }
            }
            return true;
        }
        return antecedent.equals(consequent);
    }

    public static boolean isEmpty(Component component) {
        return (component.getContents() == PlainTextContents.EMPTY && component.getSiblings().isEmpty()) || component.getString().isEmpty();
    }

    public static boolean isPlain(Style style) {
        return !style.isBold() && !style.isItalic() && !style.isObfuscated() && !style.isUnderlined() && !style.isStrikethrough();
    }

    public static String reverse(String existing) {
        return new StringBuilder(existing).reverse().toString();
    }

    public static Style styleAt(FormattedText text, int index) {
        final MutableInt position = new MutableInt(0);
        return text.visit((style, contents) -> {
            if (position.intValue() >= index && position.intValue() + (contents.length() - 1) <= index) {
                return Optional.of(style);
            }

            return Optional.empty();
        }, Style.EMPTY)
            .orElse(Style.EMPTY);
    }

    public static Component flatten(Component original) {
        final List<Style> styles = new ArrayList<>();
        styles.add(Style.EMPTY);
        final StringBuilder stringBuilder = new StringBuilder();
        final List<String> strings = new ArrayList<>();

        original.getVisualOrderText().accept((_, style, codepoint) -> {
            if (style != styles.getFirst()) {
                styles.add(style);

                String str = stringBuilder.toString();
                strings.add(str);
                stringBuilder.setLength(0);
            }

            String chars = Character.toString(codepoint);
            stringBuilder.append(chars);

            return true;
        });

        if (!stringBuilder.isEmpty())
            strings.add(stringBuilder.toString());

        final MutableComponent shortened = Component.empty();
        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);
            Style style = styles.size() > strings.size() ? styles.get(i + 1) : styles.get(i);
            shortened.append(Component.literal(str).withStyle(style));
        }

        return shortened;
    }

    public static Component withStyling(Component original, final int start, final int end, Style style) {
        final String str = original.getString();
        final SortedMap<IntegerRange, Style> styles = new Object2ObjectAVLTreeMap<>();

        final MutableInt currentStylePos = new MutableInt();
        final MutableInt index = new MutableInt();
        final MutableObject<Style> currentStyle = new MutableObject<>(Style.EMPTY);

        original.visit((current, contents) -> {
            int lastIndex = index.addAndGet(contents.length() - 1);

            if (current != currentStyle.get()) {
                styles.put(IntegerRange.of(currentStylePos.intValue(), lastIndex), currentStyle.get());
                currentStyle.setValue(current);
            }

            index.increment();

            return Optional.empty();
        }, Style.EMPTY);

        var ranges = new ArrayList<>(styles.keySet());
        final MutableComponent modified = Component.empty();

        for (IntegerRange range : ranges) {
            if (range.contains(start) || range.contains(end)) {
                Style current = styles.get(range);
                String intended = str.substring(start, end);

                if (range.getMinimum() < start) {
                    String initial = str.substring(range.getMinimum(), start - 1);
                    modified.append(Component.literal(initial).withStyle(current));
                }

                modified.append(Component.literal(intended).withStyle(style));

                if (range.getMaximum() > end) {
                    String initial = str.substring(end + 1, range.getMaximum());
                    modified.append(Component.literal(initial).withStyle(current));
                }
            } else {
                Style current = styles.get(range);
                modified.append(Component.literal(str.substring(range.getMinimum(), range.getMaximum())).withStyle(current));
            }
        }

        return modified;
    }

    public static Component substr(final Component original, final int end) {
        return substr(original, 0, end);
    }

    public static Component substr(final Component original, final int start, final int end) {
        final List<Style> styles = new ArrayList<>();
        styles.add(Style.EMPTY);
        final StringBuilder stringBuilder = new StringBuilder();
        final List<String> strings = new ArrayList<>();
        final MutableInt w = new MutableInt(0);

        original.getVisualOrderText().accept((position, style, codepoint) -> {
            if (style != styles.getFirst()) {
                styles.add(style);
                if (position < start)
                    styles.removeFirst();

                String str = stringBuilder.toString();
                strings.add(str);
                stringBuilder.setLength(0);
            }

            if (position < start)
                return true;

            String chars = Character.toString(codepoint);

            w.add(chars.length());
            if (style.isBold())
                w.increment();

            if (position > end)
                return false;

            stringBuilder.append(chars);

            return true;
        });

        if (!stringBuilder.isEmpty())
            strings.add(stringBuilder.toString());

        final MutableComponent shortened = Component.empty();
        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);
            Style style = styles.size() > strings.size() ? styles.get(i + 1) : styles.get(i);
            shortened.append(Component.literal(str).withStyle(style));
        }

        return shortened;
    }

    public static ChatFormatting fromTextColor(TextColor color) {
        if (color == TextColor.BLACK)
            return ChatFormatting.BLACK;
        else if (color == TextColor.DARK_BLUE)
            return ChatFormatting.DARK_BLUE;
        else if (color == TextColor.DARK_GREEN)
            return ChatFormatting.DARK_GREEN;
        else if (color == TextColor.DARK_AQUA)
            return ChatFormatting.DARK_AQUA;
        else if (color == TextColor.DARK_RED)
            return ChatFormatting.DARK_RED;
        else if (color == TextColor.DARK_PURPLE)
            return ChatFormatting.DARK_PURPLE;
        else if (color == TextColor.GOLD)
            return ChatFormatting.GOLD;
        else if (color == TextColor.GRAY)
            return ChatFormatting.GRAY;
        else if (color == TextColor.DARK_GRAY)
            return ChatFormatting.DARK_GRAY;
        else if (color == TextColor.BLUE)
            return ChatFormatting.BLUE;
        else if (color == TextColor.GREEN)
            return ChatFormatting.GREEN;
        else if (color == TextColor.AQUA)
            return ChatFormatting.AQUA;
        else if (color == TextColor.RED)
            return ChatFormatting.RED;
        else if (color == TextColor.LIGHT_PURPLE)
            return ChatFormatting.LIGHT_PURPLE;
        else if (color == TextColor.YELLOW)
            return ChatFormatting.YELLOW;
        else if (color == TextColor.WHITE)
            return ChatFormatting.WHITE;
        else
            return ChatFormatting.RESET;
    }
}
