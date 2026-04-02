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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a chandelier block.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public abstract class AbstractChandelierBlock extends Block
		implements EntityBlock, SimpleWaterloggedBlock {
	public static final IntegerProperty HOLDERS = IntegerProperty.create("holders", 1, 4);
	public static final IntegerProperty LIT_CANDLES = IntegerProperty.create("lit_candles", 0, 1);
	public static final IntegerProperty DUO_LIT_CANDLES = IntegerProperty.create("lit_candles", 0, 2);
	public static final IntegerProperty TRIO_LIT_CANDLES = IntegerProperty.create("lit_candles", 0, 3);
	public static final IntegerProperty QUAD_LIT_CANDLES = IntegerProperty.create("lit_candles", 0, 4);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	static <C extends AbstractChandelierBlock> MapCodec<C> makeCodec(Factory<C> instantiator) {
		return RecordCodecBuilder.mapCodec(
				instance -> instance.group(
								ExtraCodecs.intRange(1, 4).fieldOf("holders").forGetter(AbstractChandelierBlock::holders),
								propertiesCodec()
						)
						.apply(instance, instantiator::create)
		);
	}

	private static final ThreadLocal<@Nullable Integer> HOLDERS_INIT = new ThreadLocal<>();

	private final int holders;

	protected AbstractChandelierBlock(@Range(from = 1, to = 4) int holders, Properties properties) {
		super(setupContext(holders, properties));
		this.holders = holders;

		this.registerDefaultState(this.stateDefinition.any()
				.setValue(getLitCandlesProperty(holders), 0)
				.setValue(WATERLOGGED, false)
		);
	}

	/**
	 * {@return the amount of candle holders on this chandelier}
	 */
	public int holders() {
		return this.holders;
	}

	/**
	 * {@return the attachment type of this chandelier}
	 */
	public abstract AttachmentType attachmentType();

	public static IntegerProperty getLitCandlesProperty(@Range(from = 1, to = 4) int holders) {
		return switch (holders) {
			case 1 -> LIT_CANDLES;
			case 2 -> DUO_LIT_CANDLES;
			case 3 -> TRIO_LIT_CANDLES;
			case 4 -> QUAD_LIT_CANDLES;
			default -> throw new IllegalArgumentException(
					"There is only between 1 and 4 candle holders, provided: " + holders + "."
			);
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);

		var holders = HOLDERS_INIT.get();
		HOLDERS_INIT.remove();
		assert holders != null;

		builder.add(getLitCandlesProperty(holders), WATERLOGGED);
	}

	public BlockState copyStates(BlockState current, BlockState target) {
		return target
				.setValue(
						getLitCandlesProperty(((AbstractChandelierBlock) target.getBlock()).holders),
						current.getValue(getLitCandlesProperty(((AbstractChandelierBlock) current.getBlock()).holders))
				)
				.setValue(WATERLOGGED, current.getValue(WATERLOGGED));
	}

	/**
	 * {@return the amount of lit candles}
	 *
	 * @param state the state to gather the lit candles from
	 */
	public int getLit(BlockState state) {
		return state.getValue(getLitCandlesProperty(this.holders));
	}

	/**
	 * {@return {@code true} if the given chandelier can be lit, or {@code false} otherwise}
	 *
	 * @param state the chandelier block state
	 */
	public boolean canBeLit(BlockGetter level, BlockPos pos, BlockState state) {
		if (this.getLit(state) > 0 || state.getValue(WATERLOGGED)) return false;

		var chandelier = AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE.getBlockEntity(level, pos);
		return chandelier != null && chandelier.getCandlesCount() > 0;
	}

	@Override
	protected BlockState updateShape(
			BlockState state,
			LevelReader level,
			ScheduledTickAccess scheduledTickAccess,
			BlockPos pos,
			Direction direction,
			BlockPos neighborPos,
			BlockState neighborState,
			RandomSource random
	) {
		if (state.getValue(WATERLOGGED)) {
			scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
	}

	/* Placement */

	@Override
	protected boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		return !useContext.isSecondaryUseActive() && useContext.getItemInHand().getItem() == this.asItem() && this.holders < 4
				|| super.canBeReplaced(state, useContext);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
		boolean isWater = fluidState.getType() == Fluids.WATER;
		var state = super.getStateForPlacement(context);
		return state == null ? null : state.setValue(WATERLOGGED, isWater);
	}

	/* Behavior */

	private void setLit(LevelAccessor level, BlockState state, BlockPos pos, boolean lit) {
		if (!lit) {
			level.setBlock(pos, state.setValue(getLitCandlesProperty(this.holders), 0), Block.UPDATE_ALL_IMMEDIATE);
		} else {
			var chandelier = AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE.getBlockEntity(level, pos);
			if (chandelier != null) {
				level.setBlock(
						pos,
						state.setValue(getLitCandlesProperty(this.holders), chandelier.getCandlesCount()),
						Block.UPDATE_ALL_IMMEDIATE
				);
			}
		}
	}

	public void extinguish(@Nullable Player player, BlockState state, LevelAccessor level, BlockPos pos) {
		this.setLit(level, state, pos, false);
		if (state.getBlock() instanceof AbstractChandelierBlock chandelier) {
			chandelier.getParticleOffsets(state)
					.limit(chandelier.getLit(state))
					.forEach(vec3 -> level.addParticle(
							ParticleTypes.SMOKE,
							pos.getX() + vec3.x(), pos.getY() + vec3.y(), pos.getZ() + vec3.z(),
							0.0, 0.1F, 0.0
					));
		}

		level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.f, 1.f);
		level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	@Override
	protected InteractionResult useItemOn(
			ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult
	) {
		if (player.getAbilities().mayBuild) {
			var chandelier = AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE.getBlockEntity(level, pos);
			if (chandelier == null) {
				return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
			}

			boolean isLit = this.getLit(state) > 0;
			if (stack.isEmpty() && isLit) {
				this.extinguish(player, state, level, pos);
				return InteractionResult.SUCCESS;
			} else if (stack.is(ConventionalItemTags.IGNITER_TOOLS)
					&& !isLit
					&& chandelier.getCandlesCount() > 0
					&& this.canBeLit(level, pos, state)
			) {
				level.playSound(
						player, pos,
						SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
						1.f, level.getRandom().nextFloat() * .4f + .8f
				);
				this.setLit(level, state, pos, true);
				level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
				stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
				return InteractionResult.SUCCESS;
			} else if (stack.is(ItemTags.CANDLES) && chandelier.getCandlesCount() < this.holders) {
				level.playSound(
						player, pos,
						SoundEvents.CANDLE_PLACE, SoundSource.BLOCKS,
						1.f, level.getRandom().nextFloat() * .4f + .8f
				);

				if (level instanceof ServerLevel serverLevel && chandelier.placeCandle(serverLevel, player, stack)) {
					if (this.getLit(state) > 0) {
						this.setLit(level, state, pos, true);
					}

					return InteractionResult.SUCCESS_SERVER;
				}

				return InteractionResult.SUCCESS;
			}
		}

		return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		ItemStack stack = super.getCloneItemStack(level, pos, state, includeData);

		if (includeData) {
			var litCandlesProperty = getLitCandlesProperty(this.holders);

			stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY
					.with(HOLDERS, this.holders)
					.with(litCandlesProperty, state.getValue(litCandlesProperty))
			);
		}

		return stack;
	}

	@Override
	protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
		if (!level.isClientSide() && projectile.isOnFire() && this.canBeLit(level, hit.getBlockPos(), state)) {
			this.setLit(level, state, hit.getBlockPos(), true);
		}
	}

	@Override
	protected void onExplosionHit(
			BlockState state, ServerLevel level, BlockPos pos, Explosion explosion,
			BiConsumer<ItemStack, BlockPos> dropConsumer
	) {
		if (explosion.canTriggerBlocks() && this.getLit(state) > 0) {
			this.extinguish(null, state, level, pos);
		}

		super.onExplosionHit(state, level, pos, explosion, dropConsumer);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			BlockState newState = state.setValue(WATERLOGGED, true);
			if (this.getLit(state) > 0) {
				this.extinguish(null, newState, level, pos);
			} else {
				level.setBlock(pos, newState, 3);
			}

			level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
			return true;
		} else {
			return false;
		}
	}

	/* Fluid */

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	/* Block entity */

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE.create(pos, state);
	}

	@Override
	public boolean shouldChangedStateKeepBlockEntity(BlockState state) {
		return state.getBlock() instanceof AbstractChandelierBlock chandelier && chandelier.holders <= this.holders;
	}

	/* Client */

	protected abstract Stream<Vec3> getParticleOffsets(BlockState state);

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int lit = this.getLit(state);

		if (lit > 0) {
			this.getParticleOffsets(state)
					.limit(lit)
					.forEach(offset ->
							AbstractCandleBlock.addParticlesAndSound(level, offset.add(pos.getX(), pos.getY(), pos.getZ()), random)
					);
		}
	}

	public static BlockBehaviour.Properties properties(int holders, SoundType soundType, MapColor mapColor) {
		return BlockBehaviour.Properties.of()
				.strength(.5f, .8f)
				.mapColor(mapColor)
				.sound(soundType)
				.noOcclusion()
				.pushReaction(PushReaction.DESTROY)
				.lightLevel(state -> Math.clamp(3L * state.getValue(getLitCandlesProperty(holders)) + 2, 0, 15));
	}

	private static Properties setupContext(int holders, Properties properties) {
		HOLDERS_INIT.set(holders);
		return properties;
	}

	public interface Factory<C extends AbstractChandelierBlock> {
		C create(int holders, Properties properties);
	}

	public enum AttachmentType {
		CEILING("ceiling"),
		WALL("wall"),
		STANDING("standing");

		private final String id;

		AttachmentType(String id) {
			this.id = id;
		}

		public String id() {
			return this.id;
		}
	}

	public sealed interface Candle {
		Map<String, Candle> BY_NAME = Util.make(() -> {
			var map = new HashMap<String, Candle>();
			map.put(Candle.Normal.INSTANCE.name(), Candle.Normal.INSTANCE);
			Candle.Colored.BY_COLOR.values().forEach(color -> map.put(color.name(), color));
			return Map.copyOf(map);
		});
		Map<Item, Candle> BY_ITEM = Util.make(() -> Stream.concat(
				Stream.of(Candle.Normal.INSTANCE),
				Candle.Colored.BY_COLOR.values().stream()
		).collect(Collectors.toMap(Candle::item, Function.identity())));

		String name();

		String vanillaPrefix();

		Item item();

		final class Normal implements Candle {
			public static final Normal INSTANCE = new Normal();

			@Override
			public String name() {
				return "normal";
			}

			@Override
			public String vanillaPrefix() {
				return "";
			}

			@Override
			public Item item() {
				return Items.CANDLE;
			}
		}

		record Colored(DyeColor color, Item item) implements Candle {
			public static Map<DyeColor, Colored> BY_COLOR = Util.make(() -> Map.ofEntries(
					Map.entry(DyeColor.WHITE, new Colored(DyeColor.WHITE, Items.WHITE_CANDLE)),
					Map.entry(DyeColor.ORANGE, new Colored(DyeColor.ORANGE, Items.ORANGE_CANDLE)),
					Map.entry(DyeColor.MAGENTA, new Colored(DyeColor.MAGENTA, Items.MAGENTA_CANDLE)),
					Map.entry(DyeColor.LIGHT_BLUE, new Colored(DyeColor.LIGHT_BLUE, Items.LIGHT_BLUE_CANDLE)),
					Map.entry(DyeColor.YELLOW, new Colored(DyeColor.YELLOW, Items.YELLOW_CANDLE)),
					Map.entry(DyeColor.LIME, new Colored(DyeColor.LIME, Items.LIME_CANDLE)),
					Map.entry(DyeColor.PINK, new Colored(DyeColor.PINK, Items.PINK_CANDLE)),
					Map.entry(DyeColor.GRAY, new Colored(DyeColor.GRAY, Items.GRAY_CANDLE)),
					Map.entry(DyeColor.LIGHT_GRAY, new Colored(DyeColor.LIGHT_GRAY, Items.LIGHT_GRAY_CANDLE)),
					Map.entry(DyeColor.CYAN, new Colored(DyeColor.CYAN, Items.CYAN_CANDLE)),
					Map.entry(DyeColor.PURPLE, new Colored(DyeColor.PURPLE, Items.PURPLE_CANDLE)),
					Map.entry(DyeColor.BLUE, new Colored(DyeColor.BLUE, Items.BLUE_CANDLE)),
					Map.entry(DyeColor.BROWN, new Colored(DyeColor.BROWN, Items.BROWN_CANDLE)),
					Map.entry(DyeColor.GREEN, new Colored(DyeColor.GREEN, Items.GREEN_CANDLE)),
					Map.entry(DyeColor.RED, new Colored(DyeColor.RED, Items.RED_CANDLE)),
					Map.entry(DyeColor.BLACK, new Colored(DyeColor.BLACK, Items.BLACK_CANDLE)))
			);

			@Override
			public String name() {
				return this.color.getName();
			}

			@Override
			public String vanillaPrefix() {
				return this.name() + "_";
			}
		}
	}
}
