/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import dev.lambdaurora.auroraslanterns.accessor.BlockEntityTypeAccessor;
import dev.lambdaurora.auroraslanterns.block.OxidizableWallLanternBlock;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.yumi.commons.event.Event;
import dev.yumi.mc.core.api.YumiEvents;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Represents the lantern registry.
 *
 * @author LambdAurora
 * @version 1.2.1
 * @since 1.0.0
 */
public final class LanternRegistry {
	private static final Map<Identifier, WallLanternBlock<?>> WALL_LANTERNS
			= new Object2ObjectOpenHashMap<>();
	private static final Map<LanternBlock, WallLanternBlock<?>> WALL_LANTERN_BLOCK_MAP
			= new Reference2ObjectOpenHashMap<>();

	public static final Event<Identifier, OnLanternRegistration> REGISTRATION_EVENT
			= YumiEvents.EVENTS.create(OnLanternRegistration.class);

	private LanternRegistry() {
		throw new UnsupportedOperationException("LanternRegistry only contains static definitions.");
	}

	public static Stream<Identifier> streamIds() {
		return WALL_LANTERNS.keySet().stream();
	}

	public static void forEach(BiConsumer<Identifier, WallLanternBlock<?>> consumer) {
		WALL_LANTERNS.forEach(consumer);
	}

	public static void forEachAndFuture(OnLanternRegistration listener) {
		WALL_LANTERNS.forEach(listener::onRegisterWallLantern);
		REGISTRATION_EVENT.register(listener);
	}

	/**
	 * Registers a wall lantern for the given lantern block.
	 *
	 * @param block the lantern block
	 * @param lanternId the lantern block id
	 * @return the wall lantern block
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <L extends LanternBlock> WallLanternBlock<L> registerWallLantern(
			Registry<Block> registry, L block, Identifier lanternId
	) {
		var wallLanternId = getWallLanternId(lanternId);

		WallLanternBlock<L> wallLanternBlock;
		if (WALL_LANTERNS.containsKey(wallLanternId))
			return (WallLanternBlock<L>) WALL_LANTERNS.get(wallLanternId);
		else if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN) {
			wallLanternBlock = (WallLanternBlock<L>) registry.getValue(wallLanternId);
		} else {
			var key = ResourceKey.of(Registries.BLOCK, wallLanternId);
			var properties = WallLanternBlock.properties(block)
					.setId(key);

			if (block instanceof WallLanternBlock.Provider<?, ?> provider) {
				wallLanternBlock = ((WallLanternBlock.Provider<L, WallLanternBlock<L>>) provider)
						.getWallLanternFactory()
						.create(block, properties);
			} else if (block instanceof WeatheringCopper) {
				wallLanternBlock = new OxidizableWallLanternBlock(block, properties);
			} else {
				wallLanternBlock = new WallLanternBlock<>(block, properties);
			}

			Registry.register(registry, key, wallLanternBlock);
			((BlockEntityTypeAccessor) AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE)
					.auroraslanterns$addSupportedBlock(wallLanternBlock);
		}

		WALL_LANTERNS.put(wallLanternId, wallLanternBlock);
		WALL_LANTERN_BLOCK_MAP.put(block, wallLanternBlock);

		REGISTRATION_EVENT.invoker().onRegisterWallLantern(wallLanternId, wallLanternBlock);

		return wallLanternBlock;
	}

	public static <L extends LanternBlock> WallLanternBlock<L> registerWallLantern(L block) {
		return registerWallLantern(BuiltInRegistries.BLOCK, block, BuiltInRegistries.BLOCK.getId(block));
	}

	public static void tryRegisterWallLantern(Registry<Block> registry, Block block, Identifier id) {
		if (block instanceof LanternBlock)
			registerWallLantern(registry, (LanternBlock) block, id);
	}

	private static Identifier getWallLanternId(Identifier lanternId) {
		var namespace = lanternId.namespace();
		var path = lanternId.path();
		var wallLanternPath = "wall_lantern";

		if (!namespace.equals("minecraft") && !namespace.equals("auroraslanterns"))
			wallLanternPath += '/' + namespace + '/' + path.replace("_lantern_block", "")
					.replace("_lantern", "");
		else {
			if (!path.equals("lantern"))
				wallLanternPath += '/' + path.replace("_lantern", "");
		}

		return AurorasLanterns.id(wallLanternPath);
	}

	public static @Nullable WallLanternBlock<?> fromItem(Item item) {
		if (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof LanternBlock lanternBlock) {
			return WALL_LANTERN_BLOCK_MAP.get(lanternBlock);
		}
		return null;
	}

	public static void rebuildCaches() {
		forEach((id, block) -> {
			if (block instanceof OxidizableWallLanternBlock<?> oxidizable) {
				var lantern = oxidizable.getLanternBlock();
				var next = WeatheringCopper.NEXT_BY_BLOCK.get().get(lantern);

				if (next instanceof LanternBlock nextLantern) {
					var nextWallLantern = WALL_LANTERN_BLOCK_MAP.get(nextLantern);
					OxidizableBlocksRegistry.registerOxidizableBlockPair(block, nextWallLantern);
				}
			}

			var nextWaxable = HoneycombItem.WAXABLES.get().get(block);
			if (nextWaxable instanceof LanternBlock waxedLantern) {
				var waxedWallLantern = WALL_LANTERN_BLOCK_MAP.get(waxedLantern);
				OxidizableBlocksRegistry.registerWaxableBlockPair(block, waxedWallLantern);
			}
		});
	}

	static {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> rebuildCaches());
	}

	@FunctionalInterface
	public interface OnLanternRegistration {
		void onRegisterWallLantern(Identifier id, WallLanternBlock<?> wallLanternBlock);
	}
}
