/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.resource.datagen;

import com.google.common.collect.Maps;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

final class ModelProvider extends FabricModelProvider {
	private static final TextureSlot CANDLE_SLOT = TextureSlot.create("candle");
	private static final List<ModelTemplate> CEILING_CHANDELIER_CANDLE_TEMPLATES = List.of(
			modelTemplate("block/template/chandelier/ceiling/candle_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_2_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_2_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_3_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_3_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_3_3", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_4_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_4_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_4_3", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/ceiling/candle_4_4", CANDLE_SLOT)
	);
	private static final List<ModelTemplate> WALL_CHANDELIER_CANDLE_TEMPLATES = List.of(
			modelTemplate("block/template/chandelier/wall/candle_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_2_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_2_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_3_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_3_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_3_3", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_4_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_4_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_4_3", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/wall/candle_4_4", CANDLE_SLOT)
	);
	private static final List<ModelTemplate> STANDING_CHANDELIER_CANDLE_TEMPLATES = List.of(
			modelTemplate("block/template/chandelier/standing/candle_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_2_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_2_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_3_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_3_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_3_3", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_4_1", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_4_2", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_4_3", CANDLE_SLOT),
			modelTemplate("block/template/chandelier/standing/candle_4_4", CANDLE_SLOT)
	);

	private final PackOutput.PathProvider blockStatesPathProvider;
	private final Map<Identifier, BlockModelDefinitionGenerator> blockStates = new HashMap<>();

	public ModelProvider(FabricDataOutput output) {
		super(output);
		this.blockStatesPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators generators) {
		for (var candle : AbstractChandelierBlock.Candle.BY_NAME.values()) {
			var name = candle.vanillaPrefix() + "candle";
			var normalMapping = new TextureMapping()
					.put(CANDLE_SLOT, Identifier.withDefaultNamespace("block/" + name));
			var litMapping = normalMapping.copyAndUpdate(
					CANDLE_SLOT, Identifier.withDefaultNamespace("block/" + name + "_lit")
			);

			this.doCandle(CEILING_CHANDELIER_CANDLE_TEMPLATES, "ceiling/" + name, normalMapping, litMapping, generators);
			this.doWallCandle("wall/" + name, normalMapping, litMapping, generators);
			this.doCandle(STANDING_CHANDELIER_CANDLE_TEMPLATES, "standing/" + name, normalMapping, litMapping, generators);
		}
	}

	@Override
	public void generateItemModels(ItemModelGenerators generators) {
		generators.generateFlatItem(AurorasLanternsRegistry.AMETHYST_LANTERN_ITEM, ModelTemplates.FLAT_ITEM);
		generators.generateFlatItem(AurorasLanternsRegistry.REDSTONE_LANTERN_ITEM, ModelTemplates.FLAT_ITEM);
		generators.generateFlatItem(AurorasLanternsRegistry.IRON_CHANDELIER_ITEM, ModelTemplates.FLAT_ITEM);
	}

	@Override
	public String getName() {
		return "AurorasLanternsModelProvider";
	}

	private void doCandle(
			List<ModelTemplate> templates, String name,
			TextureMapping normalMapping, TextureMapping litMapping,
			BlockModelGenerators generators
	) {
		int templateIndex = 0;

		for (int holders = 1; holders <= 4; holders++) {
			for (int candleIndex = 1; candleIndex <= holders; candleIndex++) {
				String candleName = name + "_"
						+ (holders == 1 ? String.valueOf(holders) : (holders + "_" + candleIndex));

				var models = ChandelierCandleModels.create(
						templates.get(templateIndex), candleName, normalMapping, litMapping, generators
				);

				templateIndex++;
			}
		}
	}

	private void doWallCandle(
			String name,
			TextureMapping normalMapping, TextureMapping litMapping,
			BlockModelGenerators generators
	) {
		int templateIndex = 0;

		for (int holders = 1; holders <= 4; holders++) {
			for (int candleIndex = 1; candleIndex <= holders; candleIndex++) {
				String candleName = name + "_"
						+ (holders == 1 ? String.valueOf(holders) : (holders + "_" + candleIndex));

				var models = ChandelierCandleModels.create(
						WALL_CHANDELIER_CANDLE_TEMPLATES.get(templateIndex), candleName, normalMapping, litMapping, generators
				);

				templateIndex++;
			}
		}
	}

	public CompletableFuture<?> saveCustomBlockStates(CachedOutput output) {
		Map<Identifier, BlockModelDefinition> map = Maps.transformValues(
				this.blockStates, BlockModelDefinitionGenerator::create
		);
		return DataProvider.saveAll(output, BlockModelDefinition.CODEC, this.blockStatesPathProvider::json, map);
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return CompletableFuture.allOf(
				super.run(output),
				this.saveCustomBlockStates(output)
		);
	}

	private static ModelTemplate modelTemplate(String parent, TextureSlot... requiredSlots) {
		return new ModelTemplate(Optional.of(AurorasLanterns.id(parent)), Optional.empty(), requiredSlots);
	}
}
