/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.platform.neoforge;

import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;

@Mod(value = AurorasLanterns.NAMESPACE)
public final class NeoForgeInitializer {
	public NeoForgeInitializer(ModContainer nativeContainer, IEventBus modBus) {
		modBus.addListener(ModifyRegistriesEvent.class, event -> {
			// Now we can stop doing manual block state cache init.
			NeoAurorasLanterns.doCacheInit = false;
		});
	}
}
