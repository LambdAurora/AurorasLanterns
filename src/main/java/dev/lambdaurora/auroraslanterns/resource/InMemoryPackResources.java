/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.resource;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.yumi.commons.TriState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Represents an in-memory resource pack.
 * <p>
 * The resources of this pack are stored in memory instead of it being on-disk.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class InMemoryPackResources implements MutablePackResources {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final boolean DUMP = TriState.fromProperty("auroraslanterns.pack.dump_from_in_memory")
			.toBooleanOrElse(FabricLoader.getInstance().isDevelopmentEnvironment());
	private final Map<Identifier, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
	private final Map<Identifier, Supplier<byte[]>> data = new ConcurrentHashMap<>();
	private final Map<String, Supplier<byte[]>> root = new ConcurrentHashMap<>();

	@Override
	public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
		String actualPath = String.join("/", path);

		return this.openResource(this.root, actualPath);
	}

	@Override
	public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
		return this.openResource(this.getResourceMap(type), id);
	}

	protected <T> @Nullable IoSupplier<InputStream> openResource(Map<T, Supplier<byte[]>> map, T key) {
		var supplier = map.get(key);

		if (supplier == null) {
			return null;
		}

		byte[] bytes = supplier.get();

		if (bytes == null) {
			return null;
		}

		return () -> new ByteArrayInputStream(bytes);
	}

	@Override
	public void listResources(PackType type, String namespace, String startingPath, ResourceOutput consumer) {
		this.getResourceMap(type).entrySet().stream()
				.filter(entry -> entry.getKey().getNamespace().equals(namespace) && entry.getKey().getPath().startsWith(startingPath))
				.forEach(entry -> {
					byte[] bytes = entry.getValue().get();

					if (bytes != null) {
						consumer.accept(entry.getKey(), () -> new ByteArrayInputStream(bytes));
					}
				});
	}

	@Override
	public @Unmodifiable Set<String> getNamespaces(PackType type) {
		return this.getResourceMap(type).keySet().stream()
				.map(Identifier::getNamespace)
				.collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> metaReader) throws IOException {
		if (!this.root.containsKey(PackResources.PACK_META)) {
			var json = new JsonObject();
			var packJson = new JsonObject();
			packJson.addProperty("description", "A virtual resource pack.");
			packJson.addProperty("pack_format", 5); // This is like, not read by any significant system when invisible to users.
			json.add("pack", packJson);

			if (!json.has(metaReader.getMetadataSectionName())) {
				return null;
			} else {
				try {
					return metaReader.fromJson(GsonHelper.getAsJsonObject(json, metaReader.getMetadataSectionName()));
				} catch (Exception e) {
					LOGGER.error("Couldn't load {} metadata from pack \"{}\":", metaReader.getMetadataSectionName(), this.packId(), e);
					return null;
				}
			}
		}

		var resource = this.getRootResource(PackResources.PACK_META);
		if (resource == null) return null;

		try (var stream = resource.get(); var reader = new InputStreamReader(stream)) {
			return metaReader.fromJson(JsonParser.parseReader(reader).getAsJsonObject());
		}
	}

	@Override
	public void close() {
		if (DUMP) {
			this.dumpAll();
		}
	}

	@Override
	public void putResource(String fileName, byte[] resource) {
		this.root.put(fileName, () -> resource);
	}

	@Override
	public void putResource(PackType type, Identifier id, byte[] resource) {
		this.getResourceMap(type).put(id, () -> resource);
	}

	@Override
	public void putResource(String fileName, Supplier<byte[]> resource) {
		this.root.put(fileName, Suppliers.memoize(resource::get));
	}

	@Override
	public void putResource(PackType type, Identifier id, Supplier<byte[]> resource) {
		this.getResourceMap(type).put(id, Suppliers.memoize(resource::get));
	}

	@Override
	public void clearResources(PackType type) {
		this.getResourceMap(type).clear();
	}

	@Override
	public void clearResources() {
		this.root.clear();
		this.clearResources(PackType.CLIENT_RESOURCES);
		this.clearResources(PackType.SERVER_DATA);
	}

	/**
	 * Dumps the content of this resource pack into the given path.
	 *
	 * @param path the path to dump the resources into
	 */
	public void dumpTo(Path path) {
		try {
			Files.createDirectories(path);

			this.root.forEach((p, resource) -> this.dumpResource(path, p, resource.get()));
			this.assets.forEach((p, resource) ->
					this.dumpResource(path, getResourcePath(PackType.CLIENT_RESOURCES, p), resource.get()));
			this.data.forEach((p, resource) ->
					this.dumpResource(path, getResourcePath(PackType.SERVER_DATA, p), resource.get()));
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {} to {}.", this.packId(), path, e);
		}
	}

	/**
	 * {@return the path inside a resource pack of the given resource path}
	 *
	 * @param type the type of the resource
	 * @param id   the identifier of the resource
	 */
	@Contract(value = "_, _ -> new", pure = true)
	static String getResourcePath(PackType type, Identifier id) {
		return type.getDirectory() + '/' + id.getNamespace() + '/' + id.getPath();
	}

	protected void dumpAll() {
		this.dumpTo(Path.of("debug", "packs", this.packId()));
	}

	protected void dumpResource(Path parentPath, String resourcePath, byte[] resource) {
		try {
			var p = parentPath.resolve(resourcePath);
			Files.createDirectories(p.getParent());
			Files.write(p, resource, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			LOGGER.error("Failed to write resource pack dump from pack {}.", this.packId(), e);
		}
	}

	private Map<Identifier, Supplier<byte[]>> getResourceMap(PackType type) {
		return switch (type) {
			case CLIENT_RESOURCES -> this.assets;
			case SERVER_DATA -> this.data;
		};
	}

	/**
	 * Represents an in-memory resource pack with a static name.
	 */
	public static class Named extends InMemoryPackResources {
		private final String name;

		public Named(String name) {
			this.name = name;
		}

		@Override
		public String packId() {
			return this.name;
		}
	}
}