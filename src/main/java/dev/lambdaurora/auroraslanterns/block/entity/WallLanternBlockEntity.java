/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.entity;

import dev.lambdaurora.auroraslanterns.AurorasLanternsRegistry;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.Nullable;

/**
 * Represents a Lantern Block Entity for the wall lanterns.
 *
 * @author LambdAurora
 * @version 1.0.1
 * @since 1.0.0
 */
public class WallLanternBlockEntity extends SwayingBlockEntity {
	private @Nullable AABB lanternCollisionBoxX;
	private @Nullable AABB lanternCollisionBoxZ;
	public float prevAngle;
	public float angle;

	public WallLanternBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasLanternsRegistry.WALL_LANTERN_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public void setLevel(Level world) {
		super.setLevel(world);
		this.updateCollisionBoxes();
	}

	/**
	 * Returns the lantern of this wall lantern as block state.
	 *
	 * @return the lantern as block state
	 */
	public BlockState getLanternState() {
		var cachedState = this.getBlockState();
		return ((WallLanternBlock<?>) cachedState.getBlock()).getLanternState(cachedState);
	}

	@Override
	public AABB getCollisionBox() {
		var swingAxis = this.getBlockState().getValue(WallLanternBlock.FACING).getClockWise().getAxis();
		return swingAxis == Direction.Axis.X ? this.lanternCollisionBoxX : this.lanternCollisionBoxZ;
	}

	@Override
	public int getMaxSwingTicks() {
		return this.getBlockState().getFluidState().isEmpty() ? 60 : 100;
	}

	private void updateCollisionBoxes() {
		var world = this.getLevel();
		if (world == null)
			return;

		var pos = this.getBlockPos();

		var lanternState = this.getLanternState();
		var box = lanternState.getShape(world, pos, CollisionContext.empty())
				.move(0, 2.0 / 16.0, 0).bounds();

		this.lanternCollisionBoxX = box.inflate(0.1, 0, 0).move(pos);
		this.lanternCollisionBoxZ = box.inflate(0, 0, 0.1).move(pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setBlockState(BlockState state) {
		super.setBlockState(state);
		this.updateCollisionBoxes();
	}

	/* Ticking */

	@Override
	protected void tickClient(Level world) {
		super.tickClient(world);

		this.prevAngle = this.angle;
		this.angle = this.computeAngle();
	}

	public float computeAngle() {
		if (this.isSwinging() || this.isColliding()) {
			float ticks = this.getAdjustedSwingTicks();
			float shiftedTicks = ticks - 100;
			return (shiftedTicks * shiftedTicks) / 5000 * Mth.sin(ticks / Mth.PI) / (4 + ticks / 3);
		} else {
			return this.getNaturalSwayingAngle();
		}
	}

	public float getNaturalSwayingAngle() {
		if (!this.canNaturallySway())
			return 0.f;

		var pos = this.getBlockPos();

		long time = 0;
		var world = this.getLevel();
		if (world != null) {
			time = world.getGameTime();
		}

		int period = 125;
		float n = ((float) Math.floorMod(pos.getX() * 7L + pos.getY() * 9L + pos.getZ() * 13L + time, (long) period))
				/ period;
		return (float) ((.01f * Mth.cos((float) (Math.PI * 2 * n))) * Math.PI);
	}
}
