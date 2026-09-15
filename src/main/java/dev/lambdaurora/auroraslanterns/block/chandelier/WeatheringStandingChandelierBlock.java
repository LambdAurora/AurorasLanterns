/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.chandelier;

/// Represents a weathering standing chandelier block.
///
/// @author LambdAurora
/// @version 2.2.0
/// @since 2.0.0
public class WeatheringStandingChandelierBlock extends StandingChandelierBlock implements WeatheringChandelierBlock {
	private final WeatherState weatherState;

	public WeatheringStandingChandelierBlock(WeatherState weatherState, int holders, Properties properties) {
		super(holders, properties);
		this.weatherState = weatherState;
	}

	@Override
	public WeatherState getAge() {
		return this.weatherState;
	}
}
