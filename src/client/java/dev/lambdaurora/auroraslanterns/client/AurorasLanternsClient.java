/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client;

import com.mojang.logging.LogUtils;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.LanternRegistry;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.lambdaurora.auroraslanterns.client.particle.AmethystGlintParticle;
import dev.lambdaurora.auroraslanterns.client.renderer.WallLanternBlockEntityRenderer;
import dev.lambdaurora.auroraslanterns.resource.AurorasLanternsRuntimeDatagen;
import dev.lambdaurora.auroraslanterns.resource.InMemoryPackResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.io.ResourceType;
import org.slf4j.Logger;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public final class AurorasLanternsClient implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(
				AurorasLanternsRegistry.AMETHYST_GLINT_PARTICLE_TYPE, AmethystGlintParticle.Provider::new
		);

		BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(),
				AurorasLanternsRegistry.AMETHYST_LANTERN_BLOCK,
				AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK
		);

		LanternRegistry.forEachAndFuture(wallLanternBlock -> {
			BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), wallLanternBlock);
		});

		BlockEntityRenderers.register(
				AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
				WallLanternBlockEntityRenderer::new
		);

		AurorasLanternsRuntimeDatagen.CLIENT_DATAGEN.register(registrar -> {
			var pack = new InMemoryPackResources.Named(AurorasLanterns.id("generated").toString());
			registrar.accept(pack);

			try (var stream = AurorasLanternsClient.class.getResourceAsStream(
					"/assets/%s/blockstates/wall_lantern.json".formatted(AurorasLanterns.NAMESPACE)
			)) {
				if (stream != null) {
					var blockStateTemplate = stream.readAllBytes();

					LanternRegistry.forEach((id, block) -> {
						pack.putResource(ResourceType.CLIENT_RESOURCES, id.withPath(path -> "blockstates/" + path + ".json"), blockStateTemplate);
						pack.putText(
								ResourceType.CLIENT_RESOURCES, id.withPath(path -> "bettergrass/states/" + path + ".json"),
								"{\"type\":\"layer\",\"data\":\"" + WallLanternBlock.BETTERGRASS_DATA + "\"}"
						);
					});
				} else {
					LOGGER.error("Failed to load wall lantern blockstate JSON template: could not find file.");
				}
			} catch (IOException e) {
				LOGGER.error("Failed to load wall lantern blockstate JSON template:", e);
			}
		});
	}
}
