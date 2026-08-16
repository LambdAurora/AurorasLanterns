/*
 * Copyright © 2025 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Lanterns.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.auroraslanterns.client.mixin;

import dev.lambdaurora.auroraslanterns.block.entity.ChandelierBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(method = "lambda$handleBlockEntityData$0", at = @At("RETURN"))
	private void auroraslanterns$handleBlockEntityData$handleChandelier(
			ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket, BlockEntity blockEntity,
			CallbackInfo ci
	) {
		if (blockEntity instanceof ChandelierBlockEntity chandelier) {
			var pos = chandelier.getBlockPos();
			Minecraft.getInstance().levelRenderer.setSectionDirty(
					SectionPos.blockToSectionCoord(pos.getX()),
					SectionPos.blockToSectionCoord(pos.getY()),
					SectionPos.blockToSectionCoord(pos.getZ())
			);
		}
	}
}
