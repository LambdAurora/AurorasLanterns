/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.behavior;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public final class ChandelierBehavior {
	public static final TagKey<Block> TAG = TagKey.create(Registries.BLOCK, AurorasLanterns.id("chandeliers"));

	public static boolean canLight(BlockState state) {
		return state.is(TAG, s -> s.hasProperty(LIT) && s.hasProperty(WATERLOGGED))
				&& !state.getValue(LIT)
				&& !state.getValue(WATERLOGGED);
	}
}
