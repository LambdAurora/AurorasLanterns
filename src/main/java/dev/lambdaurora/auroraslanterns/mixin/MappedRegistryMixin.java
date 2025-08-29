/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import com.mojang.serialization.Lifecycle;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.accessor.RegistryEventStorage;
import dev.yumi.commons.event.Event;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = MappedRegistry.class, priority = 1100)
public abstract class MappedRegistryMixin<T> implements Registry<T>, RegistryEventStorage<T> {
	@Shadow
	public abstract int getRawId(@Nullable T value);

	@Unique
	private final Event<Identifier, RegistryEventStorage.Callback<T>> auroraslanterns$addEvent
			= AurorasLanterns.EVENT_MANAGER.create(RegistryEventStorage.Callback.class);

	public MappedRegistryMixin() {}

	@Override
	public Event<Identifier, RegistryEventStorage.Callback<T>> auroraslanterns$getAddEvent() {
		return this.auroraslanterns$addEvent;
	}

	@Inject(method = "register", at = @At("RETURN"))
	public void auroraslanterns$onRegister(
			ResourceKey<T> resourceKey, T object, Lifecycle lifecycle, CallbackInfoReturnable<Holder.Reference<T>> cir
	) {
		RegistryEventStorage.of(this).auroraslanterns$getAddEvent().invoker()
				.onEntryAdded(resourceKey.value(), object);
	}
}
