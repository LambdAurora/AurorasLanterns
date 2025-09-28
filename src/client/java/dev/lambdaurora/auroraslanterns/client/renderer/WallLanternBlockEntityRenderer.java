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
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class WallLanternBlockEntityRenderer
		implements BlockEntityRenderer<WallLanternBlockEntity, WallLanternBlockEntityRenderState> {
	public WallLanternBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public int getViewDistance() {
		return 128;
	}

	@Override
	public @NotNull WallLanternBlockEntityRenderState createRenderState() {
		return new WallLanternBlockEntityRenderState();
	}

	@Override
	public void extractRenderState(
			WallLanternBlockEntity lantern, WallLanternBlockEntityRenderState state,
			float tickDelta, Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
	) {
		BlockEntityRenderer.super.extractRenderState(lantern, state, tickDelta, cameraPos, crumblingOverlay);

		float pitch = 0.f;
		float roll = 0.f;
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

		state.lanternState = lantern.getLanternState();
		state.pitch = pitch;
		state.roll = roll;

		var lanternShape = lantern.getLanternState().getShape(lantern.getLevel(), lantern.getBlockPos());
		var lanternShapeMaxY = lanternShape.max(Direction.Axis.Y);
		var lanternShapeMinY = lanternShape.min(Direction.Axis.Y);
		state.size = lanternShapeMaxY - lanternShapeMinY;
	}

	@Override
	public void submit(
			WallLanternBlockEntityRenderState lantern, MatrixStack matrices,
			SubmitNodeCollector collector, CameraRenderState cameraRenderState
	) {
		var pos = lantern.blockPos;

		float pitch = lantern.pitch;
		float roll = lantern.roll;

		var lanternState = lantern.lanternState;
		matrices.push();

		matrices.translate(8.f / 16.f, 12.f / 16.f, 8.f / 16.f);
		if (roll != 0.f)
			matrices.rotate(Axis.ZP.rotation(roll));
		if (pitch != 0.f)
			matrices.rotate(Axis.XP.rotation(pitch));

		var facing = lantern.blockState.get(WallLanternBlock.FACING);
		int lanternRotation = switch (facing) {
			case NORTH -> 90;
			case EAST -> 180;
			case SOUTH -> 270;
			default -> 0;
		};

		int extension = lantern.blockState.get(WallLanternBlock.EXTENSION).getOffset();
		matrices.translate(
				(-facing.getStepX() * extension) / 16.f,
				0.f,
				(-facing.getStepZ() * extension) / 16.f
		);

		matrices.rotate(Axis.YN.rotationDegrees(lanternRotation));

		matrices.translate(-8.f / 16.f, -1.f / 16.f - lantern.size, -8.f / 16.f);

		LBGHooks.pushDisableBetterLayer();
		collector.submitBlock(matrices, lanternState, lantern.lightCoords, OverlayTexture.NO_OVERLAY, 0);
		LBGHooks.popDisableBetterLayer();
		matrices.pop();
	}
}
