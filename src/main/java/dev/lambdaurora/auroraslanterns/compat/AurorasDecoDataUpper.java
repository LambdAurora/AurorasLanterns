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

/**
 * Sets up the backwards compatibility of worlds that used Aurora's Decorations lanterns.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 1.1.0
 */
public final class AurorasDecoDataUpper {
	public static final String OLD_NAMESPACE = "aurorasdeco";

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(OLD_NAMESPACE, path);
	}

	public static void init() {
		final var amethystLanternId = id("amethyst_lantern");
		BuiltInRegistries.BLOCK.addAlias(amethystLanternId, AurorasLanternsRegistry.AMETHYST_LANTERN_ID);
		BuiltInRegistries.ITEM.addAlias(amethystLanternId, AurorasLanternsRegistry.AMETHYST_LANTERN_ID);

		final var redstoneLanternId = id("redstone_lantern");
		BuiltInRegistries.BLOCK.addAlias(redstoneLanternId, AurorasLanternsRegistry.REDSTONE_LANTERN_ID);
		BuiltInRegistries.ITEM.addAlias(redstoneLanternId, AurorasLanternsRegistry.REDSTONE_LANTERN_ID);

		LanternRegistry.forEachAndFuture((id, block) -> {
			BuiltInRegistries.BLOCK.addAlias(id(id.getPath()), id);
		});
	}
}
