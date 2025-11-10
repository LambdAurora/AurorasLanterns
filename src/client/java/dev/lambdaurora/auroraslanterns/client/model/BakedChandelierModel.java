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
import dev.lambdaurora.auroraslanterns.block.entity.ChandelierBlockEntity;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

public final class BakedChandelierModel extends WrapperBlockStateModel {
	private final ChandelierModelData modelData;

	public BakedChandelierModel(BlockStateModel wrapped, ChandelierModelData modelData) {
		super(wrapped);
		this.modelData = modelData;
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

		if (AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE.isValid(state)) {
			var data = (ChandelierBlockEntity.Candle[]) blockView.getBlockEntityRenderData(pos);
			if (data == null) return;

			for (int i = 0; i < data.length; i++) {
				var candle = data[i];
				this.modelData.models().get(candle)[data.length - 1][i].get(state).emitQuads(emitter, blockView, pos, state, random, cullTest);
			}
		}
	}
}
