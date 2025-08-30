/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import net.minecraft.resources.io.MultiPackResourceManager;
import net.minecraft.server.packs.PackResources;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(MultiPackResourceManager.class)
public interface MultiPackResourceManagerAccessor {
	@Accessor
	List<PackResources> getPacks();

	@Mutable
	@Final
	@Accessor
	void setPacks(List<PackResources> packs);
}
