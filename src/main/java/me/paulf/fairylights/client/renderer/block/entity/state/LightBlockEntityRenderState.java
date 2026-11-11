package me.paulf.fairylights.client.renderer.block.entity.state;

import me.paulf.fairylights.client.model.light.LightModel;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;

public class LightBlockEntityRenderState extends BlockEntityRenderState {
    public AttachFace face;
    public float rotation;
    public boolean isOrientable;
    public LightRenderState light;
    public LightModel<?> model;
}
