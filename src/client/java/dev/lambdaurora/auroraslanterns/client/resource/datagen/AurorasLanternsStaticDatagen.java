/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.resource.datagen;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.ChandelierBlocks;
import dev.lambdaurora.auroraslanterns.advancement.WallLanternBonkTrigger;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.concurrent.CompletableFuture;
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

			ChandelierBlocks.streamAll().flatMap(ChandelierBlocks::stream).forEach(block -> {
				this.add(block, b -> this.createSingleItemTable(b, ConstantValue.exactly(block.holders())));
			});
		}
	}

	private static class AdvancementProvider extends FabricAdvancementProvider {
		public AdvancementProvider(
				FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup
		) {
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

			this.shaped(RecipeCategory.DECORATIONS, AurorasLanternsRegistry.IRON_CHANDELIER_ITEM, 8)
					.define('N', Items.IRON_NUGGET)
					.define('I', Items.IRON_INGOT)
					.pattern("N")
					.pattern("I")
					.unlockedBy("has_nugget", has(Items.IRON_NUGGET))
					.unlockedBy("has_ingot", has(Items.IRON_INGOT))
					.unlockedBy("has_self", has(AurorasLanternsRegistry.IRON_CHANDELIER_ITEM))
					.save(this.output);
			this.shaped(RecipeCategory.DECORATIONS, AurorasLanternsRegistry.COPPER_CHANDELIER_ITEM, 8)
					.define('N', Items.COPPER_NUGGET)
					.define('I', Items.COPPER_INGOT)
					.pattern("N")
					.pattern("I")
					.unlockedBy("has_nugget", has(Items.IRON_NUGGET))
					.unlockedBy("has_ingot", has(Items.IRON_INGOT))
					.unlockedBy("has_self", has(AurorasLanternsRegistry.COPPER_CHANDELIER_ITEM))
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
}

