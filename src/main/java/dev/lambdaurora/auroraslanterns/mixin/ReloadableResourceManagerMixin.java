/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import dev.lambdaurora.auroraslanterns.resource.AurorasLanternsRuntimeDatagen;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
	@Shadow
	@Final
	private PackType type;

	@ModifyArgs(
			method = "createReload",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/packs/resources/MultiPackResourceManager;<init>(Lnet/minecraft/server/packs/PackType;Ljava/util/List;)V"
			)
	)
	private void auroraslanterns$onInjectResourcePack(Args args) {
		args.set(1, AurorasLanternsRuntimeDatagen.inject(this.type, args.get(1)));
	}
}
