/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block;

import dev.lambdaurora.auroraslanterns.block.behavior.RedstoneLanternBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class RedstoneWallLanternBlock extends WallLanternBlock<RedstoneLanternBlock> {
	private final RedstoneLanternBehavior behavior = new RedstoneLanternBehavior(state -> state.getValue(FACING));

	public RedstoneWallLanternBlock(RedstoneLanternBlock lantern, Properties properties) {
		super(lantern, properties);
	}

	/* Updates */

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		super.neighborChanged(state, world, pos, block, fromPos, notify);
		this.behavior.neighborChanged(state, world, pos);
	}

	/* Ticking */

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		super.tick(state, world, pos, random);
		this.behavior.scheduledTick(state, world, pos);
	}

	/* Redstone */

	@Override
	public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return this.behavior.getWeakRedstonePower(state, world, pos, direction);
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return this.behavior.getStrongRedstonePower(state, world, pos, direction);
	}
}
