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

/**
 * Represents a weathering standing chandelier block.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public class WeatheringStandingChandelierBlock extends StandingChandelierBlock implements WeatheringChandelierBlock {
	public static final MapCodec<WeatheringStandingChandelierBlock> CODEC = WeatheringChandelierBlock.makeCodec(WeatheringStandingChandelierBlock::new, propertiesCodec());

	private final WeatherState weatherState;

	public WeatheringStandingChandelierBlock(WeatherState weatherState, int holders, Properties properties) {
		super(holders, properties);
		this.weatherState = weatherState;
	}

	@Override
	public MapCodec<WeatheringStandingChandelierBlock> codec() {
		return CODEC;
	}

	@Override
	public WeatherState getAge() {
		return this.weatherState;
	}
}
