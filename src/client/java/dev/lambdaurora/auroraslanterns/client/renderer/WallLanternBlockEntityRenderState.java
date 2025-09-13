package dev.lambdaurora.auroraslanterns.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.BlockState;

public class WallLanternBlockEntityRenderState extends BlockEntityRenderState {
	public BlockState lanternState;
	public float pitch;
	public float roll;
	public double size;
}
