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
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Text;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static dev.lambdaurora.auroraslanterns.test.TestHelper.assertBlockState;

/**
 * Tests the Redstone Lantern behavior.
 *
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
public class RedstoneLanternTest {
	private static final String PREFIX = AurorasLanterns.NAMESPACE + ":redstone_lantern/";
	private static final BlockState LIT_REDSTONE_LAMP
			= Blocks.REDSTONE_LAMP.defaultState().with(BlockStateProperties.LIT, true);
	private static final BlockState UNLIT_REDSTONE_LAMP
			= Blocks.REDSTONE_LAMP.defaultState().with(BlockStateProperties.LIT, false);

	@GameTest(structure = PREFIX + "basic_connectivity")
	public void testBasicConnectivity(GameTestHelper context) {
		context.runAtTickTime(1, () -> {
			// Lamps
			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 0, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 2, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(5, 0, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(5, 2, 2));

			// Repeaters
			assertBlockState(context, litRepeater(context, Direction.NORTH), BlockPos.ofFloored(1, 1, 3));
			assertBlockState(context, litRepeater(context, Direction.EAST), BlockPos.ofFloored(0, 1, 2));
			assertBlockState(context, litRepeater(context, Direction.SOUTH), BlockPos.ofFloored(1, 1, 1));
			assertBlockState(context, litRepeater(context, Direction.WEST), BlockPos.ofFloored(2, 1, 2));

			assertBlockState(context, litRepeater(context, Direction.NORTH), BlockPos.ofFloored(5, 1, 3));
			assertBlockState(context, litRepeater(context, Direction.EAST), BlockPos.ofFloored(4, 1, 2));
			assertBlockState(context, litRepeater(context, Direction.SOUTH), BlockPos.ofFloored(5, 1, 1));
			assertBlockState(context, litRepeater(context, Direction.WEST), BlockPos.ofFloored(6, 1, 2));

			context.succeed();
		});
	}

	@GameTest(structure = PREFIX + "wall_connectivity")
	public void testWallConnectivity(GameTestHelper context) {
		context.runAtTickTime(1, () -> {
			assertRedstoneSignal(context, BlockPos.ofFloored(2, 1, 1), Direction.WEST);
			assertRedstoneSignal(context, BlockPos.ofFloored(0, 1, 1), Direction.EAST);
			assertRedstoneSignal(context, BlockPos.ofFloored(1, 1, 0), Direction.SOUTH);

			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 0, 1));
			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 2, 1));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 1, 2));

			context.succeed();
		});
	}

	@GameTest(structure = PREFIX + "tower")
	public void testTower(GameTestHelper context) {
		BlockState lit = AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK.defaultState()
				.with(BlockStateProperties.HANGING, true)
				.with(BlockStateProperties.LIT, true);
		BlockState unlit = AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK.defaultState()
				.with(BlockStateProperties.HANGING, true)
				.with(BlockStateProperties.LIT, false);

		assertBlockState(context, lit, BlockPos.ofFloored(1, 3, 1));
		assertBlockState(context, unlit, BlockPos.ofFloored(1, 1, 1));

		context.pressButton(BlockPos.ofFloored(1, 5, 1));

		context.succeedOnTickWhen(4, () -> {
			assertBlockState(context, unlit, BlockPos.ofFloored(1, 3, 1));
			assertBlockState(context, lit, BlockPos.ofFloored(1, 1, 1));
		});
	}

	private static BlockState litRepeater(GameTestHelper context, Direction facing) {
		return Blocks.REPEATER.defaultState()
				.with(BlockStateProperties.POWERED, true)
				.with(BlockStateProperties.LOCKED, false)
				.with(RepeaterBlock.FACING, context.getTestRotation().rotate(facing))
				.with(BlockStateProperties.DELAY, 1);
	}

	private static void assertRedstoneSignal(GameTestHelper context, BlockPos pos, Direction direction) {
		var rotated = context.getTestRotation().rotate(direction);
		context.assertRedstoneSignal(
				pos,
				rotated,
				value -> value == 15,
				() -> Text.literal("Missing redstone signal at %s towards %s!".formatted(pos, rotated))
		);
	}
}
