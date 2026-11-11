package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class ForwardingVertexConsumer implements VertexConsumer {
    protected abstract VertexConsumer delegate();

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return delegate().addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return delegate().setColor(r, g, b, a);
    }

    @Override
    public VertexConsumer setColor(int color) {
        return delegate().setColor(color);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return delegate().setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return delegate().setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return delegate().setUv2(u, v);
    }

    @Override
    public VertexConsumer setUv3(float u, float v) {
        return delegate().setUv3(u, v);
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        return delegate().setNormal(normalX, normalY, normalZ);
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        return delegate().setLineWidth(width);
    }

}
