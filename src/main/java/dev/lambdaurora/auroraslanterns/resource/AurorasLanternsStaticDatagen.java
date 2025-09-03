/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.resource;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
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
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class AurorasLanternsStaticDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(LootDataProvider::new);
		pack.addProvider(AdvancementProvider::new);
		pack.addProvider(AurorasRecipeProvider::new);
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
					.parent(new AdvancementHolder(Identifier.ofDefault("adventure/root"), null))
					.display(
							Blocks.LANTERN,
							Text.translatable(
									"advancements.%s.adventure.wall_lantern_bonk.title".formatted(AurorasLanterns.NAMESPACE)
							),
							Text.translatable(
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

	private static class AurorasRecipeProvider extends FabricRecipeProvider {
		public AurorasRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		public void buildRecipes(RecipeOutput output) {
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, AurorasLanternsRegistry.AMETHYST_LANTERN_ITEM)
					.define('S', Items.AMETHYST_SHARD)
					.define('L', Items.LANTERN)
					.pattern("SLS")
					.unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
					.unlockedBy("has_lantern", has(Items.LANTERN))
					.unlockedBy("has_self", has(AurorasLanternsRegistry.AMETHYST_LANTERN_ITEM))
					.save(output);
			ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, AurorasLanternsRegistry.REDSTONE_LANTERN_ITEM)
					.define('I', Items.IRON_NUGGET)
					.define('T', Items.REDSTONE_TORCH)
					.pattern("III")
					.pattern("ITI")
					.pattern("III")
					.unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
					.unlockedBy("has_redstone_torch", has(Items.REDSTONE_TORCH))
					.unlockedBy("has_self", has(AurorasLanternsRegistry.REDSTONE_LANTERN_ITEM))
					.save(output);
		}
	}
}
