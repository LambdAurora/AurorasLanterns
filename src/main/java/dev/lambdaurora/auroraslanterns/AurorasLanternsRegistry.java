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
import dev.lambdaurora.auroraslanterns.accessor.RegistryEventStorage;
import dev.lambdaurora.auroraslanterns.advancement.WallLanternBonkTrigger;
import dev.lambdaurora.auroraslanterns.block.AmethystLanternBlock;
import dev.lambdaurora.auroraslanterns.block.RedstoneLanternBlock;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.lambdaurora.auroraslanterns.block.behavior.RedstoneLanternBehavior;
import dev.lambdaurora.auroraslanterns.block.entity.WallLanternBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
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
	public static final WallLanternBonkTrigger WALL_LANTERN_BONK_TRIGGER
			= CriteriaTriggers.register(new WallLanternBonkTrigger());
	//endregion

	//region Lanterns
	public static final Identifier AMETHYST_LANTERN_ID = AurorasLanterns.id("amethyst_lantern");
	public static final AmethystLanternBlock AMETHYST_LANTERN_BLOCK = registerBlock(AMETHYST_LANTERN_ID,
			AmethystLanternBlock::new,
			FabricBlockSettings.copyOf(Blocks.LANTERN)
					.luminance(state -> 14)
	);
	public static final Item AMETHYST_LANTERN_ITEM = Items.registerBlock(AMETHYST_LANTERN_BLOCK);

	public static final Identifier REDSTONE_LANTERN_ID = AurorasLanterns.id("redstone_lantern");
	public static final RedstoneLanternBlock REDSTONE_LANTERN_BLOCK = registerBlock(REDSTONE_LANTERN_ID,
			RedstoneLanternBlock::new,
			FabricBlockSettings.copyOf(Blocks.LANTERN)
					.luminance(state -> state.get(RedstoneLanternBehavior.LIT) ? 7 : 0)
	);
	public static final Item REDSTONE_LANTERN_ITEM = Items.registerBlock(REDSTONE_LANTERN_BLOCK);
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
	public static final BlockEntityType<WallLanternBlockEntity> WALL_LANTERN_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			AurorasLanterns.id("wall_lantern"),
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
	public static final PoiType AMETHYST_LANTERN_POI = PointOfInterestHelper.register(
			AurorasLanterns.id("amethyst_lantern"),
			0, 2,
			AMETHYST_LANTERN_BLOCK, AMETHYST_WALL_LANTERN_BLOCK
	);
	//endregion


	static <T extends Block> T registerBlock(
			Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties
	) {
		var key = ResourceKey.of(Registries.BLOCK, id);
		var block = factory.apply(properties);
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
		BuiltInRegistries.BLOCK.holders()
				.filter(holder -> !holder.key().value().namespace().equals(AurorasLanterns.NAMESPACE))
				.toList() // Ensure we operate on an immutable copy of the known blocks.
				.forEach(holder -> handleRegisteredBlock(holder.key().value(), holder.value()));
		RegistryEventStorage.of(BuiltInRegistries.BLOCK)
				.auroraslanterns$getAddEvent()
				.register(AurorasLanternsRegistry::handleRegisteredBlock);

		BuiltInRegistries.ITEM.forEach(AurorasLanternsRegistry::handleRegisteredItem);
		RegistryEventStorage.of(BuiltInRegistries.ITEM)
				.auroraslanterns$getAddEvent()
				.register((id, item) -> handleRegisteredItem(item));
	}
}
