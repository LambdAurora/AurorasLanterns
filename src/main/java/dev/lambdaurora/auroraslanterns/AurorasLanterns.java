/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import dev.lambdaurora.auroraslanterns.item.ItemTree;
import dev.lambdaurora.auroraslanterns.resource.AurorasLanternsRuntimeDatagen;
import dev.lambdaurora.auroraslanterns.resource.InMemoryPackResources;
import dev.yumi.commons.event.EventManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.stream.Collectors;

/**
 * Represents the Aurora's Lanterns mod.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public final class AurorasLanterns implements ModInitializer {
	public static final String NAMESPACE = "auroraslanterns";
	public static final EventManager<Identifier> EVENT_MANAGER = new EventManager<>(id("default"), Identifier::new);

	@Override
	public void onInitialize() {
		AurorasLanternsRegistry.init();

		AurorasLanternsRuntimeDatagen.DATA_DATAGEN.register(registrar -> {
			var pack = new InMemoryPackResources.Named(AurorasLanterns.id("generated").toString());
			registrar.accept(pack);

			pack.putText(PackType.SERVER_DATA, AurorasLanterns.id("tags/blocks/wall_lanterns.json"), """
					{
						"replace": false,
						"values": [
							%s
						]
					}
					""".formatted(LanternRegistry.streamIds().map(id -> '"' + id.toString() + '"').collect(Collectors.joining(",\n\t\t")))
			);
		});

		ItemTree.init();
	}

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}
}
