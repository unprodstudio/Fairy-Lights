package me.paulf.fairylights.client.model.light;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.paulf.fairylights.client.renderer.block.entity.state.LightRenderState;
import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.feature.light.LightBehavior;
import me.paulf.fairylights.util.AABBBuilder;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public abstract class LightModel<T extends LightBehavior> extends Model<LightRenderState> {
    protected final ModelPart lit;

    protected final ModelPart litTint;

    protected final ModelPart litTintGlow;

    protected final ModelPart unlit;

    protected float brightness = 1.0F;

    protected float red = 1.0F;

    protected float green = 1.0F;

    protected float blue = 1.0F;

    @Nullable
    private AABB bounds;

    private double floorOffset = Double.NaN;

    private boolean powered;

    public LightModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
        this.lit = root.getChild("lit");
        this.litTint = root.getChild("lit_tint");
        this.litTintGlow = root.getChild("lit_tint_glow");
        this.unlit = root.getChild("unlit");
    }

    public record LightMeshHelper(EasyMeshBuilder lit, EasyMeshBuilder litTint, EasyMeshBuilder litTintGlow, EasyMeshBuilder unlit, List<EasyMeshBuilder> extra) {
        public BulbBuilder createBulb() {
            return new BulbBuilder(this.litTint(), this.litTintGlow());
        }

        public LayerDefinition build() {
            MeshDefinition def = new MeshDefinition();
            this.lit().build(def.getRoot());
            this.litTint().build(def.getRoot());
            this.litTintGlow().build(def.getRoot());
            this.unlit().build(def.getRoot());
            for (EasyMeshBuilder builder : this.extra()) {
                builder.build(def.getRoot());
            }
            return LayerDefinition.create(def, 128, 128);
        }

        public EasyMeshBuilder parented(final String name) {
            EasyMeshBuilder result = new EasyMeshBuilder(name);
            result.addChild(this.lit());
            result.addChild(this.litTint());
            result.addChild(this.litTintGlow());
            result.addChild(this.unlit());
            for (EasyMeshBuilder builder : this.extra()) {
                result.addChild(builder);
            }
            return result;
        }

        public static LightMeshHelper create() {
            EasyMeshBuilder lit = new EasyMeshBuilder("lit");
            EasyMeshBuilder litTint = new EasyMeshBuilder("lit_tint");
            EasyMeshBuilder litTintGlow = new EasyMeshBuilder("lit_tint_glow");
            EasyMeshBuilder unlit = new EasyMeshBuilder("unlit");
            return new LightMeshHelper(lit, litTint, litTintGlow, unlit, new ArrayList<>());
        }
    }

    public AABB getBounds() {
        if (this.bounds == null) {
            final PoseStack matrix = new PoseStack();
            final AABBVertexBuilder builder = new AABBVertexBuilder();
            this.renderToBuffer(matrix, builder, 0, 0, ARGB.color(255, 255, 255, 255));
            this.renderTranslucent(matrix, builder, 0, 0, ARGB.color(255, 255, 255, 255));
            this.bounds = builder.build();
        }
        return this.bounds;
    }

    public double getFloorOffset() {
        if (Double.isNaN(this.floorOffset)) {
            final AABBVertexBuilder builder = new AABBVertexBuilder();
            this.renderToBuffer(new PoseStack(), builder, 0, 0, ARGB.color(255, 255, 255, 255));
            this.floorOffset = builder.build().minY-this.getBounds().minY;
        }
        return this.floorOffset;
    }

    public void animate(final Light<?> light, final T behavior, final float delta) {
        this.powered = light.isPowered();
    }

    @Override
    public void renderToBuffer(final PoseStack matrix, final VertexConsumer builder, final int light, final int overlay, final int color) {
        this.unlit.render(matrix, builder, light, overlay, color);
        final int emissiveLight = this.getLight(light);
        this.lit.render(matrix, builder, emissiveLight, overlay, color);

        this.litTint.render(matrix, builder, emissiveLight, overlay, ARGB.multiply(color, ARGB.colorFromFloat(1f, this.red, this.green, this.blue)));
    }

    public void renderTranslucent(final PoseStack matrix, final VertexConsumer builder, final int light, final int overlay, final int color) {
        final float v = this.brightness;

        float r = ARGB.red(color) / 255f;
        float g = ARGB.green(color) / 255f;
        float b = ARGB.blue(color) / 255f;

        this.litTintGlow.render(matrix, builder, this.getLight(light), overlay, ARGB.colorFromFloat(v * 0.15F + 0.2F, r * this.red * v + (1.0F - v), g * this.green * v + (1.0F - v), b * this.blue * v + (1.0F - v)));
    }

    protected int getLight(final int packedLight) {
        return (int) Math.max((this.brightness * 15.0F * 16.0F), this.powered ? 0 : packedLight & 255) | packedLight & (255 << 16);
    }

    // http://bediyap.com/programming/convert-quaternion-to-euler-rotations/
    protected static float[] toEuler(final Quaternionf q) {
        final float r11 = 2.0F * (q.x() * q.y() + q.w() * q.z());
        final float r12 = q.w() * q.w() + q.x() * q.x() - q.y() * q.y() - q.z() * q.z();
        final float r21 = -2.0F * (q.x() * q.z() - q.w() * q.y());
        final float r31 = 2.0F * (q.y() * q.z() + q.w() * q.x());
        final float r32 = q.w() * q.w() - q.x() * q.x() - q.y() * q.y() + q.z() * q.z();
        return new float[]{
            (float) Mth.atan2(r31, r32),
            (float) Math.asin(r21),
            (float) Mth.atan2(r11, r12)
        };
    }

    static class AABBVertexBuilder implements VertexConsumer {
        final AABBBuilder builder = new AABBBuilder();

        AABB build() {
            return this.builder.build();
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.builder.include(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return this;
        }

        @Override
        public VertexConsumer setUv3(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }
    }

    static class BulbBuilder {
        EasyMeshBuilder base;
        EasyMeshBuilder glow;

        public BulbBuilder(final EasyMeshBuilder base, final EasyMeshBuilder glow) {
            this.base = base;
            this.glow = glow;
        }

        public BulbBuilder setUV(final int u, final int v) {
            this.base.setTextureOffset(u, v);
            this.glow.setTextureOffset(u, v);
            return this;
        }

        void addBox(final float x, final float y, final float z, final float width, final float height, final float depth) {
            this.addBox(x, y, z, width, height, depth, 0.0F);
        }

        void addBox(final float x, final float y, final float z, final float width, final float height, final float depth, final float expand) {
            this.addBox(x, y, z, width, height, depth, expand, 0.7F);
        }

        void addBox(final float x, final float y, final float z, final float width, final float height, final float depth, final float expand, final float glow) {
            this.base.addBox(x, y, z, width, height, depth, expand);
            this.glow.addBox(x, y, z, width, height, depth, expand + glow);
        }

        BulbBuilder createChild(final String name, final int u, final int v) {
            return this.createChild(name, u, v, EasyMeshBuilder::new);
        }

        BulbBuilder createChild(final String name, final int u, final int v, final ModelPartFactory factory) {
            final EasyMeshBuilder base = factory.create(name, u, v);
            final EasyMeshBuilder glow = factory.create(name, u, v);
            this.base.addChild(base);
            this.glow.addChild(glow);
            return new BulbBuilder(base, glow);
        }

        public void setPosition(final float x, final float y, final float z) {
            this.base.setRotationPoint(x, y, z);
            this.glow.setRotationPoint(x, y, z);
        }

        public void setAngles(final float x, final float y, final float z) {
            this.base.xRot = x;
            this.base.yRot = y;
            this.base.zRot = z;
            this.glow.xRot = x;
            this.glow.yRot = y;
            this.glow.zRot = z;
        }
    }

    interface ModelPartFactory {
        EasyMeshBuilder create(final String name, final int u, final int v);
    }
}
