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

import static dev.lambdaurora.auroraslanterns.test.TestHelper.assertBlockState;

public class RedstoneLanternTest {
	private static final String PREFIX = AurorasLanterns.NAMESPACE + ":redstone_lantern/";
	private static final BlockState LIT_REDSTONE_LAMP
			= Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, true);
	private static final BlockState UNLIT_REDSTONE_LAMP
			= Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, false);

	@GameTest(template = PREFIX + "basic_connectivity", batch = "redstone_lantern")
	public void testBasicConnectivity(GameTestHelper context) {
		context.runAtTickTime(1, () -> {
			// Lamps
			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.containing(1, 1, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.containing(1, 3, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.containing(5, 1, 2));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.containing(5, 3, 2));

			// Repeaters
			assertBlockState(context, litRepeater(Direction.NORTH), BlockPos.containing(1, 2, 3));
			assertBlockState(context, litRepeater(Direction.EAST), BlockPos.containing(0, 2, 2));
			assertBlockState(context, litRepeater(Direction.SOUTH), BlockPos.containing(1, 2, 1));
			assertBlockState(context, litRepeater(Direction.WEST), BlockPos.containing(2, 2, 2));

			assertBlockState(context, litRepeater(Direction.NORTH), BlockPos.containing(5, 2, 3));
			assertBlockState(context, litRepeater(Direction.EAST), BlockPos.containing(4, 2, 2));
			assertBlockState(context, litRepeater(Direction.SOUTH), BlockPos.containing(5, 2, 1));
			assertBlockState(context, litRepeater(Direction.WEST), BlockPos.containing(6, 2, 2));

			context.succeed();
		});
	}

	@GameTest(template = PREFIX + "wall_connectivity", batch = "redstone_lantern")
	public void testWallConnectivity(GameTestHelper context) {
		context.runAtTickTime(1, () -> {
			BlockState xRedstone = Blocks.REDSTONE_WIRE.defaultBlockState()
					.setValue(BlockStateProperties.POWER, 15)
					.setValue(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE)
					.setValue(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE)
					.setValue(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.NONE)
					.setValue(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.NONE);

			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.containing(1, 1, 1));
			assertBlockState(context, LIT_REDSTONE_LAMP, BlockPos.containing(1, 3, 1));
			assertBlockState(context, UNLIT_REDSTONE_LAMP, BlockPos.containing(1, 2, 2));

			assertBlockState(context, xRedstone, BlockPos.containing(0, 2, 1));
			assertBlockState(context, xRedstone, BlockPos.containing(2, 2, 1));
			assertBlockState(context,
					Blocks.REDSTONE_WIRE.defaultBlockState()
							.setValue(BlockStateProperties.POWER, 15)
							.setValue(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE)
							.setValue(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE)
							.setValue(BlockStateProperties.EAST_REDSTONE, RedstoneSide.NONE)
							.setValue(BlockStateProperties.WEST_REDSTONE, RedstoneSide.NONE),
					BlockPos.containing(1, 2, 0)
			);

			context.succeed();
		});
	}

	@GameTest(template = PREFIX + "tower", batch = "redstone_lantern")
	public void testTower(GameTestHelper context) {
		BlockState lit = AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK.defaultBlockState()
				.setValue(BlockStateProperties.HANGING, true)
				.setValue(BlockStateProperties.LIT, true);
		BlockState unlit = AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK.defaultBlockState()
				.setValue(BlockStateProperties.HANGING, true)
				.setValue(BlockStateProperties.LIT, false);

		assertBlockState(context, lit, BlockPos.containing(1, 4, 1));
		assertBlockState(context, unlit, BlockPos.containing(1, 2, 1));

		context.pressButton(BlockPos.containing(1, 6, 1));

		context.succeedOnTickWhen(4, () -> {
			assertBlockState(context, unlit, BlockPos.containing(1, 4, 1));
			assertBlockState(context, lit, BlockPos.containing(1, 2, 1));
		});
	}

	private static BlockState litRepeater(Direction facing) {
		return Blocks.REPEATER.defaultBlockState()
				.setValue(BlockStateProperties.POWERED, true)
				.setValue(BlockStateProperties.LOCKED, false)
				.setValue(RepeaterBlock.FACING, facing)
				.setValue(BlockStateProperties.DELAY, 1);
	}
}
