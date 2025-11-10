/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.resource;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.advancement.WallLanternBonkTrigger;
import dev.lambdaurora.auroraslanterns.block.entity.ChandelierBlockEntity;
import dev.lambdaurora.auroraslanterns.client.model.ChandelierModelData;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class AurorasLanternsStaticDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(LootDataProvider::new);
		pack.addProvider(AdvancementProvider::new);
		pack.addProvider(AurorasRecipeProvider.Runner::new);
		pack.addProvider(ModelProvider::new);
	}

	private static class LootDataProvider extends FabricBlockLootTableProvider {
		public LootDataProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generate() {
			this.add(AurorasLanternsRegistry.AMETHYST_LANTERN_BLOCK, this::createSingleItemTable);
			this.add(AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK, this::createSingleItemTable);
		}
	}

	private static class AdvancementProvider extends FabricAdvancementProvider {
		public AdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
			consumer.accept(Advancement.Builder.advancement()
					.parent(new AdvancementHolder(Identifier.withDefaultNamespace("adventure/root"), null))
					.display(
							Blocks.LANTERN,
							Component.translatable(
									"advancements.%s.adventure.wall_lantern_bonk.title".formatted(AurorasLanterns.NAMESPACE)
							),
							Component.translatable(
									"advancements.%s.adventure.wall_lantern_bonk.description".formatted(AurorasLanterns.NAMESPACE)
							),
							null,
							AdvancementType.TASK,
							true,
							true,
							false
					)
					.addCriterion("bonk", WallLanternBonkTrigger.TriggerInstance.bonk())
					.build(AurorasLanterns.id("adventure/wall_lantern_bonk"))
			);
		}
	}

	private static class AurorasRecipeProvider extends RecipeProvider {
		public AurorasRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
			super(provider, recipeOutput);
		}

		@Override
		public void buildRecipes() {
			this.shaped(RecipeCategory.DECORATIONS, AurorasLanternsRegistry.AMETHYST_LANTERN_ITEM)
					.define('S', Items.AMETHYST_SHARD)
					.define('L', Items.LANTERN)
					.pattern("SLS")
					.unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
					.unlockedBy("has_lantern", has(Items.LANTERN))
					.unlockedBy("has_self", has(AurorasLanternsRegistry.AMETHYST_LANTERN_ITEM))
					.save(this.output);
			this.shaped(RecipeCategory.REDSTONE, AurorasLanternsRegistry.REDSTONE_LANTERN_ITEM)
					.define('I', Items.IRON_NUGGET)
					.define('T', Items.REDSTONE_TORCH)
					.pattern("III")
					.pattern("ITI")
					.pattern("III")
					.unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
					.unlockedBy("has_redstone_torch", has(Items.REDSTONE_TORCH))
					.unlockedBy("has_self", has(AurorasLanternsRegistry.REDSTONE_LANTERN_ITEM))
					.save(this.output);
		}

		private static class Runner extends FabricRecipeProvider {
			public Runner(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
				super(output, registriesFuture);
			}

			@Override
			protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
				return new AurorasRecipeProvider(provider, recipeOutput);
			}

			@Override
			public String getName() {
				return "Aurora's Lanterns Recipes";
			}
		}
	}

	public static final class ModelProvider extends FabricModelProvider {
		private static final TextureSlot CANDLE_SLOT = TextureSlot.create("candle");
		private static final ModelTemplate CEILING_CHANDELIER_1_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_1", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_2_1_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_2_1", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_2_2_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_2_2", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_3_1_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_3_1", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_3_2_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_3_2", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_3_3_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_3_3", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_4_1_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_4_1", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_4_2_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_4_2", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_4_3_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_4_3", CANDLE_SLOT);
		private static final ModelTemplate CEILING_CHANDELIER_4_4_CANDLE_TEMPLATE = modelTemplate("block/template/chandelier/ceiling/candle_4_4", CANDLE_SLOT);

		public ModelProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockModelGenerators generators) {
			for (var candle : ChandelierBlockEntity.Candle.BY_NAME.values()) {
				var name = candle.vanillaPrefix() + "candle";
				var normalMapping = new TextureMapping()
						.put(CANDLE_SLOT, Identifier.withDefaultNamespace("block/" + name));
				var litMapping = normalMapping.copyAndUpdate(
						CANDLE_SLOT, Identifier.withDefaultNamespace("block/" + name + "_lit")
				);

				doCandleTemplate(CEILING_CHANDELIER_1_CANDLE_TEMPLATE, "ceiling/" + name + "_1", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_2_1_CANDLE_TEMPLATE, "ceiling/" + name + "_2_1", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_2_2_CANDLE_TEMPLATE, "ceiling/" + name + "_2_2", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_3_1_CANDLE_TEMPLATE, "ceiling/" + name + "_3_1", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_3_2_CANDLE_TEMPLATE, "ceiling/" + name + "_3_2", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_3_3_CANDLE_TEMPLATE, "ceiling/" + name + "_3_3", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_4_1_CANDLE_TEMPLATE, "ceiling/" + name + "_4_1", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_4_2_CANDLE_TEMPLATE, "ceiling/" + name + "_4_2", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_4_3_CANDLE_TEMPLATE, "ceiling/" + name + "_4_3", normalMapping, litMapping, generators.modelOutput);
				doCandleTemplate(CEILING_CHANDELIER_4_4_CANDLE_TEMPLATE, "ceiling/" + name + "_4_4", normalMapping, litMapping, generators.modelOutput);
			}
		}

		@Override
		public void generateItemModels(ItemModelGenerators generators) {
			generators.generateFlatItem(AurorasLanternsRegistry.AMETHYST_LANTERN_ITEM, ModelTemplates.FLAT_ITEM);
			generators.generateFlatItem(AurorasLanternsRegistry.REDSTONE_LANTERN_ITEM, ModelTemplates.FLAT_ITEM);
		}

		@Override
		public String getName() {
			return "AurorasLanternsModelProvider";
		}

		private static void doCandleTemplate(
				ModelTemplate template, String name, TextureMapping normalTextures, TextureMapping litTextures, BiConsumer<Identifier, ModelInstance> output
		) {
			template.create(
					ChandelierModelData.modelId(name),
					normalTextures,
					output
			);
			template.create(
					ChandelierModelData.modelId(name + "_lit"),
					litTextures,
					output
			);
		}

		private static ModelTemplate modelTemplate(String parent, TextureSlot... requiredSlots) {
			return new ModelTemplate(Optional.of(AurorasLanterns.id(parent)), Optional.empty(), requiredSlots);
		}
	}
}

