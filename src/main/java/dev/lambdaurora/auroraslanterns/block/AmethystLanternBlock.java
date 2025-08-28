/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block;

import com.mojang.serialization.MapCodec;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an amethyst lantern block.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class AmethystLanternBlock extends LanternBlock {
	public static final MapCodec<AmethystLanternBlock> CODEC = simpleCodec(AmethystLanternBlock::new);

	public static final int EFFECT_RADIUS = 32;

	public AmethystLanternBlock(Properties properties) {
		super(properties);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public @NotNull MapCodec<LanternBlock> codec() {
		return (MapCodec) CODEC;
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
