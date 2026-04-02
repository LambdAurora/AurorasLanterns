/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.compat.AurorasDecoDataUpper;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.BlockEntityRenameFix;
import net.minecraft.util.datafix.fixes.BlockRenameFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiFunction;

@Mixin(DataFixers.class)
public class DataFixersMixin {
	@WrapOperation(
			method = "addFixers",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/datafixers/DataFixerBuilder;addSchema(ILjava/util/function/BiFunction;)Lcom/mojang/datafixers/schemas/Schema;"
			)
	)
	private static Schema auroraslanterns$onAddFixers(
			DataFixerBuilder instance, int version, BiFunction<Integer, Schema, Schema> factory, Operation<Schema> original,
			DataFixerBuilder builder
	) {
		var schema = original.call(instance, version, factory);

		if (version == 3945) {
			builder.addFixer(BlockEntityRenameFix.create(schema,
					"Rename Aurora's Decorations wall lantern block entity to Aurora's Lanterns'.",
					oldName -> {
						var name = NamespacedSchema.ensureNamespaced(oldName);

						if (name.equals(AurorasDecoDataUpper.OLD_NAMESPACE + ":lantern")) {
							return AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE_ID.toString();
						}

						return oldName;
					}
			));
			builder.addFixer(BlockRenameFix.create(schema, "Rename Aurora's Decorations chandeliers to candles.",
					oldName -> {
						var name = NamespacedSchema.ensureNamespaced(oldName);

						final var chandelierPrefix = AurorasDecoDataUpper.OLD_NAMESPACE + ":chandelier/";
						final var wallPrefix = AurorasDecoDataUpper.OLD_NAMESPACE + ":wall_";
						if (name.startsWith(chandelierPrefix)) {
							var type = name.substring(chandelierPrefix.length());

							if (type.equals("candle")) {
								return "minecraft:candle";
							} else {
								return "minecraft:" + type + "_candle";
							}
						} else if (name.startsWith(wallPrefix) && name.endsWith("candle")) {
							var type = name.substring(wallPrefix.length());

							return "minecraft:" + type;
						}

						return oldName;
					}
			));
		}

		return schema;
	}
}
