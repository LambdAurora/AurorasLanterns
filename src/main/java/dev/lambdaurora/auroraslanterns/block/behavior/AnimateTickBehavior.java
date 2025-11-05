/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.behavior;

import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a block-bound animate tick behavior.
 *
 * @param <T> the block bound to this behavior
 * @author LambdAurora
 * @version 1.0.1
 * @since 1.0.1
 */
public interface AnimateTickBehavior<T extends Block> {
	AnimateTickBehavior<?> DEFAULT = Block::animateTick;

	void animateTick(T block, BlockState state, Level world, BlockPos pos, RandomSource random);

	@SuppressWarnings("unchecked")
	static <T extends Block> AnimateTickBehavior<T> defaultBehavior() {
		return (AnimateTickBehavior<T>) DEFAULT;
	}

	static <T extends LanternBlock> AnimateTickBehavior<T> lanternBehavior(T block) {
		if (block.getClass().getName().equals("juuxel.adorn.block.CandlelitLanternBlock")) {
			// Adorn's Candlelit Lantern has a flame, but by default it's too low.
			return (currentBlock, state, world, pos, random) -> {
				var lantern = AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.getBlockEntity(world, pos);

				if (lantern != null) {
					float angle = lantern.computeAngle();
					var vec = Vec3.upFromBottomCenterOf(pos, 1);
					float x = 0.f;
					float z = 0.f;

					if ((lantern.isSwinging() || lantern.isColliding()) && lantern.getSwingBaseDirection() != null) {
						switch (lantern.getSwingBaseDirection()) {
							case NORTH -> z = Mth.sin(angle);
							case SOUTH -> z = Mth.sin(-angle);
							case EAST -> x = Mth.sin(-angle);
							case WEST -> x = Mth.sin(angle);
						}
					} else {
						if (lantern.getBlockState().getValue(WallLanternBlock.FACING).getAxis() == Direction.Axis.Z) x = Mth.sin(angle);
						else z = Mth.sin(angle);
					}

					x *= 4 / 16.f;
					z *= 4 / 16.f;
					float y = -(Mth.cos(angle) * 9 / 16);

					AbstractCandleBlock.addParticlesAndSound(world, vec.add(x, y, z), random);
				} else {
					defaultBehavior().animateTick(currentBlock, state, world, pos, random);
				}
			};
		} else {
			return defaultBehavior();
		}
	}
}
