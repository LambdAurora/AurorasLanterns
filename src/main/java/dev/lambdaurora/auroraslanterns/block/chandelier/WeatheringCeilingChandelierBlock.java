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
 * Represents a weathering ceiling chandelier block.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public class WeatheringCeilingChandelierBlock extends CeilingChandelierBlock implements WeatheringChandelierBlock {
	public static final MapCodec<WeatheringCeilingChandelierBlock> CODEC = WeatheringChandelierBlock.makeCodec(WeatheringCeilingChandelierBlock::new, propertiesCodec());

	private final WeatherState weatherState;

	public WeatheringCeilingChandelierBlock(WeatherState weatherState, int holders, Properties properties) {
		super(holders, properties);
		this.weatherState = weatherState;
	}

	@Override
	public MapCodec<WeatheringCeilingChandelierBlock> codec() {
		return CODEC;
	}

	@Override
	public WeatherState getAge() {
		return this.weatherState;
	}
}
