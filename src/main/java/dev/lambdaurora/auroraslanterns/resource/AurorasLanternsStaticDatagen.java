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
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.network.chat.Text;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public final class AurorasLanternsStaticDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(LootDataProvider::new);
		pack.addProvider(AdvancementProvider::new);
	}

	private static class LootDataProvider extends FabricBlockLootTableProvider {
		public LootDataProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generate() {
			this.add(AurorasLanternsRegistry.AMETHYST_LANTERN_BLOCK, this::createSingleItemTable);
			this.add(AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK, this::createSingleItemTable);
		}
	}

	private static class AdvancementProvider extends FabricAdvancementProvider {
		public AdvancementProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateAdvancement(Consumer<Advancement> consumer) {
			Advancement root = Advancement.Builder.advancement()
					.display(
							Items.MAP,
							Text.translatable("advancements.adventure.root.title"),
							Text.translatable("advancements.adventure.root.description"),
							new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
							FrameType.TASK,
							false,
							false,
							false
					)
					.requirements(RequirementsStrategy.OR)
					.addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
					.addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer())
					.build(new Identifier(Identifier.DEFAULT_NAMESPACE, "adventure/root"));

			consumer.accept(Advancement.Builder.advancement()
					.parent(root)
					.display(
							Blocks.LANTERN,
							Text.translatable(
									"advancements.%s.adventure.wall_lantern_bonk.title".formatted(AurorasLanterns.NAMESPACE)
							),
							Text.translatable(
									"advancements.%s.adventure.wall_lantern_bonk.description".formatted(AurorasLanterns.NAMESPACE)
							),
							null,
							FrameType.TASK,
							true,
							true,
							false
					)
					.addCriterion("bonk", WallLanternBonkTrigger.TriggerInstance.bonk(null))
					.build(AurorasLanterns.id("adventure/wall_lantern_bonk"))
			);
		}
	}
}
