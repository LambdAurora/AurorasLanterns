/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin.compat;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(targets = "net.mehvahdjukaar.amendments.configs.CommonConfigs", remap = false)
public class AmendmentsConfigMixin {
	@Mutable
	@Final
	@Shadow
	public static Supplier<Boolean> WALL_LANTERN;

	@Inject(method = "<clinit>", at = @At("TAIL"), remap = false)
	private static void auroraslanterns$forceWallLanternsOff(CallbackInfo ci) {
		WALL_LANTERN = () -> false;
	}
}
