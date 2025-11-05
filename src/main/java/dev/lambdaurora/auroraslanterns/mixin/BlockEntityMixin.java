/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
	@WrapOperation(
			method = "loadStatic",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/Registry;getOptional(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;"
			)
	)
	private static Optional auroraslanterns$backwardsCompat(
			Registry<BlockEntity> instance, @NotNull Identifier id, Operation<Optional> original
	) {
		if (id.getNamespace().equals("aurorasdeco") && id.getPath().equals("lantern")) {
			return Optional.of(AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE);
		} else {
			return original.call(instance, id);
		}
	}
}
