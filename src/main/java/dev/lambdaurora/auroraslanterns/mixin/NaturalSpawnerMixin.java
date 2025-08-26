/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.block.AmethystLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
	@Inject(
			method = "isValidSpawnPostitionForType",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void auroraslanterns$onCanSpawn(
			ServerLevel world, MobCategory category, StructureManager structureAccessor,
			ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnerData,
			BlockPos.Mutable pos, double squaredDistance, CallbackInfoReturnable<Boolean> cir
	) {
		if (cir.getReturnValueZ()) {
			if (!category.isFriendly() && world.getPoiManager().getInSquare(
					poiType -> poiType.value() == AurorasLanternsRegistry.AMETHYST_LANTERN_POI,
					pos,
					AmethystLanternBlock.EFFECT_RADIUS,
					PoiManager.Occupancy.ANY
			).anyMatch(poi -> {
				int y = poi.getPos().getY();
				return pos.getY() <= y + AmethystLanternBlock.EFFECT_RADIUS && pos.getY() >= y - AmethystLanternBlock.EFFECT_RADIUS;
			})) {
				cir.setReturnValue(false);
			}
		}
	}
}
