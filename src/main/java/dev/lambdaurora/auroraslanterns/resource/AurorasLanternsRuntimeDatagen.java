/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.resource;

import com.mojang.logging.LogUtils;
import dev.yumi.commons.collections.YumiCollections;
import dev.yumi.commons.event.Event;
import dev.yumi.mc.core.api.YumiEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class AurorasLanternsRuntimeDatagen {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Event<Identifier, DataGenerator> CLIENT_DATAGEN
			= YumiEvents.EVENTS.create(DataGenerator.class);
	public static final Event<Identifier, DataGenerator> DATA_DATAGEN
			= YumiEvents.EVENTS.create(DataGenerator.class);

	private AurorasLanternsRuntimeDatagen() {
		throw new UnsupportedOperationException("AurorasLanternsRuntimeDatagen only contains static definitions.");
	}

	public static List<PackResources> inject(PackType type, List<PackResources> resources) {
		var list = new ArrayList<PackResources>();
		(type == PackType.CLIENT_RESOURCES ? CLIENT_DATAGEN : DATA_DATAGEN).invoker().inject(list::add);
		return YumiCollections.concat(list, resources);
	}

	@FunctionalInterface
	public interface DataGenerator {
		void inject(Consumer<PackResources> registrar);
	}
}
