/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.chandelier;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a ceiling chandelier block.
 *
 * @author LambdAurora
 * @version 1.5.0
 * @since 1.5.0
 */
public class CeilingChandelierBlock extends AbstractChandelierBlock implements EntityBlock {
	public static final MapCodec<CeilingChandelierBlock> CODEC = makeCodec(CeilingChandelierBlock::new);
	public static final VoxelShape ONE_CANDLE_SHAPE = box(5.0, 3.0, 5.0, 11.0, 13.0, 11.0);
	public static final VoxelShape TWO_CANDLE_SHAPE = box(2.0, 3.0, 6.0, 14.0, 12.0, 10.0);
	public static final VoxelShape THREE_CANDLE_SHAPE = box(2.0, 3.0, 2.0, 14.0, 12.0, 14.0);

	@SuppressWarnings("unchecked")
	private static final List<Vec3>[] CANDLES_TO_PARTICLE_OFFSETS = Util.make(() -> {
		double highestPoint = 0.875;
		double second = 0.8125;
		double third = 0.6875;

		return new List[]{
				List.of(new Vec3(0.5, 0.6875, 0.5)),
				List.of(new Vec3(0.25, highestPoint, 0.5), new Vec3(0.75, second, 0.5)),
				List.of(
						new Vec3(0.5, highestPoint, 0.25),
						new Vec3(0.25, second, 0.75),
						new Vec3(0.75, third, 0.75)
				),
				List.of(
						new Vec3(0.75, highestPoint, 0.25),
						new Vec3(0.75, second, 0.75),
						new Vec3(0.25, third, 0.75),
						new Vec3(0.25, second, 0.25)
				)
		};
	});

	private static final VoxelShape HOLDER_SHAPE = box(6.0, 0.0, 6.0, 10.0, 1.0, 10.0);

	public CeilingChandelierBlock(int holders, Properties properties) {
		super(holders, properties);
	}

	@Override
	public MapCodec<CeilingChandelierBlock> codec() {
		return CODEC;
	}

	@Override
	public AttachmentType attachmentType() {
		return AttachmentType.CEILING;
	}

	/* Shapes */

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return switch (this.holders()) {
			case 1 -> ONE_CANDLE_SHAPE;
			case 2 -> TWO_CANDLE_SHAPE;
			case 3, 4 -> THREE_CANDLE_SHAPE;
			default -> super.getShape(state, world, pos, context);
		};
	}

	/* Placement */

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
	}

	/* Updates */

	@Override
	public BlockState updateShape(
			BlockState state, LevelReader world, ScheduledTickAccess tickScheduler, BlockPos pos,
			Direction direction, BlockPos posFrom, BlockState newState, RandomSource randomSource
	) {
		state = super.updateShape(state, world, tickScheduler, pos, direction, posFrom, newState, randomSource);
		return direction == Direction.UP && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	/* Client */

	@Override
	protected Stream<Vec3> getParticleOffsets(BlockState state) {
		return CANDLES_TO_PARTICLE_OFFSETS[Math.clamp(this.holders() - 1, 0, 3)].stream();
	}
}
