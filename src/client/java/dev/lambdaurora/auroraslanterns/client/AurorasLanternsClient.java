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
import dev.lambdaurora.auroraslanterns.ChandelierBlocks;
import dev.lambdaurora.auroraslanterns.LanternRegistry;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock;
import dev.lambdaurora.auroraslanterns.client.model.UnbakedChandelierModel;
import dev.lambdaurora.auroraslanterns.client.model.ChandelierModelData;
import dev.lambdaurora.auroraslanterns.client.particle.AmethystGlintParticle;
import dev.lambdaurora.auroraslanterns.client.renderer.WallLanternBlockEntityRenderer;
import dev.lambdaurora.auroraslanterns.resource.AurorasLanternsRuntimeDatagen;
import dev.lambdaurora.auroraslanterns.resource.InMemoryPackResources;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.client.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Environment(EnvType.CLIENT)
public final class AurorasLanternsClient implements ClientModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitializeClient(ModContainer mod) {
		ParticleFactoryRegistry.getInstance().register(
				AurorasLanternsRegistry.AMETHYST_GLINT_PARTICLE_TYPE, AmethystGlintParticle.Provider::new
		);

		BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT,
				AurorasLanternsRegistry.AMETHYST_LANTERN_BLOCK,
				AurorasLanternsRegistry.REDSTONE_LANTERN_BLOCK
		);

		ChandelierBlocks.streamAll().map(ChandelierBlocks::ceiling)
				.forEach(blocks ->
						blocks.forEach(block ->
								BlockRenderLayerMap.putBlock(block, ChunkSectionLayer.CUTOUT)
						)
				);

		LanternRegistry.forEachAndFuture((id, wallLanternBlock) -> {
			BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, wallLanternBlock);
		});

		BlockEntityRenderers.register(
				AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE,
				WallLanternBlockEntityRenderer::new
		);

		PreparableModelLoadingPlugin.register(
				(sharedState, prepareExecutor) ->
						CompletableFuture.supplyAsync(() -> ChandelierModelData.resolve(sharedState), prepareExecutor),
				(data, pluginCtx) -> {
					pluginCtx.modifyBlockModelOnLoad().register((model, context) -> {
						if (context.state().getBlock() instanceof AbstractChandelierBlock) {
							return new UnbakedChandelierModel(data.models().get(context.state()), model);
						}

						return model;
					});
				}
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
						pack.putResource(PackType.CLIENT_RESOURCES, id.withPath(path -> "blockstates/" + path + ".json"), blockStateTemplate);
						pack.putText(
								PackType.CLIENT_RESOURCES, id.withPath(path -> "bettergrass/states/" + path + ".json"),
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
