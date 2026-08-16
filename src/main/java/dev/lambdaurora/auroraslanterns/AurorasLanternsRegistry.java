/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import dev.lambdaurora.auroraslanterns.accessor.BlockItemAccessor;
import dev.lambdaurora.auroraslanterns.advancement.WallLanternBonkTrigger;
import dev.lambdaurora.auroraslanterns.block.AmethystLanternBlock;
import dev.lambdaurora.auroraslanterns.block.RedstoneLanternBlock;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.lambdaurora.auroraslanterns.block.behavior.RedstoneLanternBehavior;
import dev.lambdaurora.auroraslanterns.block.entity.ChandelierBlockEntity;
import dev.lambdaurora.auroraslanterns.block.entity.WallLanternBlockEntity;
import dev.lambdaurora.auroraslanterns.compat.AurorasDecoDataUpper;
import dev.lambdaurora.auroraslanterns.item.ChandelierItem;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public final class AurorasLanternsRegistry {
	private AurorasLanternsRegistry() {
		throw new UnsupportedOperationException("AurorasLanternsRegistry only contains static definitions.");
	}

	//region Particles
	public static final SimpleParticleType AMETHYST_GLINT_PARTICLE_TYPE = Registry.register(
			BuiltInRegistries.PARTICLE_TYPE, AurorasLanterns.id("amethyst_glint"), FabricParticleTypes.simple()
	);
	//endregion

	//region Sounds
	public static final SoundEvent LANTERN_SWING_SOUND_EVENT = register("block.lantern.swing");

	private static SoundEvent register(String path) {
		var id = AurorasLanterns.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}
	//endregion

	//region Advancement Triggers
	public static final WallLanternBonkTrigger WALL_LANTERN_BONK_TRIGGER = Registry.register(
			BuiltInRegistries.TRIGGER_TYPES,
			AurorasLanterns.id("wall_lantern_bonk"),
			new WallLanternBonkTrigger()
	);
	//endregion

	//region Lanterns
	public static final AmethystLanternBlock AMETHYST_LANTERN_BLOCK = registerBlock(AurorasLanternsIds.AMETHYST_LANTERN_ID,
			AmethystLanternBlock::new,
			Block.Properties.ofFullCopy(Blocks.LANTERN)
					.lightLevel(state -> 14)
	);
	public static final Item AMETHYST_LANTERN_ITEM = Items.registerBlock(AurorasLanternsIds.AMETHYST_LANTERN, AMETHYST_LANTERN_BLOCK);

	public static final RedstoneLanternBlock REDSTONE_LANTERN_BLOCK = registerBlock(AurorasLanternsIds.REDSTONE_LANTERN_ID,
			RedstoneLanternBlock::new,
			Block.Properties.ofFullCopy(Blocks.LANTERN)
					.lightLevel(state -> state.getValue(RedstoneLanternBehavior.LIT) ? 7 : 0)
	);
	public static final Item REDSTONE_LANTERN_ITEM = Items.registerBlock(AurorasLanternsIds.REDSTONE_LANTERN, REDSTONE_LANTERN_BLOCK);
	//endregion

	//region Chandeliers
	public static final ChandelierBlocks IRON_CHANDELIER_BLOCKS = ChandelierBlocks.create("iron");
	public static final Item IRON_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/iron")),
			properties -> new ChandelierItem(IRON_CHANDELIER_BLOCKS, properties)
	);
	public static final OxidizableChandelierBlocks COPPER_CHANDELIER_BLOCKS = OxidizableChandelierBlocks.create("copper");
	public static final Item COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.unaffected(), properties)
	);
	public static final Item EXPOSED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/exposed_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.exposed(), properties)
	);
	public static final Item WEATHERED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/weathered_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.weathered(), properties)
	);
	public static final Item OXIDIZED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/oxidized_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.oxidized(), properties)
	);
	public static final Item WAXED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/waxed_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.waxed(), properties)
	);
	public static final Item WAXED_EXPOSED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/waxed_exposed_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.waxedExposed(), properties)
	);
	public static final Item WAXED_WEATHERED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/waxed_weathered_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.waxedWeathered(), properties)
	);
	public static final Item WAXED_OXIDIZED_COPPER_CHANDELIER_ITEM = Items.registerItem(
			ResourceKey.create(Registries.ITEM, AurorasLanterns.id("chandelier/waxed_oxidized_copper")),
			properties -> new ChandelierItem(COPPER_CHANDELIER_BLOCKS.waxedOxidized(), properties)
	);
	public static final BlockEntityType<ChandelierBlockEntity> CHANDELIER_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			AurorasLanterns.id("chandelier"),
			Util.make(() -> {
				var builder = FabricBlockEntityTypeBuilder.create(
						ChandelierBlockEntity::new
				);
				IRON_CHANDELIER_BLOCKS.forEach(builder::addBlock);
				COPPER_CHANDELIER_BLOCKS.forEach(builder::addBlock);
				return builder.build();
			})
	);
	//endregion

	//region Wall Lanterns
	public static final WallLanternBlock<LanternBlock> WALL_LANTERN_BLOCK = registerBlock(
			AurorasLanterns.id("wall_lantern"),
			properties -> new WallLanternBlock<>((LanternBlock) Blocks.LANTERN, properties),
			WallLanternBlock.properties(Blocks.LANTERN)
	);
	public static final WallLanternBlock<LanternBlock> SOUL_WALL_LANTERN_BLOCK = registerBlock(
			AurorasLanterns.id("wall_lantern/soul"),
			properties -> new WallLanternBlock<>((LanternBlock) Blocks.SOUL_LANTERN, properties),
			WallLanternBlock.properties(Blocks.SOUL_LANTERN)
	);
	public static final Identifier WALL_LANTERN_BLOCK_ENTITY_TYPE_ID = AurorasLanterns.id("wall_lantern");
	public static final BlockEntityType<WallLanternBlockEntity> WALL_LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			WALL_LANTERN_BLOCK_ENTITY_TYPE_ID,
			FabricBlockEntityTypeBuilder.create(
					WallLanternBlockEntity::new, WALL_LANTERN_BLOCK, SOUL_WALL_LANTERN_BLOCK
			).build()
	);
	public static final WallLanternBlock<AmethystLanternBlock> AMETHYST_WALL_LANTERN_BLOCK
			= LanternRegistry.registerWallLantern(AMETHYST_LANTERN_BLOCK);
	public static final WallLanternBlock<RedstoneLanternBlock> REDSTONE_WALL_LANTERN_BLOCK
			= LanternRegistry.registerWallLantern(REDSTONE_LANTERN_BLOCK);
	//endregion

	//region POI
	public static final PoiType AMETHYST_LANTERN_POI = PoiHelper.register(
			AurorasLanterns.id("amethyst_lantern"),
			0, 2,
			AMETHYST_LANTERN_BLOCK, AMETHYST_WALL_LANTERN_BLOCK
	);
	//endregion

	static <T extends Block> T registerBlock(
			Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties
	) {
		var key = ResourceKey.create(Registries.BLOCK, id);
		var block = factory.apply(properties.setId(key));
		return Registry.register(BuiltInRegistries.BLOCK, id, block);
	}

	private static void handleRegisteredBlock(Identifier id, Block block) {
		LanternRegistry.tryRegisterWallLantern(BuiltInRegistries.BLOCK, block, id);
	}

	private static void handleRegisteredItem(Item item) {
		if (item instanceof BlockItem blockItem) {
			var accessor = (BlockItemAccessor) item;

			if (blockItem.getBlock() instanceof LanternBlock) {
				var lanternBlock = LanternRegistry.fromItem(item);
				if (lanternBlock != null)
					accessor.auroraslanterns$setWallBlock(lanternBlock);
				Item.BY_BLOCK.put(lanternBlock, item);
			}
		}
	}

	static void init() {
		final var phaseId = AurorasLanterns.id("lantern_lookup");

		BuiltInRegistries.BLOCK.listElements()
				.filter(holder -> !holder.key().identifier().getNamespace().equals(AurorasLanterns.NAMESPACE))
				.toList() // Ensure we operate on an immutable copy of the known blocks.
				.forEach(holder -> handleRegisteredBlock(holder.key().identifier(), holder.value()));
		var blockEvent = RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK);
		blockEvent.register(phaseId, (_, id, block) -> handleRegisteredBlock(id, block));
		blockEvent.addPhaseOrdering(Event.DEFAULT_PHASE, phaseId);

		BuiltInRegistries.ITEM.forEach(AurorasLanternsRegistry::handleRegisteredItem);
		RegistryEntryAddedCallback.event(BuiltInRegistries.ITEM)
				.register((_, _, item) -> handleRegisteredItem(item));

		OxidizableChandelierBlocks.registerWeatheringStates(COPPER_CHANDELIER_BLOCKS);

		AurorasDecoDataUpper.init();
	}
}
