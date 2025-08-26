/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.util;

import dev.lambdaurora.auroraslanterns.mixin.StateDefinitionBuilderAccessor;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CustomStateBuilder<O, S extends StateHolder<O, S>> extends StateDefinition.Builder<O, S> {
	private final StateDefinition.Builder<O, S> parent;
	private final List<String> excludes = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public CustomStateBuilder(StateDefinition.Builder<O, S> parent) {
		super(((StateDefinitionBuilderAccessor<O, S>) parent).getOwner());
		this.parent = parent;
	}

	public CustomStateBuilder<O, S> exclude(String... excludes) {
		Collections.addAll(this.excludes, excludes);
		return this;
	}

	@Override
	public CustomStateBuilder<O, S> add(Property<?>... properties) {
		for (var property : properties) {
			if (this.excludes.contains(property.getName()))
				continue;
			this.parent.add(property);
		}
		return this;
	}

	@SuppressWarnings("unchecked")
	public CustomStateBuilder<O, S> safeAdd(Property<?>... properties) {
		for (var property : properties) {
			if (this.excludes.contains(property.getName())
					|| ((StateDefinitionBuilderAccessor<O, S>) this.parent).getProperties().containsKey(property.getName()))
				continue;
			this.parent.add(property);
		}
		return this;
	}

	@Override
	public @NotNull StateDefinition<O, S> create(Function<O, S> ownerToStateFunction, StateDefinition.Factory<O, S> factory) {
		return this.parent.create(ownerToStateFunction, factory);
	}
}
