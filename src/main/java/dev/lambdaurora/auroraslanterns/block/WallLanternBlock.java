/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.ExtensionType;
import dev.lambdaurora.auroraslanterns.accessor.BlockItemAccessor;
import dev.lambdaurora.auroraslanterns.block.behavior.AnimateTickBehavior;
import dev.lambdaurora.auroraslanterns.block.entity.SwayingBlockEntity;
import dev.lambdaurora.auroraslanterns.mixin.BlockAccessor;
import dev.lambdaurora.auroraslanterns.util.CustomStateBuilder;
import dev.lambdaurora.auroraslanterns.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Represents a wall lantern.
 *
 * @param <L> the type of the underlying lantern
 * @author LambdAurora
 * @version 1.2.0
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class WallLanternBlock<L extends LanternBlock> extends BlockWithEntity implements SimpleWaterloggedBlock {
	public static final MapCodec<? extends WallLanternBlock<?>> CODEC = makeCodec(LanternBlock.class, WallLanternBlock::new);

	static <L extends LanternBlock, W extends WallLanternBlock<? extends L>> MapCodec<W> makeCodec(
			@NotNull Class<L> lanternClass, @NotNull Factory<L, W> instantiator
	) {
		return RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								BuiltInRegistries.BLOCK.byNameCodec().fieldOf("lantern")
										.flatXmap(
												block -> lanternClass.isInstance(block)
														? DataResult.success(lanternClass.cast(block))
														: DataResult.error(() -> "Lantern must be of type LanternBlock"),
												DataResult::success
										)
										.forGetter(lantern -> lantern.lanternBlock),
								propertiesCodec()
						)
						.apply(instance, instantiator::create)
		);
	}

	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<ExtensionType> EXTENSION = EnumProperty.create("extension", ExtensionType.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final VoxelShape LANTERN_HANG_SHAPE = box(
			7.0, 11.0, 7.0,
			9.0, 13.0, 9.0
	);
	private static final double LANTERN_HANG_SHAPE_MIN_Y = LANTERN_HANG_SHAPE.min(Direction.Axis.Y);
	public static final Map<Direction, Map<ExtensionType, VoxelShape>> ATTACHMENT_SHAPES;
	private static final ThreadLocal<LanternBlock> ASSOCIATED_LANTERN_INIT = new ThreadLocal<>();

	private static final VoxelShape HOLDER_SHAPE = box(
			0.0, 8.0, 0.0,
			16.0, 16.0, 16.0
	);

	public static final Identifier BETTERGRASS_DATA = AurorasLanterns.id("bettergrass/data/wall_lantern");

	protected final L lanternBlock;
	private final AnimateTickBehavior<L> animateTickBehavior;

	public WallLanternBlock(L lantern, Properties properties) {
		super(setupContext(lantern, properties));

		this.lanternBlock = lantern;

		this.setDefaultState(Utils.remapBlockState(lantern.defaultState(), this.getStateDefinition().any())
				.with(FACING, Direction.NORTH)
				.with(EXTENSION, ExtensionType.NONE)
		);

		var item = Item.byBlock(lantern); // Avoid caching which could break stuff at this stage.
		if (item instanceof BlockItemAccessor blockItem) {
			blockItem.auroraslanterns$setWallBlock(this);
		}

		this.animateTickBehavior = AnimateTickBehavior.lanternBehavior(lanternBlock);
	}

	@Override
	protected @NotNull MapCodec<? extends WallLanternBlock<?>> codec() {
		return CODEC;
	}

	public @NotNull L getLanternBlock() {
		return this.lanternBlock;
	}

	public BlockState getLanternState(BlockState state) {
		return Utils.remapBlockState(state, this.getLanternBlock().defaultState());
	}

	@Override
	protected void createStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		var lantern = ASSOCIATED_LANTERN_INIT.get();
		ASSOCIATED_LANTERN_INIT.remove();

		var customBuilder = new CustomStateBuilder<>(builder);
		customBuilder.exclude("hanging", "facing");
		((BlockAccessor) lantern).auroraslanterns$createStateDefinition(customBuilder);

		builder.add(FACING);
		builder.add(EXTENSION);
	}

	/* Shapes */

	@Override
	public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		var facing = state.get(WallLanternBlock.FACING);
		var extension = state.get(WallLanternBlock.EXTENSION);
		var lanternShape = this.getLanternState(state).getShape(world, pos);
		var lanternShapeMaxY = lanternShape.max(Direction.Axis.Y);
		var lanternShapeMinY = lanternShape.min(Direction.Axis.Y);

		double yOffset = 2.0 / 16.0;
		if (lanternShapeMaxY < LANTERN_HANG_SHAPE_MIN_Y) {
			var size = lanternShapeMaxY - lanternShapeMinY;
			yOffset = LANTERN_HANG_SHAPE_MIN_Y - size;
		}
		return Shapes.or(
				lanternShape.move(
						(-facing.getStepX() * extension.getOffset()) / 16.0,
						yOffset,
						(-facing.getStepZ() * extension.getOffset()) / 16.0
				),
				WallLanternBlock.ATTACHMENT_SHAPES.get(facing).get(extension)
		);
	}

	/* Placement */

	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		var direction = state.get(FACING);
		var attachPos = pos.relative(direction.getOpposite());
		var attachState = world.getBlockState(attachPos);
		return !Shapes.joinIsNotEmpty(
				attachState.getBlockSupportShape(world, attachPos).getFaceShape(direction),
				HOLDER_SHAPE, BooleanOp.ONLY_SECOND
		) || ExtensionType.getExtensionValue(attachState, attachPos, world) != ExtensionType.NONE;
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		var state = this.defaultState();
		var world = context.getLevel();
		var pos = context.getClickedPos();
		var fluidState = world.getFluidState(pos);
		var directions = context.getNearestLookingDirections();

		state = state.with(WATERLOGGED, fluidState.getType() == Fluids.WATER);

		for (var direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				var opposite = direction.getOpposite();
				state = state.with(FACING, opposite);
				if (state.canSurvive(world, pos)) {
					BlockPos attachPos = pos.relative(direction);
					return state.with(
							EXTENSION,
							ExtensionType.getExtensionValue(world.getBlockState(attachPos), attachPos, world)
					);
				}
			}
		}

		return null;
	}

	@Override
	public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	/* Updates */

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
		this.lanternBlock.onPlace(state, world, pos, oldState, notify);
	}

	@Override
	public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
		this.lanternBlock.affectNeighborsAfterRemoval(state, world, pos, moved);
		super.affectNeighborsAfterRemoval(state, world, pos, moved);
	}

	@Override
	protected @NotNull BlockState updateShape(
			BlockState state, LevelReader world, ScheduledTickAccess tickScheduler, BlockPos pos,
			Direction direction, BlockPos posFrom, BlockState newState, RandomSource randomSource
	) {
		if (state.get(WATERLOGGED)) {
			tickScheduler.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}

		return direction.getOpposite() == state.get(FACING) && !state.canSurvive(world, pos)
				? Blocks.AIR.defaultState() : state;
	}

	/* Interaction */

	@Override
	public @NotNull ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		return this.getLanternState(state).getCloneItemStack(world, pos, includeData);
	}

	@Override
	protected void onProjectileHit(Level world, BlockState state, BlockHitResult hit, Projectile projectile) {
		var entity = projectile.getOwner();
		this.swing(world, state, hit, entity instanceof Player player ? player : null, true);
	}

	@Override
	protected @NotNull InteractionResult useWithoutItem(
			BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit
	) {
		return this.swing(world, state, hit, player, true)
				? InteractionResult.SUCCESS
				: InteractionResult.PASS;
	}

	public boolean swing(
			Level world, BlockState state, BlockHitResult hitResult, @Nullable Player player,
			boolean hitResultIndependent
	) {
		var direction = hitResult.getDirection();
		var blockPos = hitResult.getBlockPos();
		boolean canSwing = !hitResultIndependent
				|| this.isPointOnLantern(state, direction, hitResult.getBlockPos().getY() - (double) blockPos.getY());
		if (canSwing) {
			this.swing(player, world, blockPos, direction, false);

			return true;
		} else {
			return false;
		}
	}

	private boolean isPointOnLantern(BlockState state, Direction side, double y) {
		if (side.getAxis() != Direction.Axis.Y && y <= 0.8123999834060669D) {
			var direction = state.get(FACING);
			return direction.getAxis() != side.getAxis();
		} else {
			return false;
		}
	}

	public void swing(@Nullable Entity entity, Level world, BlockPos pos, Direction direction, boolean collision) {
		var blockEntity = AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.getBlockEntity(world, pos);
		if (!world.isClientSide() && blockEntity != null) {
			if (!blockEntity.isColliding()) {
				world.playSound(
						null, pos, AurorasLanternsRegistry.LANTERN_SWING_SOUND_EVENT, SoundSource.BLOCKS,
						2.f, 1.f
				);
				world.gameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
			}

			if (!collision)
				blockEntity.activate(direction);
			else
				blockEntity.activate(direction, entity);
		}
	}

	@Override
	protected void entityInside(
			BlockState state, Level world, BlockPos pos, Entity entity,
			InsideBlockEffectApplier insideBlockEffectApplier
	) {
		if (world.isClientSide())
			return;
		if (entity instanceof Projectile)
			return;

		var blockEntity = AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.getBlockEntity(world, pos);
		if (blockEntity == null)
			return;

		var swingAxis = state.get(FACING).getClockWise().getAxis();

		var lanternBox = blockEntity.getCollisionBox();
		var entityBox = entity.getBoundingBox();
		if (lanternBox.intersects(entityBox)) {
			var swingDirection = Direction.NORTH;
			if (swingAxis == Direction.Axis.X) {
				if ((pos.getX() + .5f) > entity.getX()) swingDirection = Direction.WEST;
				else swingDirection = Direction.EAST;
			} else if (swingAxis == Direction.Axis.Z) {
				if ((pos.getZ() + .5f) < entity.getZ()) swingDirection = Direction.SOUTH;
			}
			this.swing(entity, world, pos, swingDirection, true);

			if (entity instanceof ServerPlayer player) {
				AurorasLanternsRegistry.WALL_LANTERN_BONK_TRIGGER.trigger(player, state);
			}
		}
	}

	/* Block Entity Stuff */

	@Override
	protected @NotNull RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.create(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
			Level world, BlockState state, BlockEntityType<T> type
	) {
		return createTickerHelper(
				type, AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
				world.isClientSide() ? SwayingBlockEntity::clientTick : SwayingBlockEntity::serverTick
		);
	}

	/* Fluid */

	@Override
	protected @NotNull FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	/* Entity Stuff */

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	/* Redstone */

	@Override
	protected boolean isSignalSource(BlockState state) {
		return this.getLanternState(state).isSignalSource();
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		var lantern = AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE.getBlockEntity(world, pos);
		if (lantern != null) {
			if (lantern.isColliding()) {
				return Redstone.SIGNAL_MAX;
			} else if (lantern.isSwinging()) {
				int max = lantern.getMaxSwingTicks();
				float progress = (max - lantern.getSwingTicks()) / (float) max;
				return (int) (progress * 14);
			}
		}

		return Redstone.SIGNAL_NONE;
	}

	/* Visual */

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		this.animateTickBehavior.animateTick(this.getLanternBlock(), this.getLanternState(state), world, pos, random);
	}

	private static Properties setupContext(LanternBlock lanternBlock, Properties properties) {
		ASSOCIATED_LANTERN_INIT.set(lanternBlock);
		return properties;
	}

	public static Properties properties(LanternBlock lanternBlock) {
		return Properties.ofFullCopy(lanternBlock)
				.pushReaction(PushReaction.DESTROY)
				.overrideDescription(lanternBlock.getDescriptionId())
				.overrideLootTable(lanternBlock.getLootTable());
	}

	static {
		double attachmentMaxY = 16.0;
		double attachmentMinY = 10.0;

		Map<ExtensionType, VoxelShape> northShape;
		{
			var builder = ImmutableMap.<ExtensionType, VoxelShape>builder();
			var wallAttachment = box(
					6.0, attachmentMinY - 3.0, 15.0,
					10.0, attachmentMaxY, 16.0
			);
			builder.put(ExtensionType.NONE, Shapes.or(
					LANTERN_HANG_SHAPE,
					box(7.0, 13.0, 7.0, 9.0, 15.0, 15.0),
					box(6.0, attachmentMinY, 15.0, 10.0, attachmentMaxY, 16.0)
			));
			builder.put(ExtensionType.WALL, Shapes.or(
					LANTERN_HANG_SHAPE.move(0, 0, 0.125),
					box(7.0, 13.0, 7.0, 9.0, 15.0, 19.0),
					wallAttachment.move(0, 0, 0.25)
			));
			builder.put(ExtensionType.FENCE, Shapes.or(
					LANTERN_HANG_SHAPE.move(0, 0, 0.25),
					box(7.0, 13.0, 9.0, 9.0, 15.0, 21.0),
					wallAttachment.move(0, 0, 0.375)
			));

			northShape = builder.build();
		}

		Map<ExtensionType, VoxelShape> southShape;
		{
			var builder = ImmutableMap.<ExtensionType, VoxelShape>builder();
			var wallAttachment = box(
					6.0, attachmentMinY - 3.0, 0.0,
					10.0, attachmentMaxY, 1.0
			);
			builder.put(ExtensionType.NONE, Shapes.or(
					LANTERN_HANG_SHAPE,
					box(7.0, 13.0, 1.0, 9.0, 15.0, 9.0),
					box(6.0, attachmentMinY, 0.0, 10.0, attachmentMaxY, 1.0)
			));
			builder.put(ExtensionType.WALL, Shapes.or(
					LANTERN_HANG_SHAPE.move(0, 0, -.125),
					box(7.0, 13.0, -3.0, 9.0, 15.0, 9.0),
					wallAttachment.move(0, 0, -.25)
			));
			builder.put(ExtensionType.FENCE, Shapes.or(
					LANTERN_HANG_SHAPE.move(0, 0, -.25),
					box(7.0, 13.0, -5.0, 9.0, 15.0, 7.0),
					wallAttachment.move(0, 0, -.375)
			));

			southShape = builder.build();
		}

		Map<ExtensionType, VoxelShape> westShape;
		{
			var builder = ImmutableMap.<ExtensionType, VoxelShape>builder();
			var wallAttachment = box(
					15.0, attachmentMinY - 3.0, 6.0,
					16.0, attachmentMaxY, 10.0
			);
			builder.put(ExtensionType.NONE, Shapes.or(
					LANTERN_HANG_SHAPE,
					box(7.0, 13.0, 7.0, 15.0, 15.0, 9.0),
					box(15.0, attachmentMinY, 6.0, 16.0, attachmentMaxY, 10.0)
			));
			builder.put(ExtensionType.WALL, Shapes.or(
					LANTERN_HANG_SHAPE.move(0.125, 0, 0),
					box(7.0, 13.0, 7.0, 19.0, 15.0, 9.0),
					wallAttachment.move(0.25, 0, 0)
			));
			builder.put(ExtensionType.FENCE, Shapes.or(
					LANTERN_HANG_SHAPE.move(0.25, 0, 0),
					box(9.0, 13.0, 7.0, 21.0, 15.0, 9.0),
					wallAttachment.move(0.375, 0, 0)
			));

			westShape = builder.build();
		}

		Map<ExtensionType, VoxelShape> eastShape;
		{
			var builder = ImmutableMap.<ExtensionType, VoxelShape>builder();
			var wallAttachment = box(
					0.0, attachmentMinY - 3.0, 6.0,
					1.0, attachmentMaxY, 10.0
			);
			builder.put(ExtensionType.NONE, Shapes.or(
					LANTERN_HANG_SHAPE,
					box(1.0, 13.0, 7.0, 9.0, 15.0, 9.0),
					box(0.0, attachmentMinY, 6.0, 1.0, attachmentMaxY, 10.0)
			));
			builder.put(ExtensionType.WALL, Shapes.or(
					LANTERN_HANG_SHAPE.move(-.125, 0, 0),
					box(-3.0, 13.0, 7.0, 9.0, 15.0, 9.0),
					wallAttachment.move(-.25, 0, 0)
			));
			builder.put(ExtensionType.FENCE, Shapes.or(
					LANTERN_HANG_SHAPE.move(-.25, 0, 0),
					box(-5.0, 13.0, 7.0, 7.0, 15.0, 9.0),
					wallAttachment.move(-.375, 0, 0)
			));

			eastShape = builder.build();
		}

		ATTACHMENT_SHAPES = Maps.newEnumMap(
				ImmutableMap.of(
						Direction.NORTH, northShape,
						Direction.SOUTH, southShape,
						Direction.WEST, westShape,
						Direction.EAST, eastShape
				)
		);
	}

	public interface Factory<L extends LanternBlock, W extends WallLanternBlock<? extends L>> {
		W create(L lantern, Properties properties);
	}

	public interface Provider<L extends LanternBlock, W extends WallLanternBlock<? extends L>> {
		Factory<L, W> getWallLanternFactory();
	}
}
