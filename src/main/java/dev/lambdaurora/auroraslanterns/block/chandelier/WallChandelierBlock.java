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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a wall-mounted chandelier block.
 *
 * @author LambdAurora
 * @version 1.5.0
 * @since 1.5.0
 */
public class WallChandelierBlock extends AbstractChandelierBlock {
	public static final MapCodec<WallChandelierBlock> CODEC = makeCodec(WallChandelierBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

	private static final VoxelShape HOLDER_SHAPE = box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

	public WallChandelierBlock(int holders, Properties properties) {
		super(holders, properties);

		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
	}

	@Override
	public MapCodec<WallChandelierBlock> codec() {
		return CODEC;
	}

	@Override
	public AttachmentType attachmentType() {
		return AttachmentType.WALL;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	@Override
	public BlockState copyStates(BlockState current, BlockState target) {
		return super.copyStates(current, target)
				.setValue(FACING, current.getValue(FACING));
	}

	/* Shape */

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[Math.clamp(this.holders() - 1, 0, 3)].get(state.getValue(FACING));
	}

	/* Placement */

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		var direction = state.getValue(FACING);
		var blockPos = pos.relative(direction.getOpposite());
		var blockState = world.getBlockState(blockPos);
		return !Shapes.joinIsNotEmpty(
				blockState.getBlockSupportShape(world, blockPos).getFaceShape(direction),
				HOLDER_SHAPE, BooleanOp.ONLY_SECOND
		);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		var state = super.getStateForPlacement(ctx);
		if (state == null)
			return null;

		var world = ctx.getLevel();
		var pos = ctx.getClickedPos();
		var directions = ctx.getNearestLookingDirections();

		for (var direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				var opposite = direction.getOpposite();
				state = state.setValue(FACING, opposite);
				if (state.canSurvive(world, pos)) {
					return state;
				}
			}
		}

		return null;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/* Updates */

