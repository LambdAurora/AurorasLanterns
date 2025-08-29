/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.test;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.ExtensionType;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;

import static dev.lambdaurora.auroraslanterns.test.TestHelper.assertBlockState;

public class WallLanternTest {
	@GameTest(structure = AurorasLanterns.NAMESPACE + ":wall_lantern_attachment")
	public void wallLanternAttachment(GameTestHelper context) {
		final var wallLantern = AurorasLanternsRegistry.WALL_LANTERN_BLOCK.defaultState()
				.with(WallLanternBlock.EXTENSION, ExtensionType.LOW_WALL)
				.with(WallLanternBlock.FACING, context.getTestRotation().rotate(Direction.NORTH));
		final var lanternPos = BlockPos.ofFloored(2, 2, 0);

		context.runAtTickTime(1, () -> {
			assertBlockState(context, wallLantern, lanternPos);

			context.pressButton(BlockPos.ofFloored(2, 3, 4));

			context.succeedOnTickWhen(3, () ->
					assertBlockState(context, wallLantern.with(WallLanternBlock.EXTENSION, ExtensionType.WALL), lanternPos)
			);
		});
	}
}
