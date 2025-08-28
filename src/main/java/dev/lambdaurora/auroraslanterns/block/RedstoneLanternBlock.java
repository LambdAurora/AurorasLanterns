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
import dev.lambdaurora.auroraslanterns.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a redstone lantern block.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class RedstoneLanternBlock extends LanternBlock {
	public static final MapCodec<RedstoneLanternBlock> CODEC = simpleCodec(RedstoneLanternBlock::new);

	private final RedstoneLanternBehavior behavior
			= new RedstoneLanternBehavior(state -> state.get(HANGING) ? Direction.DOWN : Direction.UP);

	public RedstoneLanternBlock(Properties properties) {
		super(properties);

		this.setDefaultState(this.defaultState().with(RedstoneLanternBehavior.LIT, true));
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public @NotNull MapCodec<LanternBlock> codec() {
		return (MapCodec) CODEC;
	}

	@Override
	protected void createStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createStateDefinition(builder);
		builder.add(RedstoneLanternBehavior.LIT);
	}

	/* Updates */

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		for (var direction : Utils.DIRECTIONS) {
			world.updateNeighborsAt(pos.relative(direction), this);
		}
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if (!moved) {
			for (var direction : Utils.DIRECTIONS) {
				world.updateNeighborsAt(pos.relative(direction), this);
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		super.neighborChanged(state, world, pos, block, fromPos, notify);
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
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return this.behavior.getWeakRedstonePower(state, world, pos, direction);
	}

	@Override
	protected int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return this.behavior.getStrongRedstonePower(state, world, pos, direction);
	}

	/* Visual */

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (RedstoneLanternBehavior.isLit(state)) {
			double x = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.75;
			double y = (double) pos.getY() + 0.25 + (random.nextDouble() - 0.5) * 0.4;
			double z = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.75;
			world.addParticle(DustParticleOptions.REDSTONE, x, y, z, 0.0, 0.0, 0.0);
		}
	}
}
