/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import dev.lambdaurora.auroraslanterns.accessor.BlockEntityTypeAccessor;
import dev.yumi.commons.collections.YumiCollections;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin implements BlockEntityTypeAccessor {
	@Mutable
	@Shadow
	@Final
	private Set<Block> validBlocks;

	@Override
	public void auroraslanterns$addSupportedBlock(@NonNull Block block) {
		this.validBlocks = YumiCollections.concat(this.validBlocks, Set.of(block));
	}
}
