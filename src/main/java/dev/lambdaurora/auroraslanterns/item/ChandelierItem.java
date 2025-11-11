/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.item;

import dev.lambdaurora.auroraslanterns.ChandelierBlocks;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class ChandelierItem extends BlockItem {
	private final ChandelierBlocks chandeliers;

	public ChandelierItem(ChandelierBlocks chandeliers, Properties properties) {
		super(chandeliers.wall().single(), properties);
		this.chandeliers = chandeliers;
	}

	@Override
	protected @Nullable BlockState getPlacementState(final BlockPlaceContext context) {
		BlockState existingState = context.getLevel().getBlockState(context.getClickedPos());
		//noinspection SuspiciousMethodCalls
		var nextIncrement = ChandelierBlocks.NEXT_BY_BLOCK.get().get(existingState.getBlock());

		if (nextIncrement != null) {
			return nextIncrement.copyStates(existingState, nextIncrement.defaultBlockState());
		}

		int holders = 1;
		var stateData = context.getItemInHand().getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		if (!stateData.isEmpty()) {
			Integer candidate = stateData.get(AbstractChandelierBlock.HOLDERS);

			if (candidate != null) {
				holders = candidate;
			}
		}

		BlockState wallState = this.chandeliers.wall().get(holders).getStateForPlacement(context);
		BlockState stateForPlacement = null;
		var level = context.getLevel();
		BlockPos pos = context.getClickedPos();

		for (Direction direction : context.getNearestLookingDirections()) {
			BlockState possibleState = switch (direction) {
				case UP -> this.chandeliers.ceiling().get(holders).getStateForPlacement(context);
				default -> wallState;
			};
			if (possibleState != null && possibleState.canSurvive(level, pos)) {
				stateForPlacement = possibleState;
				break;
			}
		}

		return stateForPlacement != null && level.isUnobstructed(stateForPlacement, pos, CollisionContext.empty())
				? stateForPlacement
				: null;
	}

	@Override
	public void registerBlocks(Map<Block, Item> blocks, Item item) {
		super.registerBlocks(blocks, item);
		this.chandeliers.forEach(block -> blocks.put(block, item));
	}
}
