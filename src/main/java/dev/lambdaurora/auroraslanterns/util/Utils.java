/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class Utils {
	public static final List<Direction> DIRECTIONS = List.of(Direction.values());

	private Utils() {
		throw new UnsupportedOperationException("Utils only contains static definitions.");
	}

	public static boolean isShapeEqual(AABB s1, AABB s2) {
		return s1.minX == s2.minX && s1.minY == s2.minY && s1.minZ == s2.minZ
				&& s1.maxX == s2.maxX && s1.maxY == s2.maxY && s1.maxZ == s2.maxZ;
	}
}
