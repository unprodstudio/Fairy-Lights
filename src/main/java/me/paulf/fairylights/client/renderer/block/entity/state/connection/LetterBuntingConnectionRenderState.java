package me.paulf.fairylights.client.renderer.block.entity.state.connection;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LetterBuntingConnectionRenderState extends ConnectionRenderState {
    public final List<LetterRenderState> letters = new ArrayList<>();

    public static class LetterRenderState {
        public final BlockModelRenderState model = new BlockModelRenderState();

        public int color;
        public Vec3 pos;
        public float yaw;
        public float pitch;
        public float roll;
    }
}
