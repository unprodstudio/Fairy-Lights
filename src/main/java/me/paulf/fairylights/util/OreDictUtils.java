package me.paulf.fairylights.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class OreDictUtils {
    private OreDictUtils() {}

    public static boolean isDye(final ItemStack stack) {
        if (!stack.isEmpty()) {
            return stack.has(DataComponents.DYE) || stack.is(ConventionalItemTags.DYES);
        }
        return false;
    }

    public static DyeColor getDyeColor(final ItemStack stack) {
        if (!stack.isEmpty()) {
            if (stack.has(DataComponents.DYE)) {
                return stack.get(DataComponents.DYE);
            }
            for (final Dye dye : Dye.values()) {
                if (stack.is(dye.getName())) {
                    return dye.getColor();
                }
            }
        }
        return DyeColor.YELLOW;
    }

    public static ImmutableList<ItemStack> getDyes(final DyeColor color) {
        return getDyeItemStacks().get(color).asList();
    }

    public static ImmutableList<ItemStack> getAllDyes() {
        return getDyeItemStacks().values().asList();
    }

    private static ImmutableMultimap<DyeColor, ItemStack> getDyeItemStacks() {
        final ImmutableMultimap.Builder<DyeColor, ItemStack> bob = ImmutableMultimap.builder();
        for (final Dye dye : Dye.values()) {
            for (final Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(dye.getName())) {
                bob.put(dye.getColor(), new ItemStack(holder));
            }
        }
        return bob.build();
    }

    private enum Dye {
        WHITE(ConventionalItemTags.WHITE_DYES, DyeColor.WHITE),
        ORANGE(ConventionalItemTags.ORANGE_DYES, DyeColor.ORANGE),
        MAGENTA(ConventionalItemTags.MAGENTA_DYES, DyeColor.MAGENTA),
        LIGHT_BLUE(ConventionalItemTags.LIGHT_BLUE_DYES, DyeColor.LIGHT_BLUE),
        YELLOW(ConventionalItemTags.YELLOW_DYES, DyeColor.YELLOW),
        LIME(ConventionalItemTags.LIGHT_BLUE_DYES, DyeColor.LIME),
        PINK(ConventionalItemTags.PINK_DYES, DyeColor.PINK),
        GRAY(ConventionalItemTags.GRAY_DYES, DyeColor.GRAY),
        LIGHT_GRAY(ConventionalItemTags.LIGHT_GRAY_DYES, DyeColor.LIGHT_GRAY),
        CYAN(ConventionalItemTags.CYAN_DYES, DyeColor.CYAN),
        PURPLE(ConventionalItemTags.PURPLE_DYES, DyeColor.PURPLE),
        BLUE(ConventionalItemTags.BLUE_DYES, DyeColor.BLUE),
        BROWN(ConventionalItemTags.BROWN_DYES, DyeColor.BROWN),
        GREEN(ConventionalItemTags.GREEN_DYES, DyeColor.GREEN),
        RED(ConventionalItemTags.RED_DYES, DyeColor.RED),
        BLACK(ConventionalItemTags.BLACK_DYES, DyeColor.BLACK);

        private final TagKey<Item> name;

        private final DyeColor color;

        Dye(final TagKey<Item> name, final DyeColor color) {
            this.name = name;
            this.color = color;
        }

        private TagKey<Item> getName() {
            return this.name;
        }

        private DyeColor getColor() {
            return this.color;
        }
    }
}
