/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.entity;

import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock.Candle;
import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

/**
 * Represents a chandelier block entity.
 *
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public class ChandelierBlockEntity extends BlockEntity implements RenderDataBlockEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChandelierBlockEntity.class);
	private final NonNullList<ItemStack> candles = NonNullList.withSize(4, ItemStack.EMPTY);

	public ChandelierBlockEntity(BlockPos pos, BlockState blockState) {
		super(AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE, pos, blockState);
	}

	public int getCandlesCount() {
		return Math.toIntExact(this.candles.stream().filter(Predicate.not(ItemStack::isEmpty)).count());
	}

	public boolean placeCandle(ServerLevel level, @Nullable LivingEntity entity, ItemStack stack) {
		for (int i = 0; i < this.candles.size(); i++) {
			ItemStack existingStack = this.candles.get(i);
			if (existingStack.isEmpty() && stack.is(ItemTags.CANDLES)) {
				this.candles.set(i, stack.consumeAndReturn(1, entity));
				level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(entity, this.getBlockState()));
				this.markUpdated();
				return true;
			}
		}

		return false;
	}

	private void markUpdated() {
		this.setChanged();
		this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null) {
			Containers.dropContents(this.level, pos, this.candles);
		}
	}

	@Override
	public @Nullable Object getRenderData() {
		return this.candles
				.stream()
				.filter(Predicate.not(ItemStack::isEmpty))
				.map(stack -> Candle.BY_ITEM.getOrDefault(stack.getItem(), Candle.Normal.INSTANCE))
				.toArray(Candle[]::new);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.candles.clear();
		ContainerHelper.loadAllItems(input, this.candles);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		ContainerHelper.saveAllItems(output, this.candles, true);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, registries);
			this.saveAdditional(tagValueOutput);
			return tagValueOutput.buildResult();
		}
	}

	@Override
	protected void applyImplicitComponents(DataComponentGetter components) {
		super.applyImplicitComponents(components);
		if (this.candles.stream().allMatch(ItemStack::isEmpty)) {
			components.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.candles);
		}
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.candles));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void removeComponentsFromTag(ValueOutput output) {
		output.discard("Items");
	}
}
