/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block;

import com.mojang.serialization.MapCodec;
import dev.lambdaurora.auroraslanterns.block.behavior.RedstoneLanternBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class RedstoneWallLanternBlock extends WallLanternBlock<RedstoneLanternBlock> {
	public static final MapCodec<? extends RedstoneWallLanternBlock> CODEC
			= makeCodec(RedstoneLanternBlock.class, RedstoneWallLanternBlock::new);

	private final RedstoneLanternBehavior behavior = new RedstoneLanternBehavior(state -> state.getValue(FACING));

	public RedstoneWallLanternBlock(RedstoneLanternBlock lantern, Properties properties) {
		super(lantern, properties);
	}

	@Override
	protected MapCodec<? extends RedstoneWallLanternBlock> codec() {
		return CODEC;
	}

	/* Updates */

	@Override
	protected void neighborChanged(
			BlockState state, Level world, BlockPos pos, Block block, @Nullable Orientation orientation, boolean notify
	) {
		super.neighborChanged(state, world, pos, block, orientation, notify);
		this.behavior.neighborChanged(state, world, pos);
	}

	/* Ticking */

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		super.tick(state, world, pos, random);
		this.behavior.scheduledTick(state, world, pos);
	}

	/* Redstone */

	@Override
	protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return this.behavior.getWeakRedstonePower(state, world, pos, direction);
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return this.behavior.getStrongRedstonePower(state, world, pos, direction);
	}
}
