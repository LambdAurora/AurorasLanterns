
/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.model;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.math.Quadrant;
import com.mojang.serialization.JsonOps;
import dev.lambdaurora.auroraslanterns.AurorasLanterns;
import dev.lambdaurora.auroraslanterns.ChandelierBlocks;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock.AttachmentType;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock.Candle;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public record ChandelierModelData(
		Map<BlockState, Map<Candle, BlockStateModel.@Nullable UnbakedRoot[]>> models
) {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChandelierModelData.class);
	public static final KeyValueCondition.Terms UNLIT_CONDITION_TERMS = new KeyValueCondition.Terms(List.of(
			new KeyValueCondition.Term("0", false)
	));
	public static final Condition UNLIT_CONDITION = new KeyValueCondition(Map.of(
			"lit_candles", UNLIT_CONDITION_TERMS
	));
	public static final KeyValueCondition.Terms LIT_CONDITION_TERMS = new KeyValueCondition.Terms(List.of(
			new KeyValueCondition.Term("0", true)
	));
	public static final Condition LIT_CONDITION = new KeyValueCondition(Map.of(
			"lit_candles",
			LIT_CONDITION_TERMS
	));

	public static Identifier blockStateId(String name) {
		return AurorasLanterns.id("chandelier/candle/" + name);
	}

	public static Identifier modelId(String name) {
		return AurorasLanterns.id("block/chandelier/candle/" + name);
	}

	public static ChandelierModelData resolve(PreparableReloadListener.SharedState sharedState) {
		var map = new HashMap<BlockState, Map<Candle, BlockStateModel.@Nullable UnbakedRoot[]>>();
		ModelConsumer collector =
				(state, candle, holders, i, model) -> {
					map.computeIfAbsent(state, k -> new HashMap<>())
							.computeIfAbsent(candle, k -> new BlockStateModel.UnbakedRoot[holders])[i] = model;
				};
		resolve(sharedState, AttachmentType.CEILING, collector);
		resolve(sharedState, AttachmentType.WALL, collector);
		resolve(sharedState, AttachmentType.STANDING, collector);
		return new ChandelierModelData(Collections.unmodifiableMap(map));
	}

	private static Map<Candle, @Nullable PendingEntry[]> resolveModelDefinitions(
			PreparableReloadListener.SharedState sharedState,
			AttachmentType attachmentType
	) {
		var map = new HashMap<Candle, PendingEntry[]>();

		for (var candle : Candle.BY_NAME.values()) {
			var name = candle.vanillaPrefix() + "candle";
			var entries = new PendingEntry[10];
			map.put(candle, entries);

			int i = 0;
			for (int candles = 1; candles <= 4; candles++) {
				for (int currentCandle = 1; currentCandle <= candles; currentCandle++) {
					String suffix = candles == 1 ? "" : ("_" + currentCandle);

					String path = attachmentType.id() + "/" + name + "_" + candles + suffix;
					var blockStateId = blockStateId(path);
					var resourceId = blockStateId.withPath(p -> "blockstates/" + p + ".json");

					var resourceCandidate = sharedState.resourceManager().getResource(resourceId);

					if (resourceCandidate.isPresent()) {
						try (var reader = resourceCandidate.get().openAsReader()) {
							var json = JsonParser.parseReader(reader);

							entries[i] = new PendingEntry(
									BlockModelDefinition.CODEC.parse(JsonOps.INSTANCE, json)
											.getOrThrow(JsonParseException::new),
									blockStateId.toString()
							);
						} catch (Exception e) {
							LOGGER.error("Failed to load chandelier candle block state definition {}.", blockStateId, e);
						}
					} else {
						entries[i] = new PendingEntry(
								generateBlockModelDefinition(path, attachmentType),
								blockStateId.toString()
						);
					}

					i++;
				}
			}
		}

		return map;
	}

	private static BlockModelDefinition generateBlockModelDefinition(
			String path, AttachmentType attachmentType
	) {
		return attachmentType != AttachmentType.WALL
				? generateSimpleBlockModelDefinition(path)
				: generateWallBlockModelDefinition(path);
	}

	private static BlockModelDefinition generateSimpleBlockModelDefinition(String path) {
		return new BlockModelDefinition(Optional.empty(), Optional.of(new BlockModelDefinition.MultiPartDefinition(
				List.of(
						new Selector(
								Optional.of(UNLIT_CONDITION),
								new SingleVariant.Unbaked(new Variant(modelId(path)))
						),
						new Selector(
								Optional.of(LIT_CONDITION),
								new SingleVariant.Unbaked(new Variant(modelId(path + "_lit")))
						)
				)
		)));
	}

	private static BlockModelDefinition generateWallBlockModelDefinition(String path) {
		var unlitVariant = new Variant(modelId(path));
		var litVariant = new Variant(modelId(path + "_lit"));
		return new BlockModelDefinition(Optional.empty(), Optional.of(new BlockModelDefinition.MultiPartDefinition(
				List.of(
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", UNLIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.NORTH)
								))),
								new SingleVariant.Unbaked(unlitVariant.withYRot(Quadrant.R180))
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", LIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.NORTH)
								))),
								new SingleVariant.Unbaked(litVariant.withYRot(Quadrant.R180))
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", UNLIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.SOUTH)
								))),
								new SingleVariant.Unbaked(unlitVariant)
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", LIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.SOUTH)
								))),
								new SingleVariant.Unbaked(litVariant)
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", UNLIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.WEST)
								))),
								new SingleVariant.Unbaked(unlitVariant.withYRot(Quadrant.R90))
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", LIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.WEST)
								))),
								new SingleVariant.Unbaked(litVariant.withYRot(Quadrant.R90))
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", UNLIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.EAST)
								))),
								new SingleVariant.Unbaked(unlitVariant.withYRot(Quadrant.R270))
						),
						new Selector(
								Optional.of(new KeyValueCondition(Map.of(
										"lit_candles", LIT_CONDITION_TERMS,
										"facing", makeFacingTerms(Direction.EAST)
								))),
								new SingleVariant.Unbaked(litVariant.withYRot(Quadrant.R270))
						)
				)
		)));
	}

	private static KeyValueCondition.Terms makeFacingTerms(Direction direction) {
		return new KeyValueCondition.Terms(List.of(
				new KeyValueCondition.Term(direction.getName(), false)
		));
	}

	@SuppressWarnings("deprecation")
	private static void resolve(
			PreparableReloadListener.SharedState sharedState,
			AttachmentType attachmentType,
			ModelConsumer consumer
	) {
		var loaded = resolveModelDefinitions(sharedState, attachmentType);

		ChandelierBlocks.streamAll().flatMap(ChandelierBlocks::stream)
				.filter(block -> block.attachmentType() == attachmentType)
				.forEach(block -> {
					loaded.forEach((candle, entries) -> {
						int start = startIndex(block.holders());

						for (int i = start; i < start + block.holders(); i++) {
							var model = entries[i];
							if (model != null) {
								var models = model.definition.instantiate(block.getStateDefinition(), () ->
										model.source + " (" + block.builtInRegistryHolder().key().identifier() + ")"
								);

								int finalI = i - start;
								models.forEach((state, unbakedModel) -> {
									consumer.accept(state, candle, block.holders(), finalI, unbakedModel);
								});
							}
						}
					});
				});
	}

	private static int startIndex(int holders) {
		return switch (holders) {
			case 2 -> 1;
			case 3 -> 1 + 2;
			case 4 -> 1 + 2 + 3;
			default -> 0;
		};
	}

	@FunctionalInterface
	interface ModelConsumer {
		void accept(
				BlockState state, Candle candle, int holders, int index, BlockStateModel.UnbakedRoot model
		);
	}

	record PendingEntry(BlockModelDefinition definition, String source) {
	}
}
