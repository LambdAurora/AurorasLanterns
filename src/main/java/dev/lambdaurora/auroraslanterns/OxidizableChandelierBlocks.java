/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import dev.lambdaurora.auroraslanterns.ChandelierBlocks.AttachmentEntry;
import dev.lambdaurora.auroraslanterns.block.chandelier.AbstractChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.WeatheringCeilingChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.WeatheringStandingChandelierBlock;
import dev.lambdaurora.auroraslanterns.block.chandelier.WeatheringWallChandelierBlock;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.world.level.block.WeatheringCopper;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Represents a set of oxidizable chandelier blocks.
 *
 * @param unaffected the unaffected chandelier block set
 * @param exposed the exposed chandelier block set
 * @param weathered the weathered chandelier block set
 * @param oxidized the oxidized chandelier block set
 * @param waxed the waxed chandelier block set
 * @param waxedExposed the waxed exposed chandelier block set
 * @param waxedWeathered the waxed weathered chandelier block set
 * @param waxedOxidized the waxed oxidized chandelier block set
 * @author LambdAurora
 * @version 2.0.0
 * @since 2.0.0
 */
public record OxidizableChandelierBlocks(
		ChandelierBlocks unaffected, ChandelierBlocks exposed, ChandelierBlocks weathered, ChandelierBlocks oxidized,
		ChandelierBlocks waxed, ChandelierBlocks waxedExposed, ChandelierBlocks waxedWeathered, ChandelierBlocks waxedOxidized
) {
	public static OxidizableChandelierBlocks create(
			String type
	) {
		return new OxidizableChandelierBlocks(
				createWeathering(type, WeatheringCopper.WeatherState.UNAFFECTED),
				createWeathering("exposed_" + type, WeatheringCopper.WeatherState.EXPOSED),
				createWeathering("weathered_" + type, WeatheringCopper.WeatherState.WEATHERED),
				createWeathering("oxidized_" + type, WeatheringCopper.WeatherState.OXIDIZED),
				ChandelierBlocks.create("waxed_" + type),
				ChandelierBlocks.create("waxed_exposed_" + type),
				ChandelierBlocks.create("waxed_weathered_" + type),
				ChandelierBlocks.create("waxed_oxidized_" + type)
		);
	}

	private static ChandelierBlocks createWeathering(
			String type, WeatheringCopper.WeatherState weatherState
	) {
		return ChandelierBlocks.create(
				type,
				(holders, properties) -> new WeatheringCeilingChandelierBlock(weatherState, holders, properties),
				(holders, properties) -> new WeatheringWallChandelierBlock(weatherState, holders, properties),
				(holders, properties) -> new WeatheringStandingChandelierBlock(weatherState, holders, properties)
		);
	}

	public static void registerWeatheringStates(OxidizableChandelierBlocks blocks) {
		registerWeatheringStates(
				blocks.unaffected, blocks.exposed, blocks.weathered, blocks.oxidized,
				blocks.waxed, blocks.waxedExposed, blocks.waxedWeathered, blocks.waxedOxidized
		);
	}

	private static void registerWeatheringStates(
			ChandelierBlocks unaffected, ChandelierBlocks exposed, ChandelierBlocks weathered, ChandelierBlocks oxidized,
			ChandelierBlocks waxed, ChandelierBlocks waxedExposed, ChandelierBlocks waxedWeathered, ChandelierBlocks waxedOxidized
	) {
		registerWeatheringStates(unaffected.ceiling(), exposed.ceiling(), weathered.ceiling(), oxidized.ceiling());
		registerWeatheringStates(unaffected.wall(), exposed.wall(), weathered.wall(), oxidized.wall());
		registerWeatheringStates(unaffected.standing(), exposed.standing(), weathered.standing(), oxidized.standing());

		registerWaxableStates(unaffected, waxed);
		registerWaxableStates(exposed, waxedExposed);
		registerWaxableStates(weathered, waxedWeathered);
		registerWaxableStates(oxidized, waxedOxidized);
	}

	private static void registerWeatheringStates(
			AttachmentEntry<?> unaffected,
			AttachmentEntry<?> exposed,
			AttachmentEntry<?> weathered,
			AttachmentEntry<?> oxidized
	) {
		OxidizableBlocksRegistry.registerNextStage(unaffected.single(), exposed.single());
		OxidizableBlocksRegistry.registerNextStage(unaffected.duo(), exposed.duo());
		OxidizableBlocksRegistry.registerNextStage(unaffected.trio(), exposed.trio());
		OxidizableBlocksRegistry.registerNextStage(unaffected.quad(), exposed.quad());
		OxidizableBlocksRegistry.registerNextStage(exposed.single(), weathered.single());
		OxidizableBlocksRegistry.registerNextStage(exposed.duo(), weathered.duo());
		OxidizableBlocksRegistry.registerNextStage(exposed.trio(), weathered.trio());
		OxidizableBlocksRegistry.registerNextStage(exposed.quad(), weathered.quad());
		OxidizableBlocksRegistry.registerNextStage(weathered.single(), oxidized.single());
		OxidizableBlocksRegistry.registerNextStage(weathered.duo(), oxidized.duo());
		OxidizableBlocksRegistry.registerNextStage(weathered.trio(), oxidized.trio());
		OxidizableBlocksRegistry.registerNextStage(weathered.quad(), oxidized.quad());
	}

	private static void registerWaxableStates(
			ChandelierBlocks unwaxed, ChandelierBlocks waxed
	) {
		registerWaxableStates(unwaxed.ceiling(), waxed.ceiling());
		registerWaxableStates(unwaxed.wall(), waxed.wall());
		registerWaxableStates(unwaxed.standing(), waxed.standing());
	}

	private static void registerWaxableStates(
			AttachmentEntry<?> unwaxed,
			AttachmentEntry<?> waxed
	) {
		OxidizableBlocksRegistry.registerWaxable(unwaxed.single(), waxed.single());
		OxidizableBlocksRegistry.registerWaxable(unwaxed.duo(), waxed.duo());
		OxidizableBlocksRegistry.registerWaxable(unwaxed.trio(), waxed.trio());
		OxidizableBlocksRegistry.registerWaxable(unwaxed.quad(), waxed.quad());
	}

	public void forEach(Consumer<? super AbstractChandelierBlock> consumer) {
		this.unaffected.forEach(consumer);
		this.exposed.forEach(consumer);
		this.weathered.forEach(consumer);
		this.oxidized.forEach(consumer);
		this.waxed.forEach(consumer);
		this.waxedExposed.forEach(consumer);
		this.waxedWeathered.forEach(consumer);
		this.waxedOxidized.forEach(consumer);
	}

	public Stream<AbstractChandelierBlock> stream() {
		var builder = Stream.<AbstractChandelierBlock>builder();
		this.forEach(builder::add);
		return builder.build();
	}
}
