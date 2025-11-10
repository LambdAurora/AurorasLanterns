/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.model;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.block.entity.ChandelierBlockEntity;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record ChandelierModelData(Map<ChandelierBlockEntity.Candle, Entry[][]> models) {
	public static Identifier modelId(String name) {
		return AurorasLanterns.id("block/chandelier/candle/" + name);
	}

	public static ChandelierModelData resolve(String attachmentType, ModelBaker baker) {
		var map = new HashMap<ChandelierBlockEntity.Candle, Entry[][]>();

		for (var candle : ChandelierBlockEntity.Candle.BY_NAME.values()) {
			var name = candle.vanillaPrefix() + "candle";
			var arrayOuter = new Entry[4][];
			map.put(candle, arrayOuter);

			for (int candles = 1; candles <= 4; candles++) {
				var arrayInner = new Entry[candles];
				arrayOuter[candles - 1] = arrayInner;

				for (int currentCandle = 1; currentCandle <= candles; currentCandle++) {
					String suffix = candles == 1 ? "" : ("_" + currentCandle);

					arrayInner[currentCandle - 1] = new Entry(
							new SingleVariant(SimpleModelWrapper.bake(
									baker, modelId(attachmentType + "/" + name + "_" + candles + suffix), BlockModelRotation.IDENTITY
							)),
							new SingleVariant(SimpleModelWrapper.bake(
									baker, modelId(attachmentType + "/" + name + "_" + candles + suffix + "_lit"), BlockModelRotation.IDENTITY
							))
					);
				}
			}

		}

		return new ChandelierModelData(Collections.unmodifiableMap(map));
	}

	public record Entry(BlockStateModel unlit, BlockStateModel lit) {
		public BlockStateModel get(BlockState state) {
			return state.getValue(BlockStateProperties.LIT) ? this.lit : this.unlit;
		}
	}
}
