/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.block.entity;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.Set;

public abstract class SwayingBlockEntity extends BlockEntity {
	protected boolean naturalSway = false;
	private int swingTicks;
	private boolean swinging;
	private Direction swingBaseDirection;
	private boolean colliding = false;
	private final Set<Entity> collisions = new ObjectOpenHashSet<>();

	public SwayingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * Returns whether this block entity can naturally sway.
	 *
	 * @return {@code true} if this block entity can naturally sway, or {@code false} otherwise
	 */
	public boolean canNaturallySway() {
		return this.naturalSway;
	}

	/**
	 * {@return the collision box of the swaying part of this block entity}
	 */
	public abstract AABB getCollisionBox();

	public int getSwingTicks() {
		return this.swingTicks;
	}

	public float getAdjustedSwingTicks() {
		boolean fluid = !this.getBlockState().getFluidState().isEmpty();
		float ticks = (float) this.getSwingTicks();

		if (this.isColliding() && ticks > 4) {
			ticks = 4.f;
		}
		if (fluid)
			ticks /= 2.f;

		return ticks;
	}

	/**
	 * {@return the max swing ticks}
	 */
	public abstract int getMaxSwingTicks();

	/**
	 * Returns whether this swaying block entity is being force to sway or not.
	 *
	 * @return {@code true} if this swaying block entity is swaying, or {@code false} otherwise
	 */
	public boolean isSwinging() {
		return this.swinging;
	}

	public Direction getSwingBaseDirection() {
		return this.swingBaseDirection;
	}

	/**
	 * Returns whether this swaying block entity is colliding with an entity or not.
	 *
	 * @return {@code true} if this swaying block entity is colliding with an entity, or {@code false} otherwise
	 */
	public boolean isColliding() {
		return this.colliding;
	}

	/**
	 * Swings the swaying block entity in a given direction.
	 *
	 * @param direction the direction to swing to
	 */
	public void activate(Direction direction) {
		var blockPos = this.getBlockPos();
		this.swingBaseDirection = direction;
		if (this.swinging) {
			if (this.isColliding())
				this.swingTicks = 4;
			else
				this.swingTicks = 0;
		} else {
			this.swinging = true;
		}

		this.getLevel().blockEvent(blockPos, this.getBlockState().getBlock(), 1, direction.get3DDataValue());
	}

	/**
	 * Swings the swaying block entity in a given direction. Caused by an entity collision.
	 *
	 * @param direction the direction to swing to
	 * @param entity the entity who made the swaying block entity swing
	 */
	public void activate(Direction direction, Entity entity) {
		this.collisions.add(entity);
		this.colliding = true;

		var pos = this.getBlockPos();
		var world = this.getLevel();
		world.blockEvent(pos, this.getBlockState().getBlock(), 2, 1);
		world.updateNeighbourForOutputSignal(pos, this.getBlockState().getBlock());

		this.activate(direction);
	}

	/**
	 * Swings the swaying block entity in a given direction. Caused by an entity collision.
	 *
	 * @param entity the entity who made the swaying block entity swing
	 */
	public void activate(Entity entity) {
		var pos = this.getBlockPos();
		double selfX = pos.getX() + 0.5;
		double selfZ = pos.getZ() + 0.5;

		double diffX = selfX - entity.getX();
		double diffZ = selfZ - entity.getZ();

		Direction direction;
		if (Math.abs(diffX) > Math.abs(diffZ)) {
			if (diffX > 0) direction = Direction.WEST;
			else direction = Direction.EAST;
		} else {
			if (diffZ > 0) direction = Direction.NORTH;
			else direction = Direction.SOUTH;
		}

		this.activate(direction, entity);
	}

	/* Syncing */

	@Override
	public boolean triggerEvent(int type, int data) {
		if (type == 1) {
			this.swingBaseDirection = Direction.from3DDataValue(data);
			if (!this.swinging || !this.isColliding()) {
				this.swingTicks = 0;
			}
			this.swinging = true;
			return true;
		} else if (type == 2) {
			this.colliding = data != 0;
			return true;
		} else {
			return super.triggerEvent(type, data);
		}
	}

	/* Ticking */

	private void tick() {
		++this.swingTicks;

		if (this.swingTicks >= 4 && this.isColliding()) {
			this.swingTicks = 4;
		}

		if (this.swingTicks >= this.getMaxSwingTicks()) {
			this.swinging = false;
			this.swingTicks = 0;
		}
	}

	protected void tickClient(Level world) {
		this.naturalSway = world.getBrightness(LightLayer.SKY, this.worldPosition) >= 12;
		this.tick();
	}

	public static void clientTick(Level world, BlockPos pos, BlockState state, SwayingBlockEntity swayingBlockEntity) {
		swayingBlockEntity.tickClient(world);
	}

	public static void serverTick(Level world, BlockPos pos, BlockState state, SwayingBlockEntity swayingBlockEntity) {
		boolean canTick = true;

		if (!swayingBlockEntity.collisions.isEmpty()) {
			var it = swayingBlockEntity.collisions.iterator();

			while (it.hasNext()) {
				var entry = it.next();

				if (entry.isRemoved())
					it.remove();
				else {
					if (swayingBlockEntity.getCollisionBox().intersects(entry.getBoundingBox())) {
						canTick = false;
					} else {
						it.remove();
					}
				}
			}
		}
		if (swayingBlockEntity.collisions.isEmpty() && swayingBlockEntity.isColliding()) {
			swayingBlockEntity.colliding = false;
			world.blockEvent(pos, state.getBlock(), 2, 0);
			world.updateNeighbourForOutputSignal(pos, state.getBlock());
		}

		if (canTick) {
			int oldSwingTicks = swayingBlockEntity.swingTicks;
			swayingBlockEntity.tick();
			if (oldSwingTicks != swayingBlockEntity.swingTicks) {
				world.updateNeighbourForOutputSignal(pos, state.getBlock());
			}
		}
	}
}
