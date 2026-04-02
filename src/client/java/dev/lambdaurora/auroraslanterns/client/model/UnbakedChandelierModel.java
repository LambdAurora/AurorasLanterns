/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.model;

import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock.Candle;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record UnbakedChandelierModel(
		Map<Candle, BlockStateModel.@Nullable UnbakedRoot[]> data,
		BlockStateModel.UnbakedRoot wrapped
)
		implements BlockStateModel.UnbakedRoot {
	private static final Logger LOGGER = LoggerFactory.getLogger(UnbakedChandelierModel.class);

	@Override
	public Object visualEqualityGroup(BlockState state) {
		return this.wrapped.visualEqualityGroup(state);
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.wrapped.resolveDependencies(resolver);
		this.data.values().stream()
				.flatMap(Arrays::stream)
				.filter(Objects::nonNull)
				.forEach(model -> model.resolveDependencies(resolver));
	}

	@Override
	public BlockStateModel bake(BlockState state, ModelBaker baker) {
		var candles = this.data.entrySet().stream()
				.map(entry -> {
					var baked = new BlockStateModel[entry.getValue().length];
					for (int i = 0; i < entry.getValue().length; i++) {
						var unbaked = entry.getValue()[i];

						if (unbaked != null) {
							baked[i] = unbaked.bake(state, baker);
						} else {
							LOGGER.warn("Could not find {} candle model for {} with index {}",
									entry.getKey().name(), state, i
							);
						}
					}
					return Map.entry(entry.getKey(), baked);
				})
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return new BakedChandelierModel(
				this.wrapped.bake(state, baker),
				candles
		);
	}
}
