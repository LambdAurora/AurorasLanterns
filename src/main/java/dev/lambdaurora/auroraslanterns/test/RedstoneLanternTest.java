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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.jetbrains.annotations.NotNull;

public class RedstoneLanternTest {
	private static final String PREFIX = AurorasLanterns.NAMESPACE + ":redstone_lantern/";
	private static final BlockState LIT_REDSTONE_LAMP
			= Blocks.REDSTONE_LAMP.defaultState().with(BlockStateProperties.LIT, true);
	private static final BlockState UNLIT_REDSTONE_LAMP
			= Blocks.REDSTONE_LAMP.defaultState().with(BlockStateProperties.LIT, false);

	@GameTest(template = PREFIX + "basic_connectivity", batch = "redstone_lantern")
	public void testBasicConnectivity(GameTestHelper context) {
		context.runAtTickTime(1, () -> {
			// Lamps
			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 1, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 3, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(5, 1, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(5, 3, 2));

			// Repeaters
			assertBlockState(context, litRepeater(Direction.NORTH), BlockPos.ofFloored(1, 2, 3));
			assertBlockState(context, litRepeater(Direction.EAST), BlockPos.ofFloored(0, 2, 2));
			assertBlockState(context, litRepeater(Direction.SOUTH), BlockPos.ofFloored(1, 2, 1));
			assertBlockState(context, litRepeater(Direction.WEST), BlockPos.ofFloored(2, 2, 2));

			assertBlockState(context, litRepeater(Direction.NORTH), BlockPos.ofFloored(5, 2, 3));
			assertBlockState(context, litRepeater(Direction.EAST), BlockPos.ofFloored(4, 2, 2));
			assertBlockState(context, litRepeater(Direction.SOUTH), BlockPos.ofFloored(5, 2, 1));
			assertBlockState(context, litRepeater(Direction.WEST), BlockPos.ofFloored(6, 2, 2));

			context.succeed();
		});
	}

	@GameTest(template = PREFIX + "wall_connectivity", batch = "redstone_lantern")
	public void testWallConnectivity(GameTestHelper context) {
		context.runAtTickTime(1, () -> {
			BlockState xRedstone = Blocks.REDSTONE_WIRE.defaultState()
					.with(BlockStateProperties.POWER, 15)
					.with(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE)
					.with(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE)
					.with(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
					.with(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE);

			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 1, 1));
			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 3, 1));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.ofFloored(1, 2, 2));

			assertBlockState(context, xRedstone, BlockPos.ofFloored(0, 2, 1));
			assertBlockState(context, xRedstone, BlockPos.ofFloored(2, 2, 1));
			assertBlockState(context,
					Blocks.REDSTONE_WIRE.defaultState()
							.with(BlockStateProperties.POWER, 15)
							.with(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE)
							.with(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE)
							.with(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
							.with(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
					BlockPos.ofFloored(1, 2, 0)
			);

			context.succeed();
		});
	}

	@GameTest(template = PREFIX + "tower", batch = "redstone_lantern")
	public void testTower(GameTestHelper context) {
		BlockState lit = AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK.defaultState()
				.with(BlockStateProperties.HANGING, true)
				.with(BlockStateProperties.LIT, true);
		BlockState unlit = AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK.defaultState()
				.with(BlockStateProperties.HANGING, true)
				.with(BlockStateProperties.LIT, false);

		assertBlockState(context, lit, BlockPos.ofFloored(1, 4, 1));
		assertBlockState(context, unlit, BlockPos.ofFloored(1, 2, 1));

		context.pressButton(BlockPos.ofFloored(1, 6, 1));

		context.succeedOnTickWhen(4, () -> {
			assertBlockState(context, unlit, BlockPos.ofFloored(1, 4, 1));
			assertBlockState(context, lit, BlockPos.ofFloored(1, 2, 1));
		});
	}

	private static BlockState litRepeater(Direction facing) {
		return Blocks.REPEATER.defaultState()
				.with(BlockStateProperties.POWERED, true)
				.with(BlockStateProperties.LOCKED, false)
				.with(RepeaterBlock.FACING, facing)
				.with(BlockStateProperties.DELAY, 1);
	}

	/**
	 * Expects the given block state at the given block position.
	 *
	 * @param state the expected block state
	 * @param pos   the position to check for
	 */
	public static void assertBlockState(GameTestHelper context, @NotNull BlockState state, @NotNull BlockPos pos) {
		context.assertBlockState(
				pos,
				s -> s.equals(state),
				() -> "Expected block state " + state + " at position " + pos.toShortString() + '.'
		);
	}
}
