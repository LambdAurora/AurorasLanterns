/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.resource.datagen;

import dev.lambdaurora.auroraslanterns.client.model.ChandelierModelData;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;

record ChandelierCandleModels(MultiVariant unlit, MultiVariant lit) {
	static ChandelierCandleModels create(
			ModelTemplate template, String name, TextureMapping normalTextures, TextureMapping litTextures,
			BlockModelGenerators generators
	) {
		MultiVariant unlitVariant = BlockModelGenerators.plainVariant(
				template.create(
						ChandelierModelData.modelId(name),
						normalTextures,
						generators.modelOutput
				)
		);
		MultiVariant litVariant = BlockModelGenerators.plainVariant(
				template.create(
						ChandelierModelData.modelId(name + "_lit"),
						litTextures,
						generators.modelOutput
				)
		);

		return new ChandelierCandleModels(unlitVariant, litVariant);
	}
}
