/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.accessor;

import dev.yumi.commons.event.Event;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

// Little workaround due to QFAPI not remapping the registry events in a good way.
public interface RegistryEventStorage<T> {
	Event<Identifier, Callback<T>> auroraslanterns$getAddEvent();

	@SuppressWarnings("unchecked")
	static <T> RegistryEventStorage<T> of(Registry<T> registry) {
		if (!(registry instanceof MappedRegistry<T>)) {
			throw new IllegalArgumentException("Must be a mapped registry.");
		}

		return (RegistryEventStorage<T>) registry;
	}

	@FunctionalInterface
	interface Callback<T> {
		void onEntryAdded(Identifier resourceKey, T object);
	}
}
