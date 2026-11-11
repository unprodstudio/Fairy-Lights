package me.paulf.fairylights.server.creativetabs;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public final class FairyLightsItemGroup
{
    public FairyLightsItemGroup()
    {
        super();
    }

    public static final CreativeModeTab GENERAL = register("general", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1)
                                                                                                      .icon(() -> new ItemStack(FLItems.HANGING_LIGHTS))
                                                                                                      .title(Component.literal("FairyLights")).displayItems((config, output) -> {


          for (final DyeColor color : DyeColor.values())
          {
              output.accept(FLCraftingRecipes.makeHangingLights(new ItemStack(FLItems.HANGING_LIGHTS), color));
          }

          for (final DyeColor color : DyeColor.values())
          {
              final ItemStack stack = new ItemStack(FLItems.PENNANT_BUNTING);
              DyeableItem.setColor(stack, color);
              output.accept(FLCraftingRecipes.makePennant(stack, color));
          }
          output.acceptAll(generateCollection(FLItems.TINSEL));

          final ItemStack bunting = new ItemStack(FLItems.LETTER_BUNTING, 1);
          bunting.set(FLComponents.STYLED_STRING, Component.empty());
          output.accept(bunting);
          output.accept(new ItemStack(FLItems.GARLAND));

          output.acceptAll(generateCollection(FLItems.TRIANGLE_PENNANT));
          output.acceptAll(generateCollection(FLItems.SPEARHEAD_PENNANT));
          output.acceptAll(generateCollection(FLItems.SWALLOWTAIL_PENNANT));
          output.acceptAll(generateCollection(FLItems.SQUARE_PENNANT));

          output.acceptAll(generateCollection(FLItems.FAIRY_LIGHT));
          output.acceptAll(generateCollection(FLItems.PAPER_LANTERN));
          output.acceptAll(generateCollection(FLItems.ORB_LANTERN));
          output.acceptAll(generateCollection(FLItems.FLOWER_LIGHT));
          output.acceptAll(generateCollection(FLItems.CANDLE_LANTERN_LIGHT));
          output.acceptAll(generateCollection(FLItems.OIL_LANTERN_LIGHT));
          output.acceptAll(generateCollection(FLItems.JACK_O_LANTERN));
          output.acceptAll(generateCollection(FLItems.SKULL_LIGHT));
          output.acceptAll(generateCollection(FLItems.GHOST_LIGHT));
          output.acceptAll(generateCollection(FLItems.SPIDER_LIGHT));
          output.acceptAll(generateCollection(FLItems.WITCH_LIGHT));
          output.acceptAll(generateCollection(FLItems.SNOWFLAKE_LIGHT));
          output.acceptAll(generateCollection(FLItems.HEART_LIGHT));
          output.acceptAll(generateCollection(FLItems.MOON_LIGHT));
          output.acceptAll(generateCollection(FLItems.STAR_LIGHT));
          output.acceptAll(generateCollection(FLItems.ICICLE_LIGHTS));
          output.acceptAll(generateCollection(FLItems.METEOR_LIGHT));

          output.accept(new ItemStack(FLItems.OIL_LANTERN));
          output.accept(new ItemStack(FLItems.CANDLE_LANTERN));
          output.accept(new ItemStack(FLItems.INCANDESCENT_LIGHT));
      }).build());

    private static Collection<ItemStack> generateCollection(final @NonNull Item item)
    {
        final List<ItemStack> stacks = new ArrayList<>();
        for (final DyeColor color : DyeColor.values())
        {
            stacks.add(DyeableItem.setColor(new ItemStack(item), color));
        }
        return stacks;
    }

    public static void register() {}

    private static CreativeModeTab register(String name, Supplier<CreativeModeTab> register) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, FairyLights.id(name), register.get());
    }
}
