/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.model;

import dev.lambdaurora.auroraslanterns.block.entity.ChandelierBlockEntity;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Consumer;

public record UnbakedChandelierModel(BlockStateModel.UnbakedRoot wrapped) implements BlockStateModel.UnbakedRoot {
	private static final List<Identifier> CEILING_DEPENDENCIES = ChandelierBlockEntity.Candle.BY_NAME.values().stream()
			.<Identifier>mapMulti((candle, consumer) -> {
				acceptDependency("ceiling", candle, consumer);
			})
			.toList();

	@Override
	public Object visualEqualityGroup(BlockState state) {
		return this.wrapped.visualEqualityGroup(state);
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		this.wrapped.resolveDependencies(resolver);
		// TODO: do not hardcode type
		CEILING_DEPENDENCIES.forEach(resolver::markDependency);
	}

	@Override
	public BlockStateModel bake(BlockState state, ModelBaker baker) {
		return new BakedChandelierModel(
				this.wrapped.bake(state, baker),
				ChandelierModelData.resolve("ceiling", baker)
		);
	}

	private static void acceptDependency(String attachmentType, ChandelierBlockEntity.Candle candle, Consumer<Identifier> consumer) {
		String name = candle.vanillaPrefix() + "candle";
		acceptDependency(attachmentType, name + "_1", consumer);
		acceptDependency(attachmentType, name + "_2_1", consumer);
		acceptDependency(attachmentType, name + "_2_2", consumer);
		acceptDependency(attachmentType, name + "_3_1", consumer);
		acceptDependency(attachmentType, name + "_3_2", consumer);
		acceptDependency(attachmentType, name + "_3_3", consumer);
		acceptDependency(attachmentType, name + "_4_1", consumer);
		acceptDependency(attachmentType, name + "_4_2", consumer);
		acceptDependency(attachmentType, name + "_4_3", consumer);
		acceptDependency(attachmentType, name + "_4_4", consumer);
	}

	private static void acceptDependency(String attachmentType, String name, Consumer<Identifier> consumer) {
		consumer.accept(ChandelierModelData.modelId(attachmentType + "/" + name));
		consumer.accept(ChandelierModelData.modelId(attachmentType + "/" + name + "_lit"));
	}
}
