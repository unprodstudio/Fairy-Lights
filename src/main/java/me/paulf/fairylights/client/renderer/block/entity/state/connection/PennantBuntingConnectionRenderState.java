package me.paulf.fairylights.client.renderer.block.entity.state.connection;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PennantBuntingConnectionRenderState extends ConnectionRenderState {
    public final List<PennantRenderState> features = new ArrayList<>();
    public Component text;

    public static class PennantRenderState {
        public final BlockModelRenderState model = new BlockModelRenderState();
        public int color;
        public Vec3 point;

        public float yaw;
        public float pitch;
        public float roll;
    }
}
