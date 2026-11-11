package me.paulf.fairylights.client.renderer.block.entity.state.connection;

import me.paulf.fairylights.client.renderer.block.entity.state.LightRenderState;

import java.util.ArrayList;
import java.util.List;

public class HangingLightsConnectionRenderState extends ConnectionRenderState {
    public final List<LightRenderState> features = new ArrayList<>();
}
