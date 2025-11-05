/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Redstone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Contains most of the redstone lantern behavior logic.
 * <p>
 * The logic should:
 * <ul>
 *     <li>never transmit power upward</li>
 *     <li>respect {@link RedstoneTorchBlock}'s burnout logic</li>
 *     <li>never power the attachment block</li>
 *     <li>if hung from the ceiling or from the sides, then it should transmit power to the surrounding and strongly downwards</li>
 * </ul>
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RedstoneLanternBehavior {
	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	private final Map<BlockGetter, List<BurnoutEntry>> burnoutMap = new WeakHashMap<>();
	private final Function<BlockState, Direction> attachmentDirection;

	public RedstoneLanternBehavior(Function<BlockState, Direction> attachmentDirection) {
		this.attachmentDirection = attachmentDirection;
	}

	public static boolean isLit(BlockState state) {
		return state.getValue(LIT);
	}

	public void neighborChanged(BlockState state, Level world, BlockPos pos) {
		if (isLit(state) == this.shouldUnpower(world, pos, state) && !world.getBlockTicks().willTickThisTick(pos, state.getBlock())) {
			world.scheduleTick(pos, state.getBlock(), 2);
		}
	}

	public int getWeakRedstonePower(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		// This is needed to never power anything upward.
		if (this.attachmentDirection.apply(state) == Direction.UP && direction == Direction.DOWN) {
			return Redstone.SIGNAL_NONE;
		}

		return state.getValue(LIT) && this.attachmentDirection.apply(state) != direction ? Redstone.SIGNAL_MAX : Redstone.SIGNAL_NONE;
	}

	public int getStrongRedstonePower(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return direction == Direction.UP ? state.getSignal(world, pos, direction) : Redstone.SIGNAL_NONE;
	}

	/**
	 * {@return {@code true} if the given redstone lantern state should power off, or {@code false} otherwise}
	 *
	 * @param world the world the redstone lantern is in
	 * @param pos the position of the redstone lantern
	 * @param state the block state of the redstone lantern
	 */
	private boolean shouldUnpower(SignalGetter world, BlockPos pos, BlockState state) {
		Direction direction = this.attachmentDirection.apply(state).getOpposite();
		return world.hasSignal(pos.relative(direction), direction);
	}

	public void scheduledTick(BlockState state, ServerLevel world, BlockPos pos) {
		boolean shouldUnpower = this.shouldUnpower(world, pos, state);
		List<BurnoutEntry> list = this.burnoutMap.get(world);

		while (list != null && !list.isEmpty() && world.getGameTime() - list.get(0).time > RedstoneTorchBlock.RECENT_TOGGLE_TIMER) {
			list.remove(0);
		}

		if (isLit(state)) {
			if (shouldUnpower) {
				world.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);

				if (this.isBurnedOut(world, pos, true)) {
					world.levelEvent(LevelEvent.REDSTONE_TORCH_BURNOUT, pos, 0);
					world.scheduleTick(pos, world.getBlockState(pos).getBlock(), RedstoneTorchBlock.RESTART_DELAY);
				}
			}
		} else if (!shouldUnpower && !this.isBurnedOut(world, pos, false)) {
			world.setBlock(pos, state.setValue(LIT, true), Block.UPDATE_ALL);
		}
	}

	private boolean isBurnedOut(Level world, BlockPos pos, boolean addNew) {
		var list = this.burnoutMap.computeIfAbsent(world, w -> new ArrayList<>());

		if (addNew) {
			list.add(new BurnoutEntry(pos.immutable(), world.getGameTime()));
		}

		int i = 0;

		for (var burnoutEntry : list) {
			if (burnoutEntry.pos().equals(pos) && ++i >= RedstoneTorchBlock.MAX_RECENT_TOGGLES) {
				return true;
			}
		}

		return false;
	}

	public record BurnoutEntry(BlockPos pos, long time) {}
}
