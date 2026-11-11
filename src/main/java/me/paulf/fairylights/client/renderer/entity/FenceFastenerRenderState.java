package me.paulf.fairylights.client.renderer.entity;

import me.paulf.fairylights.client.renderer.block.entity.state.FastenerRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class FenceFastenerRenderState extends EntityRenderState {
    public final BlockModelRenderState model = new BlockModelRenderState();
    public FastenerRenderState fastener;
}
