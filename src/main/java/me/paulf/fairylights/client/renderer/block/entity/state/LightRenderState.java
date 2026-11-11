package me.paulf.fairylights.client.renderer.block.entity.state;

import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.item.LightVariant;
import net.minecraft.world.phys.Vec3;

public class LightRenderState {
    public boolean isPowered;
    public double descent;
    public Vec3 point;
    public float yaw;
    public float pitch;
    public float roll;
    public boolean parallelsCord;

    public LightVariant<?> variant;

    public static LightRenderState fromLight(Light<?> light, float delta) {
        var state = new LightRenderState();
        state.isPowered = light.isPowered();
        state.descent = light.getDescent();
        state.point = light.getPoint(delta);
        state.yaw = light.getYaw(delta);
        state.pitch = light.getPitch(delta);
        state.roll = light.getRoll(delta);
        state.parallelsCord = light.parallelsCord();
        state.variant = light.getVariant();

        return state;
    }
}
