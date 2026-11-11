package me.paulf.fairylights.data;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.color.*;
import me.paulf.fairylights.mixin.fabric.ShapedRecipeBuilderAccessor;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.item.FLItems;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.item.crafting.FLCraftingRecipes;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class DataGatherer implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator gen) {
        var pack = gen.createPack();
        pack.addProvider(RecipeGenerator::new);
        pack.addProvider(BlockLootTableGenerator::new);
        pack.addProvider(ModelGenerator::new);
    }

    static class ModelGenerator extends FabricModelProvider {
        public ModelGenerator(FabricPackOutput output) {
            super(output);
        }

        @Override
        public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        }

        @Override
        public void generateItemModels(ItemModelGenerators generator) {
            List.of(
                FLItems.FAIRY_LIGHT.get(),
                FLItems.PAPER_LANTERN.get(),
                FLItems.ORB_LANTERN.get(),
                FLItems.FLOWER_LIGHT.get(),
                FLItems.CANDLE_LANTERN_LIGHT.get(),
                FLItems.OIL_LANTERN_LIGHT.get(),
                FLItems.JACK_O_LANTERN.get(),
                FLItems.SKULL_LIGHT.get(),
                FLItems.GHOST_LIGHT.get(),
                FLItems.SPIDER_LIGHT.get(),
                FLItems.WITCH_LIGHT.get(),
                FLItems.SNOWFLAKE_LIGHT.get(),
                FLItems.HEART_LIGHT.get(),
                FLItems.MOON_LIGHT.get(),
                FLItems.STAR_LIGHT.get(),
                FLItems.ICICLE_LIGHTS.get(),
                FLItems.METEOR_LIGHT.get()
            )
                .forEach(item -> {
                    item(generator, item, List.of(
                        new Constant(0xFFFFFFFF),
                        new ColorChangingItemTintSource()
                    ));
                });

            item(generator, FLItems.HANGING_LIGHTS.get(), List.of(
                new StringTypeBasedItemTintSource(),
                new PatternOrDefaultItemTintSource(1),
                new PatternOrDefaultItemTintSource(2),
                new PatternOrDefaultItemTintSource(3)
            ));

            item(generator, FLItems.TINSEL.get(), List.of(
                new Dye(0xFFFFFFFF)
            ));

            item(generator, FLItems.PENNANT_BUNTING.get(), List.of(
                new Dye(0xFFFFFFFF),
                new PatternItemTintSource(1),
                new PatternItemTintSource(2),
                new PatternItemTintSource(3)
            ));

            List<ItemTintSource> secondLayerColor = List.of(new Constant(0xFFFFFFFF), new Dye(0xFFFFFFFF));

            item(generator, FLItems.TRIANGLE_PENNANT.get(), secondLayerColor);
            item(generator, FLItems.SPEARHEAD_PENNANT.get(), secondLayerColor);
            item(generator, FLItems.SWALLOWTAIL_PENNANT.get(), secondLayerColor);
            item(generator, FLItems.SQUARE_PENNANT.get(), secondLayerColor);

            item(generator, FLItems.LETTER_BUNTING.get(), List.of(
                new Constant(0xFFFFFFFF),
                new StyledStringItemTintSource(1),
                new StyledStringItemTintSource(2),
                new StyledStringItemTintSource(3)
            ));
        }

        private static void item(ItemModelGenerators generator, Item item, List<ItemTintSource> tintSources) {
            generator.itemModelOutput.accept(item, new CuboidItemModelWrapper.Unbaked(
                ModelLocationUtils.getModelLocation(item),
                Optional.empty(),
                tintSources
            ));
        }
    }

    static class RecipeGenerator extends FabricRecipeProvider {
        public RecipeGenerator(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
            super(output, registriesFuture);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements) {
            return new RecipeProvider(recipes, advancements) {
                private ShapedRecipeBuilder shaped(HolderGetter<Item> itemsRegistry, RecipeCategory category, ItemLike item, DataComponentPatch components) {
                    return shaped(itemsRegistry, category, item, 1, components);
                }

                private ShapedRecipeBuilder shaped(HolderGetter<Item> itemsRegistry, RecipeCategory category, ItemLike item, int count, DataComponentPatch components) {
                    var template = new ItemStackTemplate(item.asItem().builtInRegistryHolder(), count, components);
                    return ShapedRecipeBuilderAccessor.createShapedRecipeBuilder(itemsRegistry, category, template);
                }

                @Override
                public void buildRecipes() {
                    var components = DataComponentPatch.builder()
                        .set(FLComponents.STYLED_STRING, Component.empty())
                        .build();
                    var items = registries.lookupOrThrow(Registries.ITEM);
                    shaped(items, RecipeCategory.DECORATIONS, FLItems.LETTER_BUNTING.get(), components)
                        .pattern("I-I")
                        .pattern("PBF")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('-', ConventionalItemTags.STRINGS)
                        .define('P', Items.PAPER)
                        .define('B', Items.INK_SAC)
                        .define('F', ConventionalItemTags.FEATHERS)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_string", has(ConventionalItemTags.STRINGS))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.GARLAND.get(), 2)
                        .pattern("I-I")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('-', Items.VINE)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_vine", has(Items.VINE))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.OIL_LANTERN.get(), 4)
                        .pattern(" I ")
                        .pattern("STS")
                        .pattern("IGI")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('S', Items.STICK)
                        .define('T', Items.TORCH)
                        .define('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_torch", has(Items.TORCH))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.CANDLE_LANTERN.get(), 4)
                        .pattern(" I ")
                        .pattern("GTG")
                        .pattern("IGI")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('G', ConventionalItemTags.GOLD_NUGGETS)
                        .define('T', Items.TORCH)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_torch", has(Items.TORCH))
                        .save(output);
                    ShapedRecipeBuilder.shaped(items, RecipeCategory.DECORATIONS,FLItems.INCANDESCENT_LIGHT.get(), 4)
                        .pattern(" I ")
                        .pattern("ITI")
                        .pattern(" G ")
                        .define('I', ConventionalItemTags.IRON_INGOTS)
                        .define('G', ConventionalItemTags.GLASS_PANES_COLORLESS)
                        .define('T', Items.TORCH)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_torch", has(Items.TORCH))
                        .save(output);
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.HANGING_LIGHTS.get())
                        .unlockedBy("has_lights", has(FLCraftingRecipes.LIGHTS))
                        .build(output, FairyLights.id("hanging_lights"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.HANGING_LIGHTS_AUGMENTATION.get())
                        .build(output, FairyLights.id("hanging_lights_augmentation"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.TINSEL_GARLAND.get())
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_string", has(ConventionalItemTags.STRINGS))
                        .build(output, FairyLights.id("tinsel_garland"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.PENNANT_BUNTING.get())
                        .unlockedBy("has_pennants", has(FLCraftingRecipes.PENNANTS))
                        .build(output, FairyLights.id("pennant_bunting"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.PENNANT_BUNTING_AUGMENTATION.get())
                        .build(output, FairyLights.id("pennant_bunting_augmentation"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.EDIT_COLOR.get())
                        .build(output, FairyLights.id("edit_color"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.COPY_COLOR.get())
                        .build(output, FairyLights.id("copy_color"));
                    this.pennantRecipe(FLCraftingRecipes.TRIANGLE_PENNANT.get())
                        .build(output, FairyLights.id("triangle_pennant"));
                    this.pennantRecipe(FLCraftingRecipes.SPEARHEAD_PENNANT.get())
                        .build(output, FairyLights.id("spearhead_pennant"));
                    this.pennantRecipe(FLCraftingRecipes.SWALLOWTAIL_PENNANT.get())
                        .build(output, FairyLights.id("swallowtail_pennant"));
                    this.pennantRecipe(FLCraftingRecipes.SQUARE_PENNANT.get())
                        .build(output, FairyLights.id("square_pennant"));
                    this.lightRecipe(FLCraftingRecipes.FAIRY_LIGHT.get())
                        .build(output, FairyLights.id("fairy_light"));
                    this.lightRecipe(FLCraftingRecipes.PAPER_LANTERN.get())
                        .build(output, FairyLights.id("paper_lantern"));
                    this.lightRecipe(FLCraftingRecipes.ORB_LANTERN.get())
                        .build(output, FairyLights.id("orb_lantern"));
                    this.lightRecipe(FLCraftingRecipes.FLOWER_LIGHT.get())
                        .build(output, FairyLights.id("flower_light"));
                    this.lightRecipe(FLCraftingRecipes.CANDLE_LANTERN_LIGHT.get())
                        .build(output, FairyLights.id("candle_lantern_light"));
                    this.lightRecipe(FLCraftingRecipes.OIL_LANTERN_LIGHT.get())
                        .build(output, FairyLights.id("oil_lantern_light"));
                    this.lightRecipe(FLCraftingRecipes.JACK_O_LANTERN.get())
                        .build(output, FairyLights.id("jack_o_lantern"));
                    this.lightRecipe(FLCraftingRecipes.SKULL_LIGHT.get())
                        .build(output, FairyLights.id("skull_light"));
                    this.lightRecipe(FLCraftingRecipes.GHOST_LIGHT.get())
                        .build(output, FairyLights.id("ghost_light"));
                    this.lightRecipe(FLCraftingRecipes.SPIDER_LIGHT.get())
                        .build(output, FairyLights.id("spider_light"));
                    this.lightRecipe(FLCraftingRecipes.WITCH_LIGHT.get())
                        .build(output, FairyLights.id("witch_light"));
                    this.lightRecipe(FLCraftingRecipes.SNOWFLAKE_LIGHT.get())
                        .build(output, FairyLights.id("snowflake_light"));
                    this.lightRecipe(FLCraftingRecipes.HEART_LIGHT.get())
                        .build(output, FairyLights.id("heart_light"));
                    this.lightRecipe(FLCraftingRecipes.MOON_LIGHT.get())
                        .build(output, FairyLights.id("moon_light"));
                    this.lightRecipe(FLCraftingRecipes.STAR_LIGHT.get())
                        .build(output, FairyLights.id("star_light"));
                    this.lightRecipe(FLCraftingRecipes.ICICLE_LIGHTS.get())
                        .build(output, FairyLights.id("icicle_lights"));
                    this.lightRecipe(FLCraftingRecipes.METEOR_LIGHT.get())
                        .build(output, FairyLights.id("meteor_light"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.LIGHT_TWINKLE.get())
                        .build(output, FairyLights.id("light_twinkle"));
                    GenericRecipeBuilder.customRecipe(FLCraftingRecipes.COLOR_CHANGING_LIGHT.get())
                        .build(output, FairyLights.id("color_changing_light"));
                }

                GenericRecipeBuilder lightRecipe(final RecipeSerializer<?> serializer) {
                    return GenericRecipeBuilder.customRecipe(serializer)
                        .unlockedBy("has_iron", has(ConventionalItemTags.IRON_INGOTS))
                        .unlockedBy("has_dye", has(ConventionalItemTags.DYES));
                }

                GenericRecipeBuilder pennantRecipe(final RecipeSerializer<?> serializer) {
                    return GenericRecipeBuilder.customRecipe(serializer)
                        .unlockedBy("has_paper", has(Items.PAPER))
                        .unlockedBy("has_string", has(ConventionalItemTags.STRINGS));
                }
            };
        }
    }

    static class BlockLootTableGenerator extends FabricBlockLootSubProvider
    {
        protected BlockLootTableGenerator(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture)
        {
            super(packOutput, registriesFuture);
        }

//        @Override
//        protected Iterable<Block> getKnownBlocks() {
//            return StreamSupport.stream(FLBlocks.REG.spliterator(), false).map(RegistrySupplier::get).collect(Collectors.toList());
//        }

        @Override
        public void generate()
        {
            this.add(FLBlocks.FASTENER.get(), noDrop());
            this.add(FLBlocks.FAIRY_LIGHT.get(), noDrop());
            this.add(FLBlocks.PAPER_LANTERN.get(), noDrop());
            this.add(FLBlocks.ORB_LANTERN.get(), noDrop());
            this.add(FLBlocks.FLOWER_LIGHT.get(), noDrop());
            this.add(FLBlocks.CANDLE_LANTERN_LIGHT.get(), noDrop());
            this.add(FLBlocks.OIL_LANTERN_LIGHT.get(), noDrop());
            this.add(FLBlocks.JACK_O_LANTERN.get(), noDrop());
            this.add(FLBlocks.SKULL_LIGHT.get(), noDrop());
            this.add(FLBlocks.GHOST_LIGHT.get(), noDrop());
            this.add(FLBlocks.SPIDER_LIGHT.get(), noDrop());
            this.add(FLBlocks.WITCH_LIGHT.get(), noDrop());
            this.add(FLBlocks.SNOWFLAKE_LIGHT.get(), noDrop());
            this.add(FLBlocks.HEART_LIGHT.get(), noDrop());
            this.add(FLBlocks.MOON_LIGHT.get(), noDrop());
            this.add(FLBlocks.STAR_LIGHT.get(), noDrop());
            this.add(FLBlocks.ICICLE_LIGHTS.get(), noDrop());
            this.add(FLBlocks.METEOR_LIGHT.get(), noDrop());
            this.add(FLBlocks.OIL_LANTERN.get(), noDrop());
            this.add(FLBlocks.CANDLE_LANTERN.get(), noDrop());
            this.add(FLBlocks.INCANDESCENT_LIGHT.get(), noDrop());
        }
    }
}
