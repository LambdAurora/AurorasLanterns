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
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/**
 * Represents the amethyst glint particle.
 *
 * @author LambdAurora
 * @version 1.4.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class AmethystGlintParticle extends SingleQuadParticle {
	protected AmethystGlintParticle(
			ClientLevel clientWorld,
			double x, double y, double z,
			double velocityX, double velocityY, double velocityZ,
			TextureAtlasSprite sprite
	) {
		super(clientWorld, x, y, z, sprite);
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
	protected Layer getLayer() {
		return Layer.OPAQUE;
	}

	public record Provider(SpriteSet spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@Override
		public SingleQuadParticle createParticle(
				SimpleParticleType parameters, ClientLevel clientWorld, double x, double y, double z,
				double velocityX, double velocityY, double velocityZ, RandomSource random
		) {
			return new AmethystGlintParticle(
					clientWorld, x, y, z,
					0.f, random.nextDouble() * -0.1, 0.f,
					this.spriteProvider.get(random)
			);
		}
	}
}
