/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.accessor;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;

public interface BlockItemAccessor {
	@ApiStatus.Internal
	void auroraslanterns$setWallBlock(Block block);
}
