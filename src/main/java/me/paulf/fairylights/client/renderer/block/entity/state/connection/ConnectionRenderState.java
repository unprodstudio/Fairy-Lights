package me.paulf.fairylights.client.renderer.block.entity.state.connection;

import me.paulf.fairylights.util.Curve;

public abstract class ConnectionRenderState {
    public Curve catenary;
    public int wireColor;

    public int lightCoords;
    public int overlayCoords;
}
