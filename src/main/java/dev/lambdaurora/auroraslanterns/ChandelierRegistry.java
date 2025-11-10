/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import dev.lambdaurora.auroraslanterns.block.chandelier.CeilingChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.WallChandelierBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

public final class ChandelierRegistry {
	private static Block getCandleByColor(@Nullable DyeColor color) {
		if (color == null) {
			return Blocks.CANDLE;
		}

		return switch (color) {
			case WHITE -> Blocks.WHITE_CANDLE;
			case ORANGE -> Blocks.ORANGE_CANDLE;
			case MAGENTA -> Blocks.MAGENTA_CANDLE;
			case LIGHT_BLUE -> Blocks.LIGHT_BLUE_CANDLE;
			case YELLOW -> Blocks.YELLOW_CANDLE;
			case LIME -> Blocks.LIME_CANDLE;
			case PINK -> Blocks.PINK_CANDLE;
			case GRAY -> Blocks.GRAY_CANDLE;
			case LIGHT_GRAY -> Blocks.LIGHT_GRAY_CANDLE;
			case CYAN -> Blocks.CYAN_CANDLE;
			case PURPLE -> Blocks.PURPLE_CANDLE;
			case BLUE -> Blocks.BLUE_CANDLE;
			case BROWN -> Blocks.BROWN_CANDLE;
			case GREEN -> Blocks.GREEN_CANDLE;
			case RED -> Blocks.RED_CANDLE;
			case BLACK -> Blocks.BLACK_CANDLE;
			default -> Blocks.CANDLE;
		};
	}

	static BlockBehaviour.Properties getProperties(Block source, SoundType soundType, MapColor mapColor) {
		return BlockBehaviour.Properties.ofFullCopy(source)
				.strength(.5f, .8f)
				.mapColor(mapColor)
				.sound(soundType)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
				.lightLevel(state -> Math.clamp(CandleBlock.LIGHT_EMISSION.applyAsInt(state) + 2, 0, 15));
	}

	public record Type(
			CeilingChandelierBlock ceiling,
			WallChandelierBlock wall
	) {
		public Stream<Block> streamBlocks() {
			return Stream.of(this.ceiling, this.wall);
		}
	}

	public record Entry(Type iron, Type copper, Type exposedCopper, Type weatheredCopper, Type oxidizedCopper) {
		public Stream<Block> streamBlocks() {
			return Stream.concat(
					this.iron.streamBlocks(),
					Stream.concat(
							this.copper.streamBlocks(),
							Stream.concat(
									this.exposedCopper.streamBlocks(),
									Stream.concat(
											this.weatheredCopper.streamBlocks(),
											this.oxidizedCopper.streamBlocks()
									)
							)
					)
			);
		}
	}
}
