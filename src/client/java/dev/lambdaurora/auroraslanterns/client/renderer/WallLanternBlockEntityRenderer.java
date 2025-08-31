/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.renderer;

import com.mojang.blaze3d.vertex.MatrixStack;
import com.mojang.math.Axis;
import dev.lambdaurora.auroraslanterns.block.WallLanternBlock;
import dev.lambdaurora.auroraslanterns.block.entity.WallLanternBlockEntity;
import dev.lambdaurora.auroraslanterns.client.utils.LBGHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WallLanternBlockEntityRenderer implements BlockEntityRenderer<WallLanternBlockEntity> {
	public WallLanternBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public int getViewDistance() {
		return 128;
	}

	@Override
	public void submit(
			WallLanternBlockEntity lantern, float tickDelta, MatrixStack matrices,
			int light, int overlay, Vec3 cameraPos,
			@Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
			SubmitNodeCollector collector
	) {
		var pos = lantern.getBlockPos();

		float pitch = 0.0F;
		float roll = 0.0F;
		float angle = MathHelper.lerp(tickDelta, lantern.prevAngle, lantern.angle);
		lantern.prevAngle = angle;
		if ((lantern.isSwinging() || lantern.isColliding()) && lantern.getSwingBaseDirection() != null) {
			switch (lantern.getSwingBaseDirection()) {
				case NORTH -> pitch = -angle;
				case SOUTH -> pitch = angle;
				case EAST -> roll = -angle;
				case WEST -> roll = angle;
			}
		} else {
			if (lantern.getCachedState().get(WallLanternBlock.FACING).getAxis() == Direction.Axis.Z) roll = angle;
			else pitch = angle;
		}

		var lanternState = lantern.getLanternState();
		matrices.push();

		matrices.translate(8.f / 16.f, 12.f / 16.f, 8.f / 16.f);
		if (roll != 0.f)
			matrices.rotate(Axis.ZP.rotation(roll));
		if (pitch != 0.f)
			matrices.rotate(Axis.XP.rotation(pitch));

		var facing = lantern.getCachedState().get(WallLanternBlock.FACING);
		int lanternRotation = switch (facing) {
			case NORTH -> 90;
			case EAST -> 180;
			case SOUTH -> 270;
			default -> 0;
		};

		int extension = lantern.getCachedState().get(WallLanternBlock.EXTENSION).getOffset();
		matrices.translate(
				(-facing.getStepX() * extension) / 16.f,
				0.f,
				(-facing.getStepZ() * extension) / 16.f
		);

		matrices.rotate(Axis.YN.rotationDegrees(lanternRotation));

		var lanternShape = lanternState.getShape(lantern.getLevel(), pos);
		var lanternShapeMaxY = lanternShape.max(Direction.Axis.Y);
		var lanternShapeMinY = lanternShape.min(Direction.Axis.Y);
		var size = lanternShapeMaxY - lanternShapeMinY;
		matrices.translate(-8.f / 16.f, -1.f / 16.f - size, -8.f / 16.f);

		LBGHooks.pushDisableBetterLayer();
		collector.submitBlock(matrices, lanternState, light, overlay, 0);
		LBGHooks.popDisableBetterLayer();
		matrices.pop();
	}
}
