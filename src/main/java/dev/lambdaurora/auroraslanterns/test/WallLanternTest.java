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
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Text;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;

import static dev.lambdaurora.auroraslanterns.test.TestHelper.assertBlockState;

public class WallLanternTest {
	@GameTest
	public void tagTest(GameTestHelper context) {
		final var tag = TagKey.of(Registries.BLOCK, AurorasLanterns.id("wall_lanterns"));

		LanternRegistry.forEach((id, block) -> {
			var state = block.defaultState();

			context.assertTrue(
					state.is(tag),
					Text.literal("Wall lantern %s is not part of the auroraslanterns:wall_lanterns tag.".formatted(id))
			);
			context.assertTrue(
					state.is(BlockTags.MINEABLE_WITH_PICKAXE),
					Text.literal("Wall lantern %s is not part of the pickaxe mineable tag.".formatted(id))
			);
		});

		context.succeed();
	}

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
