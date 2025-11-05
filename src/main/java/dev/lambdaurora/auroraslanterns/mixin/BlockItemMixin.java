/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import dev.lambdaurora.auroraslanterns.accessor.BlockItemAccessor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds wall lantern to the lantern block item.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Mixin(value = BlockItem.class, priority = 960)
public abstract class BlockItemMixin extends Item implements BlockItemAccessor {
	private BlockItemMixin(Item.Properties properties) {
		super(properties);
	}

	@Shadow
	public abstract Block getBlock();

	@Unique
	private Block auroraslanterns$wallBlock = null;


	@Override
	public void auroraslanterns$setWallBlock(@NonNull Block block) {
		this.auroraslanterns$wallBlock = block;
	}

	@Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
	private void auroraslanterns$onGetPlacementState(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
		var placementDirections = context.getNearestLookingDirections();
		var world = context.getLevel();
		var pos = context.getClickedPos();
		var placedState = world.getBlockState(pos);

		if (this.auroraslanterns$wallBlock != null) {
			var wallState = this.auroraslanterns$wallBlock.getStateForPlacement(context);
			BlockState resultState = null;

			for (var direction : context.getNearestLookingDirections()) {
				var state = direction.getAxis().isVertical() ? this.getBlock().getStateForPlacement(context) : wallState;
				if (state != null && state.canSurvive(world, pos)) {
					resultState = state;
					break;
				}
			}

			if (resultState != null && world.isUnobstructed(resultState, pos, CollisionContext.empty()))
				cir.setReturnValue(resultState);
		}
	}
}
