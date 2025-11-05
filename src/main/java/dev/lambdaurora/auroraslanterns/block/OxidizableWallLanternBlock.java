/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block;

import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Represents an oxidizable wall lantern block.
 *
 * @param <L> the type of the underlying lantern
 * @author LambdAurora
 * @version 1.0.2
 * @since 1.0.2
 */
public class OxidizableWallLanternBlock<L extends LanternBlock & WeatheringCopper>
		extends WallLanternBlock<L>
		implements WeatheringCopper {
	public OxidizableWallLanternBlock(L lantern, Properties properties) {
		super(lantern, properties);
	}

	@Override
	public WeatherState getAge() {
		return this.lanternBlock.getAge();
	}
}
