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
import dev.lambdaurora.auroraslanterns.block.behavior.ChandelierBehavior;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlintAndSteelItem.class)
public class FlintAndSteelItemMixin {
	@WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CandleBlock;canLight(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
	private boolean auroraslanterns$useOn$chandelierCheck(BlockState state, Operation<Boolean> original) {
		return original.call(state) || ChandelierBehavior.canLight(state);
	}
}
