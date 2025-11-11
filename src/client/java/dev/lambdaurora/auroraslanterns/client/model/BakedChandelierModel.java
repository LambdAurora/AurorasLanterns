/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.model;

import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock.Candle;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

public final class BakedChandelierModel extends WrapperBlockStateModel {
	private final Map<Candle, BlockStateModel[]> candles;

	public BakedChandelierModel(BlockStateModel wrapped, Map<Candle, BlockStateModel[]> candles) {
		super(wrapped);
		this.candles = candles;
	}

	@Override
	public void emitQuads(
			QuadEmitter emitter,
			BlockAndTintGetter blockView,
			BlockPos pos,
			BlockState state,
			RandomSource random,
			Predicate<@Nullable Direction> cullTest
	) {
		super.emitQuads(emitter, blockView, pos, state, random, cullTest);

		if (AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE.isValid(state)
				&& state.getBlock() instanceof AbstractChandelierBlock chandelierBlock
		) {
			var data = (Candle[]) blockView.getBlockEntityRenderData(pos);
			if (data == null) return;

			for (int i = 0; i < Math.min(data.length, chandelierBlock.holders()); i++) {
				var candle = data[i];
				var entry = this.candles.get(candle);

				if (entry != null && entry[i] != null) {
					entry[i].emitQuads(emitter, blockView, pos, state, random, cullTest);
				}
			}
		}
	}
}
