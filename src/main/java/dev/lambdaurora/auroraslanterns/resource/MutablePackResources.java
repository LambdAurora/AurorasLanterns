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
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

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
	 * @see #putResource(PackType, Identifier, byte[])
	 * @see #putResource(String, Supplier)
	 */
	void putResource(String fileName, byte[] resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the resource content
	 * @see #putResource(String, byte[])
	 * @see #putResource(PackType, Identifier, Supplier)
	 */
	void putResource(PackType type, Identifier id, byte[] resource);

	/**
	 * Puts a resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(PackType, Identifier, Supplier)
	 * @see #putResource(String, byte[])
	 */
	void putResource(String fileName, Supplier<byte[]> resource);

	/**
	 * Puts a resource into the resource pack for the given side and path.
	 *
	 * @param type     the resource type
	 * @param id       the path of the resource
	 * @param resource the supplier of the resource content
	 * @apiNote the supplier is {@link com.google.common.base.Suppliers#memoize(com.google.common.base.Supplier) memoized}
	 * @see #putResource(String, Supplier)
	 * @see #putResource(PackType, Identifier, byte[])
	 */
	void putResource(PackType type, Identifier id, Supplier<byte[]> resource);

	/**
	 * Puts a text resource into the resource pack's root.
	 *
	 * @param fileName the name of the file
	 * @param text     the resource content
	 * @see #putResource(String, byte[])
	 */
	default void putText(String fileName, String text) {
		this.putResource(fileName, text.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Puts a text resource into the resource pack for the given side and path.
	 *
	 * @param type the resource type
	 * @param id   the path of the resource
	 * @param text the resource content
	 * @see #putResource(PackType, Identifier, byte[])
	 */
	default void putText(PackType type, Identifier id, String text) {
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
	default void putText(String fileName, Supplier<String> textSupplier) {
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
	default void putText(PackType type, Identifier id, Supplier<String> textSupplier) {
		this.putResource(type, id, () -> textSupplier.get().getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Clears the resource of a specific resource type.
	 *
	 * @param type the resource type
	 */
	void clearResources(PackType type);

	/**
	 * Clears all the resources from memory.
	 */
	void clearResources();
}