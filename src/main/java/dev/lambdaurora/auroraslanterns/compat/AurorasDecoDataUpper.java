/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.compat;

import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.LanternRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Sets up the backwards compatibility of worlds that used Aurora's Decorations lanterns.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.1.0
 */
public final class AurorasDecoDataUpper {
	private static @NotNull Identifier id(@NotNull String path) {
		return Identifier.of("aurorasdeco", path);
	}

	public static void init() {
		BuiltInRegistries.BLOCK_ENTITY_TYPE.addAlias(
				id("lantern"), AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE_ID
		);

		final var amethystLanternId = id("amethyst_lantern");
		BuiltInRegistries.BLOCK.addAlias(amethystLanternId, AurorasLanternsRegistry.AMETHYST_LANTERN_ID);
		BuiltInRegistries.ITEM.addAlias(amethystLanternId, AurorasLanternsRegistry.AMETHYST_LANTERN_ID);

		final var redstoneLanternId = id("redstone_lantern");
		BuiltInRegistries.BLOCK.addAlias(redstoneLanternId, AurorasLanternsRegistry.REDSTONE_LANTERN_ID);
		BuiltInRegistries.ITEM.addAlias(redstoneLanternId, AurorasLanternsRegistry.REDSTONE_LANTERN_ID);

		LanternRegistry.forEachAndFuture((id, block) -> {
			BuiltInRegistries.BLOCK.addAlias(id(id.path()), id);
		});
	}
}
