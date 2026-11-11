package me.paulf.fairylights.client.renderer.block.entity.state;

import me.paulf.fairylights.client.renderer.block.entity.state.connection.ConnectionRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class FastenerRenderState {
    public final List<ConnectionRenderState> connections = new ArrayList<>();

    public boolean isFenceFastener;

    public Direction facing;
    public BlockState state;
    public int lightCoords;
    public int overlayCoords;
}