	@Override
	public BlockState updateShape(
			BlockState state, LevelReader world, ScheduledTickAccess tickScheduler, BlockPos pos,
			Direction direction, BlockPos posFrom, BlockState newState, RandomSource randomSource
	) {
		state = super.updateShape(state, world, tickScheduler, pos, direction, posFrom, newState, randomSource);
		return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(world, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	/* Client */

	@Override
	protected Stream<Vec3> getParticleOffsets(BlockState state) {
		return CANDLES_TO_PARTICLE_OFFSETS[Math.clamp(this.holders() - 1, 0, 3)].get(state.getValue(FACING)).stream();
	}

	@SuppressWarnings("unchecked")
	private static final Map<Direction, VoxelShape>[] SHAPES = Util.make(() -> {
		var shapes = new Map[4];

		double highest = 12.0;
		shapes[0] = new EnumMap<>(Map.of(
				Direction.NORTH, box(6.0, 2.0, 10.0, 10.0, highest, 16.0),
				Direction.EAST, box(0.0, 2.0, 6.0, 6.0, highest, 10.0),
				Direction.SOUTH, box(6.0, 2.0, 0.0, 10.0, highest, 6.0),
				Direction.WEST, box(10.0, 2.0, 6.0, 16.0, highest, 10.0)
		));

		shapes[1] = new EnumMap<>(Map.of(
				Direction.NORTH, box(2.0, 2.0, 10.0, 14.0, highest, 16.0),
				Direction.EAST, box(0.0, 2.0, 2.0, 6.0, highest, 14.0),
				Direction.SOUTH, box(2.0, 2.0, 0.0, 14.0, highest, 6.0),
				Direction.WEST, box(10.0, 2.0, 2.0, 16.0, highest, 14.0)
		));

		shapes[2] = new EnumMap<>(Map.of(
				Direction.NORTH, box(1.0, 2.0, 8.0, 15.0, highest, 16.0),
				Direction.EAST, box(0.0, 2.0, 1.0, 8.0, highest, 15.0),
				Direction.SOUTH, box(1.0, 2.0, 0.0, 15.0, highest, 8.0),
				Direction.WEST, box(8.0, 2.0, 1.0, 16.0, highest, 15.0)
		));

		highest = 14.0;
		shapes[3] = new EnumMap<>(Map.of(
				Direction.NORTH, box(1.0, 2.0, 6.0, 15.0, highest, 16.0),
				Direction.EAST, box(0.0, 2.0, 1.0, 10.0, highest, 15.0),
				Direction.SOUTH, box(1.0, 2.0, 0.0, 15.0, highest, 10.0),
				Direction.WEST, box(6.0, 2.0, 1.0, 16.0, highest, 15.0)
		));

		return shapes;
	});
	@SuppressWarnings("unchecked")
	private static final Map<Direction, List<Vec3>>[] CANDLES_TO_PARTICLE_OFFSETS = Util.make(() -> {
		var offsets = new Map[4];

		double highestPoint = 0.875;
		double second = 0.8125;
		double third = 0.75;
		offsets[0] = new EnumMap<>(Map.of(
				Direction.NORTH, List.of(new Vec3(0.5, highestPoint, 0.75)),
				Direction.EAST, List.of(new Vec3(0.25, highestPoint, 0.5)),
				Direction.SOUTH, List.of(new Vec3(0.5, highestPoint, 0.25)),
				Direction.WEST, List.of(new Vec3(0.75, highestPoint, 0.5))
		));

		offsets[1] = new EnumMap<>(Map.of(
				Direction.NORTH, List.of(new Vec3(0.75, highestPoint, 0.75), new Vec3(0.25, second, 0.75)),
				Direction.EAST, List.of(new Vec3(0.25, highestPoint, 0.75), new Vec3(0.25, second, 0.25)),
				Direction.SOUTH, List.of(new Vec3(0.25, highestPoint, 0.25), new Vec3(0.75, second, 0.25)),
				Direction.WEST, List.of(new Vec3(0.75, highestPoint, 0.25), new Vec3(0.75, second, 0.75))
		));

		offsets[2] = new EnumMap<>(Map.of(
				Direction.NORTH, List.of(
						new Vec3(0.8125, highestPoint, 0.75),
						new Vec3(0.1875, second, 0.75),
						new Vec3(0.5, third, 0.625)
				),
				Direction.EAST, List.of(
						new Vec3(0.25, highestPoint, 0.8125),
						new Vec3(0.25, second, 0.1875),
						new Vec3(0.375, third, 0.5)
				),
				Direction.SOUTH, List.of(
						new Vec3(0.1875, highestPoint, 0.25),
						new Vec3(0.8125, second, 0.25),
						new Vec3(0.5, third, 0.375)
				),
				Direction.WEST, List.of(
						new Vec3(0.75, highestPoint, 0.1875),
						new Vec3(0.75, second, 0.8125),
						new Vec3(0.625, third, 0.5)
				)
		));

		highestPoint = 1.0;
		double four = second;
		second = 0.9375;
		third = 0.6875;
		offsets[3] = new EnumMap<>(Map.of(
				Direction.NORTH, List.of(
						new Vec3(0.8125, highestPoint, 0.75),
						new Vec3(0.1875, second, 0.75),
						new Vec3(0.3125, third, 0.5),
						new Vec3(0.6875, four, 0.5)
				),
				Direction.EAST, List.of(
						new Vec3(0.25, highestPoint, 0.8125),
						new Vec3(0.25, second, 0.1875),
						new Vec3(0.5, third, 0.3125),
						new Vec3(0.5, four, 0.6875)
				),
				Direction.SOUTH, List.of(
						new Vec3(0.1875, highestPoint, 0.25),
						new Vec3(0.8125, second, 0.25),
						new Vec3(0.6875, third, 0.5),
						new Vec3(0.3125, four, 0.5)
				),
				Direction.WEST, List.of(
						new Vec3(0.75, highestPoint, 0.1875),
						new Vec3(0.75, second, 0.8125),
						new Vec3(0.5, third, 0.6875),
						new Vec3(0.5, four, 0.3125)
				)
		));

		return offsets;
	});
}
