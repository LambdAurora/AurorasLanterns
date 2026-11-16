/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.chandelier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/// Represents a weathering wall chandelier block.
///
/// @author LambdAurora
/// @version 2.2.1
/// @since 2.0.0
public class WeatheringWallChandelierBlock extends WallChandelierBlock implements WeatheringChandelierBlock {
	public static final MapCodec<WeatheringWallChandelierBlock> CODEC = WeatheringChandelierBlock.makeCodec(WeatheringWallChandelierBlock::new, propertiesCodec());
	private final WeatherState weatherState;

	public WeatheringWallChandelierBlock(WeatherState weatherState, int holders, Properties properties) {
		super(holders, properties);
		this.weatherState = weatherState;
	}

	@Override
	public MapCodec<WeatheringWallChandelierBlock> codec() {
		return CODEC;
	}

	@Override
	public void randomTick(final BlockState state, final ServerLevel level, final BlockPos pos, final RandomSource random) {
		this.changeOverTime(state, level, pos, random);
	}

	@Override
	public boolean isRandomlyTicking(final BlockState state) {
		return WeatheringCopper.getNext(state.getBlock()).isPresent();
	}

	@Override
	public WeatherState getAge() {
		return this.weatherState;
	}
}
