/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.entity;

import com.mojang.serialization.Codec;
import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ChandelierBlockEntity extends BlockEntity implements RenderDataBlockEntity {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChandelierBlockEntity.class);
	private Candle[] candles = new Candle[]{Candle.Normal.INSTANCE};

	public ChandelierBlockEntity(BlockPos pos, BlockState blockState) {
		super(AurorasLanternsRegistry.CHANDELIER_BLOCK_ENTITY_TYPE, pos, blockState);
	}

	@Override
	public @Nullable Object getRenderData() {
		var candles = new Candle[this.candles.length];
		System.arraycopy(this.candles, 0, candles, 0, this.candles.length);
		return candles;
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		var list = input.list("candles", Candle.CODEC);
		this.candles = list.map(this::mapToArray)
				.orElseGet(() -> new Candle[]{Candle.Normal.INSTANCE});
	}

	private Candle[] mapToArray(ValueInput.TypedInputList<Candle> input) {
		var candles = input.stream().toList();
		return candles.subList(0, Math.min(candles.size(), 4)).toArray(Candle[]::new);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		var list = output.list("candles", Candle.CODEC);
		for (var candle : this.candles) {
			list.add(candle);
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this.problemPath(), LOGGER)) {
			TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, registries);
			this.saveAdditional(tagValueOutput);
			return tagValueOutput.buildResult();
		}
	}

	public sealed interface Candle {
		Map<String, Candle> BY_NAME = Util.make(() -> {
			var map = new HashMap<String, Candle>();
			map.put(Normal.INSTANCE.name(), Normal.INSTANCE);
			Colored.BY_COLOR.values().forEach(color -> map.put(color.name(), color));
			return Map.copyOf(map);
		});
		Codec<Candle> CODEC = Codec.stringResolver(
				Candle::name,
				name -> BY_NAME.getOrDefault(name, Normal.INSTANCE)
		);

		String name();

		String vanillaPrefix();

		final class Normal implements Candle {
			public static final Normal INSTANCE = new Normal();

			@Override
			public String name() {
				return "normal";
			}

			@Override
			public String vanillaPrefix() {
				return "";
			}
		}

		record Colored(DyeColor color) implements Candle {
			public static Map<DyeColor, Colored> BY_COLOR = Util.make(() -> {
				var map = new HashMap<DyeColor, Colored>();
				for (var color : DyeColor.values()) {
					map.put(color, new Colored(color));
				}
				return Collections.unmodifiableMap(new EnumMap<>(map));
			});

			@Override
			public String name() {
				return this.color.getName();
			}

			@Override
			public String vanillaPrefix() {
				return this.name() + "_";
			}
		}
	}
}
