/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry.AMETHYST_LANTERN_BLOCK;
import static dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry.AMETHYST_WALL_LANTERN_BLOCK;

@Mixin({Giant.class, Monster.class, Pillager.class})
public class AvoidAmethystLanternMixin {
	@Inject(method = "getWalkTargetValue", at = @At("RETURN"), cancellable = true)
	private void auroraslanterns$onGetWalkTargetValue(BlockPos pos, LevelReader world, CallbackInfoReturnable<Float> cir) {
		var state = world.getBlockState(pos);
		if (state.is(AMETHYST_LANTERN_BLOCK) || state.is(AMETHYST_WALL_LANTERN_BLOCK)) {
			cir.setReturnValue(-30.f);
		}
	}
}
