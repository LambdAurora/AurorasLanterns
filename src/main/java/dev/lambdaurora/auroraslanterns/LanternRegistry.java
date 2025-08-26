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
import dev.lambdaurora.auroraslanterns.block.RedstoneLanternBlock;
import dev.lambdaurora.auroraslanterns.block.RedstoneWallLanternBlock;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.yumi.commons.event.Event;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class LanternRegistry {
	private static final Map<Identifier, WallLanternBlock<?>> WALL_LANTERNS = new Object2ObjectOpenHashMap<>();
	private static final Map<LanternBlock, WallLanternBlock<?>> WALL_LANTERN_BLOCK_MAP = new Reference2ObjectOpenHashMap<>();

	public static final Event<Identifier, OnLanternRegistration> REGISTRATION_EVENT
			= AurorasLanterns.EVENT_MANAGER.create(OnLanternRegistration.class);

	public static Stream<Identifier> streamIds() {
		return WALL_LANTERNS.keySet().stream();
	}

	public static void forEach(BiConsumer<Identifier, WallLanternBlock<?>> consumer) {
		WALL_LANTERNS.forEach(consumer);
	}

	public static void forEachAndFuture(OnLanternRegistration listener) {
		WALL_LANTERNS.values().forEach(listener::onRegisterWallLantern);
		REGISTRATION_EVENT.register(listener);
	}

	/**
	 * Registers a wall lantern for the given lantern block.
	 *
	 * @param block the lantern block
	 * @param lanternId the lantern block id
	 * @return the wall lantern block
	 */
	@SuppressWarnings("unchecked")
	public static <L extends LanternBlock> WallLanternBlock<L> registerWallLantern(Registry<Block> registry, L block, Identifier lanternId) {
		var wallLanternId = getWallLanternId(lanternId);

		WallLanternBlock<L> wallLanternBlock;
		if (WALL_LANTERNS.containsKey(wallLanternId))
			return (WallLanternBlock<L>) WALL_LANTERNS.get(wallLanternId);
		else if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN) {
			wallLanternBlock = (WallLanternBlock<L>) BuiltInRegistries.BLOCK.get(wallLanternId);
		} else if (block instanceof RedstoneLanternBlock redstoneLanternBlock) {
			wallLanternBlock = (WallLanternBlock<L>) Registry.register(
					registry, wallLanternId, new RedstoneWallLanternBlock(redstoneLanternBlock)
			);
		} else {
			wallLanternBlock = Registry.register(registry, wallLanternId, new WallLanternBlock<>(block));
			((BlockEntityTypeAccessor) AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE)
					.auroraslanterns$addSupportedBlock(wallLanternBlock);
		}

		WALL_LANTERNS.put(wallLanternId, wallLanternBlock);
		WALL_LANTERN_BLOCK_MAP.put(block, wallLanternBlock);

		REGISTRATION_EVENT.invoker().onRegisterWallLantern(wallLanternBlock);

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

	@FunctionalInterface
	public interface OnLanternRegistration {
		void onRegisterWallLantern(WallLanternBlock<?> wallLanternBlock);
	}
}
