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
import dev.lambdaurora.auroraslanterns.LanternRegistry;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;

import static dev.lambdaurora.auroraslanterns.test.TestHelper.assertBlockState;

public class WallLanternTest {
	@GameTest(template = FabricGameTest.EMPTY_STRUCTURE, batch = "wall_lantern")
	public void tagTest(GameTestHelper context) {
		final var tag = TagKey.create(Registries.BLOCK, AurorasLanterns.id("wall_lanterns"));

		LanternRegistry.forEach((id, block) -> {
			var state = block.defaultBlockState();

			context.assertTrue(
					state.is(tag),
					"Wall lantern %s is not part of the auroraslanterns:wall_lanterns tag.".formatted(id)
			);
			context.assertTrue(
					state.is(BlockTags.MINEABLE_WITH_PICKAXE),
					"Wall lantern %s is not part of the pickaxe mineable tag.".formatted(id)
			);
		});

		context.succeed();
	}

	@GameTest(template = AurorasLanterns.NAMESPACE + ":wall_lantern_attachment", batch = "wall_lantern")
	public void wallLanternAttachment(GameTestHelper context) {
		final var wallLantern = AurorasLanternsRegistry.WALL_LANTERN_BLOCK.defaultBlockState()
				.setValue(WallLanternBlock.EXTENSION, ExtensionType.LOW_WALL)
				.setValue(WallLanternBlock.FACING, Direction.NORTH);
		final var lanternPos = BlockPos.containing(2, 3, 0);

		context.runAtTickTime(1, () -> {
			assertBlockState(context, wallLantern, lanternPos);

			context.pressButton(BlockPos.containing(2, 4, 4));

			context.succeedOnTickWhen(3, () ->
					assertBlockState(context, wallLantern.setValue(WallLanternBlock.EXTENSION, ExtensionType.WALL), lanternPos)
			);
		});
	}
}
