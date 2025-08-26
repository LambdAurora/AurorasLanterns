/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.io.ResourceType;
import net.minecraft.server.packs.PackResources;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Represents a resource pack whose resources are mutable.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MutablePackResources extends PackResources {
	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the resource content
	 * @see #putResource(ResourceType, Identifier, byte[])
	 * @see #putResource(String, Supplier)
	 */
	void putResource(@NotNull String fileName, byte @NotNull [] resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the resource content
	 * @see #putResource(String, byte[])
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 */
	void putResource(@NotNull ResourceType type, @NotNull Identifier id, byte @NotNull [] resource);

	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(ResourceType, Identifier, Supplier)
	 * @see #putResource(String, byte[])
	 */
	void putResource(@NotNull String fileName, @NotNull Supplier<byte @NotNull []> resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(String, Supplier)
	 * @see #putResource(ResourceType, Identifier, byte[])
	 */
	void putResource(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<byte @NotNull []> resource);

	/**
	 * Puts a text resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param text     the resource content
	 * @see #putResource(String, byte[])
	 */
	default void putText(@NotNull String fileName, @NotNull String text) {
		this.putResource(fileName, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack for the given side and path.
	 *
	 * @param type the resource type
	 * @param id   the path of the resource
	 * @param text the resource content
	 * @see #putResource(ResourceType, Identifier, byte[])
	 */
	default void putText(@NotNull ResourceType type, @NotNull Identifier id, @NotNull String text) {
		this.putResource(type, id, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack's root.
	 *
	 * @param fileName     the name of the file
	 * @param textSupplier the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(String, Supplier)
	 */
	default void putText(@NotNull String fileName, @NotNull Supplier<@NotNull String> textSupplier) {
		this.putResource(fileName, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack for the given side and path.
	 *
	 * @param type         the resource type
	 * @param id           the path of the resource
	 * @param textSupplier the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 */
	default void putText(@NotNull ResourceType type, @NotNull Identifier id, @NotNull Supplier<@NotNull String> textSupplier) {
		this.putResource(type, id, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Clears the resource of a specific resource type.
	 *
	 * @param type the resource type
	 */
	void clearResources(ResourceType type);

	/**
	 * Clears all the resources from memory.
	 */
	void clearResources();
}