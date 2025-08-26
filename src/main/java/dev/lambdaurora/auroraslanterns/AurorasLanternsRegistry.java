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
import dev.lambdaurora.auroraslanterns.block.entity.WallLanternBlockEntity;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.BiFunction;

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
	public static final WallLanternBonkTrigger WALL_LANTERN_BONK_TRIGGER
			= CriteriaTriggers.register(new WallLanternBonkTrigger());
	//endregion

	//region Lanterns
	public static final AmethystLanternBlock AMETHYST_LANTERN_BLOCK = registerWithItem("amethyst_lantern",
			new AmethystLanternBlock(), new FabricItemSettings()
	);
	public static final RedstoneLanternBlock REDSTONE_LANTERN_BLOCK = registerWithItem("redstone_lantern",
			new RedstoneLanternBlock(), new FabricItemSettings()
	);
	//endregion

	//region Wall Lanterns
	public static final WallLanternBlock<LanternBlock> WALL_LANTERN_BLOCK = registerBlock(
			"wall_lantern", new WallLanternBlock<>((LanternBlock) Blocks.LANTERN)
	);
	public static final WallLanternBlock<LanternBlock> SOUL_WALL_LANTERN_BLOCK = registerBlock(
			"wall_lantern/soul", new WallLanternBlock<>((LanternBlock) Blocks.SOUL_LANTERN)
	);
	public static final WallLanternBlock<RedstoneLanternBlock> REDSTONE_WALL_LANTERN_BLOCK
			= LanternRegistry.registerWallLantern(REDSTONE_LANTERN_BLOCK);
	public static final BlockEntityType<WallLanternBlockEntity> WALL_LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			AurorasLanterns.id("wall_lantern"),
			FabricBlockEntityTypeBuilder.create(
					WallLanternBlockEntity::new, WALL_LANTERN_BLOCK, SOUL_WALL_LANTERN_BLOCK, REDSTONE_WALL_LANTERN_BLOCK
			).build()
	);
	public static final WallLanternBlock<AmethystLanternBlock> AMETHYST_WALL_LANTERN_BLOCK
			= LanternRegistry.registerWallLantern(AMETHYST_LANTERN_BLOCK);
	//endregion

	//region POI
	public static final PoiType AMETHYST_LANTERN_POI = PointOfInterestHelper.register(
			AurorasLanterns.id("amethyst_lantern"),
			0, 2,
			AMETHYST_LANTERN_BLOCK, AMETHYST_WALL_LANTERN_BLOCK
	);
	//endregion

	static <T extends Block> T registerBlock(String name, T block) {
		return Registry.register(BuiltInRegistries.BLOCK, AurorasLanterns.id(name), block);
	}

	static <T extends Item> T registerItem(String name, T item) {
		return Registry.register(BuiltInRegistries.ITEM, AurorasLanterns.id(name), item);
	}

	static <T extends Block> T registerWithItem(String name, T block, Item.Properties properties) {
		return registerWithItem(name, block, properties, BlockItem::new);
	}

	static <T extends Block> T registerWithItem(
			String name, T block, Item.Properties properties,
			BiFunction<T, Item.Properties, BlockItem> factory
	) {
		registerItem(name, factory.apply(registerBlock(name, block), properties));
		return block;
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
		BuiltInRegistries.BLOCK.holders()
				.filter(holder -> !holder.key().value().namespace().equals(AurorasLanterns.NAMESPACE))
				.toList() // Ensure we operate on an immutable copy of the known blocks.
				.forEach(holder -> handleRegisteredBlock(holder.key().value(), holder.value()));
		RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK)
				.register((rawId, id, block) -> handleRegisteredBlock(id, block));

		BuiltInRegistries.ITEM.forEach(AurorasLanternsRegistry::handleRegisteredItem);
		RegistryEntryAddedCallback.event(BuiltInRegistries.ITEM)
				.register((rawId, id, item) -> handleRegisteredItem(item));
	}
}
