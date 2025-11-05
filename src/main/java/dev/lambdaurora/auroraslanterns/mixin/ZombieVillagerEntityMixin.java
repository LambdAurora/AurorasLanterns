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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ZombieVillager.class)
public class ZombieVillagerEntityMixin extends Zombie {
	public ZombieVillagerEntityMixin(EntityType<? extends Zombie> entityType, Level world) {
		super(entityType, world);
	}

	@Inject(method = "getConversionProgress", at = @At("RETURN"), cancellable = true)
	private void auroraslanterns$onGetConversionProgress(CallbackInfoReturnable<Integer> cir) {
		if (this.random.nextFloat() < .35f) {
			int lanterns = (int) ((ServerLevel) this.level()).getPoiManager().getInSquare(
					poiType -> poiType.value() == AurorasLanternsRegistry.AMETHYST_LANTERN_POI,
					this.blockPosition(),
					AmethystLanternBlock.EFFECT_RADIUS,
					PoiManager.Occupancy.ANY
			).filter(poi -> {
				int y = poi.getPos().getY();
				int entityY = this.getBlockY();
				return entityY <= y + AmethystLanternBlock.EFFECT_RADIUS && entityY >= y - AmethystLanternBlock.EFFECT_RADIUS;
			}).count();
			if (lanterns > 0)
				cir.setReturnValue(lanterns + Math.min(cir.getReturnValueI(), 14));
		}
	}
}
