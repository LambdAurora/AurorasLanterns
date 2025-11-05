/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.LanternRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
	@Shadow
	@Final
	private String directory;

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/FileToIdConverter;json(Ljava/lang/String;)Lnet/minecraft/resources/FileToIdConverter;"))
	private void auroraslanterns$onLoadTags(
			ResourceManager resourceManager,
			CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir,
			@Local Map<Identifier, List<TagLoader.EntryWithSource>> entries
	) {
		if (this.directory.equals("tags/blocks")) {
			var list = entries.computeIfAbsent(AurorasLanterns.id("wall_lanterns"), id -> new ArrayList<>());
			LanternRegistry.streamIds()
					.map(id -> new TagLoader.EntryWithSource(TagEntry.element(id), "auroraslanterns:generated"))
					.forEach(list::add);
		}
	}
}
