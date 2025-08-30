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
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.resources.io.ResourceManager;
import net.minecraft.resources.io.ResourceType;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
	@Inject(method = "loadResources", at = @At("HEAD"))
	private static void onLoadResources(
			ResourceManager resourceManager,
			LayeredRegistryAccess<RegistryLayer> registries,
			List<Registry.PendingTags<?>> pendingTags,
			FeatureFlagSet featureFlags,
			Commands.CommandSelection commandSelection,
			int level,
			Executor prepareExecutor,
			Executor applyExecutor,
			CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir
	) {
		if (resourceManager instanceof MultiPackResourceManagerAccessor multiPackResourceManager) {
			multiPackResourceManager.setPacks(
					AurorasLanternsRuntimeDatagen.inject(ResourceType.SERVER_DATA, multiPackResourceManager.getPacks())
			);
		}
	}
}
