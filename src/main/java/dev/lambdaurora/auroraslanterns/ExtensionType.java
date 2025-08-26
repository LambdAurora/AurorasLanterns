/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns;

import dev.lambdaurora.auroraslanterns.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * Represents an extension type for blocks placed on horizontal sides which can also be placed on fences or walls.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ExtensionType implements StringRepresentable {
	NONE("none", 0),
	WALL("wall", 2),
	FENCE("fence", 4);

	private final String name;
	private final int offset;

	ExtensionType(String name, int offset) {
		this.name = name;
		this.offset = offset;
	}

	public int getOffset() {
		return this.offset;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public static final AABB FENCE_SHAPE = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
			.bounds();
	public static final AABB WALL_SHAPE = Block.box(7.0, 0.0, 7.0, 12.0, 16.0, 12.0)
			.bounds();

	/**
	 * Returns the extension value.
	 *
	 * @param state the block to connect to
	 * @param pos the position of the block to connect to
	 * @param world the world
	 * @return the extension value
	 */
	public static ExtensionType getExtensionValue(BlockState state, BlockPos pos, LevelReader world) {
		var block = state.getBlock();

		var shape = state.getBlockSupportShape(world, pos);
		if (shape != Shapes.empty()) {
			var box = shape.bounds();
			if (block instanceof FenceBlock || state.is(BlockTags.FENCES) || Utils.isShapeEqual(FENCE_SHAPE, box))
				return FENCE;
			if (block instanceof WallBlock || state.is(BlockTags.WALLS) || Utils.isShapeEqual(WALL_SHAPE, box))
				return WALL;
		}

		return NONE;
	}
}
