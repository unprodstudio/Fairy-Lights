package me.paulf.fairylights.server.creativetabs;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class FairyLightsItemGroup
{
    public FairyLightsItemGroup()
    {
        super();
    }

    public static final DeferredRegister<CreativeModeTab> TAB_REG = DeferredRegister.create(FairyLights.ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> GENERAL = TAB_REG.register("general", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1)
                                                                                                      .icon(() -> new ItemStack(FLItems.HANGING_LIGHTS.get()))
                                                                                                      .title(Component.literal("FairyLights")).displayItems((config, output) -> {


          for (final DyeColor color : DyeColor.values())
          {
              output.accept(FLCraftingRecipes.makeHangingLights(new ItemStack(FLItems.HANGING_LIGHTS.get()), color));
          }

          for (final DyeColor color : DyeColor.values())
          {
              final ItemStack stack = new ItemStack(FLItems.PENNANT_BUNTING.get());
              DyeableItem.setColor(stack, color);
              output.accept(FLCraftingRecipes.makePennant(stack, color));
          }
          output.acceptAll(generateCollection(FLItems.TINSEL.get()));

          final ItemStack bunting = new ItemStack(FLItems.LETTER_BUNTING.get(), 1);
          bunting.set(FLComponents.STYLED_STRING, Component.empty());
          output.accept(bunting);
          output.accept(new ItemStack(FLItems.GARLAND.get()));

          output.acceptAll(generateCollection(FLItems.TRIANGLE_PENNANT.get()));
          output.acceptAll(generateCollection(FLItems.SPEARHEAD_PENNANT.get()));
          output.acceptAll(generateCollection(FLItems.SWALLOWTAIL_PENNANT.get()));
          output.acceptAll(generateCollection(FLItems.SQUARE_PENNANT.get()));

          output.acceptAll(generateCollection(FLItems.FAIRY_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.PAPER_LANTERN.get()));
          output.acceptAll(generateCollection(FLItems.ORB_LANTERN.get()));
          output.acceptAll(generateCollection(FLItems.FLOWER_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.CANDLE_LANTERN_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.OIL_LANTERN_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.JACK_O_LANTERN.get()));
          output.acceptAll(generateCollection(FLItems.SKULL_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.GHOST_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.SPIDER_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.WITCH_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.SNOWFLAKE_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.HEART_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.MOON_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.STAR_LIGHT.get()));
          output.acceptAll(generateCollection(FLItems.ICICLE_LIGHTS.get()));
          output.acceptAll(generateCollection(FLItems.METEOR_LIGHT.get()));

          output.accept(new ItemStack(FLItems.OIL_LANTERN.get()));
          output.accept(new ItemStack(FLItems.CANDLE_LANTERN.get()));
          output.accept(new ItemStack(FLItems.INCANDESCENT_LIGHT.get()));
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
}
