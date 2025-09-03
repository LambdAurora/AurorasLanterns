/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents an amethyst lantern block.
 *
 * @author LambdAurora
 * @version 1.0.2
 * @since 1.0.0
 */
public class AmethystLanternBlock extends LanternBlock {
	public static final Identifier BLOCK_TEXTURE = AurorasLanterns.id("block/amethyst_lantern");
	public static final Identifier HANGING_MODEL = AurorasLanterns.id("block/hanging_amethyst_lantern");
	public static final int EFFECT_RADIUS = 32;

	public AmethystLanternBlock(Properties properties) {
		super(properties);
	}

	/* Visual */

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (random.nextBoolean()) {
			double x = pos.getX() + random.nextFloat();
			double y = pos.getY() + random.nextFloat();
			double z = pos.getZ() + random.nextFloat();
			world.addParticle(AurorasLanternsRegistry.AMETHYST_GLINT_PARTICLE_TYPE, x, y, z, 0.f, 0.f, 0.f);
		}
	}
}
