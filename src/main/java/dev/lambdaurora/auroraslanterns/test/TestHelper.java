/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

class TestHelper {
	/**
	 * Expects the given block state at the given block position.
	 *
	 * @param state the expected block state
	 * @param pos   the position to check for
	 */
	static void assertBlockState(GameTestHelper context, BlockState state, BlockPos pos) {
		context.assertBlockState(
				pos,
				s -> s.equals(state),
				s -> Component.literal(
						"Expected block state %s at position %s, found %s.".formatted(state, pos.toShortString(), s)
				)
		);
	}
}
