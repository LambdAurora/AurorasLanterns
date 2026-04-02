/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.chandelier;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;

public interface WeatheringChandelierBlock extends WeatheringCopper {
	static <C extends AbstractChandelierBlock & WeatheringChandelierBlock> MapCodec<C> makeCodec(Factory<C> instantiator, RecordCodecBuilder<C, BlockBehaviour.Properties> propertiesCodec) {
		return RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state").forGetter(WeatheringCopper::getAge),
								ExtraCodecs.intRange(1, 4).fieldOf("holders").forGetter(AbstractChandelierBlock::holders),
								propertiesCodec
						)
						.apply(instance, instantiator::create)
		);
	}

	interface Factory<C extends AbstractChandelierBlock> {
		C create(WeatherState weatherState, int holders, BlockBehaviour.Properties properties);
	}
}
