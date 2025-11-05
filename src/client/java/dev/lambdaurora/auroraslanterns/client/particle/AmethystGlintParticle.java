/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the amethyst glint particle.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class AmethystGlintParticle extends TextureSheetParticle {
	protected AmethystGlintParticle(
			ClientLevel clientWorld,
			double x, double y, double z,
			double velocityX, double velocityY, double velocityZ
	) {
		super(clientWorld, x, y, z);
		this.hasPhysics = false;

		this.xd = velocityX;
		this.yd = velocityY * 0.15f;
		this.zd = velocityZ;

		this.setSize(0.01F, 0.01F);
		this.quadSize *= this.random.nextFloat() * 0.4F + 0.7F;
		this.lifetime = 40;
	}

	@Override
	protected int getLightColor(float tint) {
		return 0xf000f0;
	}

	@Override
	public @NotNull ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	public record Provider(@NotNull SpriteSet spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(
				SimpleParticleType parameters, ClientLevel clientWorld, double x, double y, double z,
				double velocityX, double velocityY, double velocityZ
		) {
			var random = clientWorld.random;
			var particle = new AmethystGlintParticle(
					clientWorld, x, y, z,
					0.f, random.nextDouble() * -0.1, 0.
			);
			particle.pickSprite(this.spriteProvider());
			return particle;
		}
	}
}
