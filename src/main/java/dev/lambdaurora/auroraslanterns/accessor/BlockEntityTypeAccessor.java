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

public interface BlockEntityTypeAccessor {
	void auroraslanterns$addSupportedBlock(Block block);
}
