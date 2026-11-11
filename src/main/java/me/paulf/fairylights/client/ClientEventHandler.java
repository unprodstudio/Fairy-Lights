package me.paulf.fairylights.client;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.paulf.fairylights.server.collision.Collidable;
import me.paulf.fairylights.server.collision.Intersection;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.connection.PlayerAction;
import me.paulf.fairylights.server.entity.FenceFastenerEntity;
import me.paulf.fairylights.server.fastener.CollectFastenersEvent;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FastenerType;
import me.paulf.fairylights.server.jingle.Jingle;
import me.paulf.fairylights.util.Curve;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

public final class ClientEventHandler {
    private static final float HIGHLIGHT_ALPHA = 0.4F;

    public ClientEventHandler() {
        LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((context, result) -> {
            if (result instanceof EntityHitResult entityHitResult) {
                extractBlockHighlight(context, entityHitResult);
            }
        });

        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, outlineRenderState) -> {
            var state = context.levelState();

            var pos = state.getDataOrDefault(POSITION_KEY, Vec3.ZERO);
            var outline = state.getData(OUTLINE_KEY);
            var curveData = state.getData(CURVE_KEY);

            if (outline != null) {
                context.poseStack().pushPose();
                context.poseStack().translate(pos.x(), pos.y(), pos.z());
                context.submitNodeCollector().submitShapeOutline(context.poseStack(), Shapes.create(outline), RenderTypes.LINES, ARGB.colorFromFloat(HIGHLIGHT_ALPHA, 0f, 0f, 0f), context.gameRenderer().gameRenderState().windowRenderState.appropriateLineWidth, true);
                context.poseStack().popPose();

                return false;
            } else if (curveData != null) {
                context.poseStack().pushPose();
                context.poseStack().translate(pos.x(), pos.y(), pos.z());
                context.submitNodeCollector().submitCustomGeometry(context.poseStack(), RenderTypes.LINES, (pose, buffer) -> {
                    submitHighlight(curveData, pose, buffer);
                });
                context.poseStack().popPose();

                return false;
            }

            return true;
        });
    }

    @Nullable
    public static Connection getHitConnection() {
        final net.minecraft.world.phys.HitResult result = Minecraft.getInstance().hitResult;
        if (result instanceof EntityHitResult) {
            final Entity entity = ((EntityHitResult) result).getEntity();
            if (entity instanceof HitConnection) {
                return ((HitConnection) entity).result.connection;
            }
        }
        return null;
    }

    public void renderOverlay(final Gui gui, final GuiGraphicsExtractor poseStack, final float partialTick, final int screenWidth, final int screenHeight) {
        final Connection conn = getHitConnection();
        if (!(conn instanceof HangingLightsConnection)) {
            return;
        }
        final Jingle jingle = ((HangingLightsConnection) conn).getPlayingJingle();
        if (jingle == null) {
            return;
        }
        final List<String> lines = List.of(
            "Song: " + jingle.getTitle(),
            "Artist: " + jingle.getArtist());
        for (int i = 0; i < lines.size(); i++) {
            final String line = lines.get(i);
            if (!Strings.isNullOrEmpty(line)) {
                Font font = Minecraft.getInstance().font;
                final int lineHeight = font.lineHeight;
                final int textWidth = font.width(line);
                final int y = 2 + lineHeight * i;
                poseStack.fill(1, y - 1, 2 + textWidth + 1, y + lineHeight - 1, 0x90505050);
                poseStack.text(font, line, 2, y, 0xFFe0e0e0);
            }
        }
    }

    public static void updateHitConnection() {
        final Minecraft mc = Minecraft.getInstance();
        final Entity viewer = mc.getCameraEntity();
        if (mc.hitResult != null && mc.level != null && viewer != null) {
            final HitResult result = getHitConnection(mc.level, viewer);
            if (result != null) {
                final Vec3 eyes = viewer.getEyePosition(1.0F);
                if (result.intersection.getResult().distanceTo(eyes) < mc.hitResult.getLocation().distanceTo(eyes)) {
                    mc.hitResult = new EntityHitResult(new HitConnection(mc.level, result));
                    mc.crosshairPickEntity = null;
                }
            }
        }
    }

    @Nullable
    private static HitResult getHitConnection(final Level world, final Entity viewer) {
        final AABB bounds = new AABB(viewer.blockPosition()).inflate(Connection.MAX_LENGTH + 1.0D);
        final Set<Fastener<?>> fasteners = collectFasteners(world, bounds);
        return getHitConnection(viewer, bounds, fasteners);
    }

    private static Set<Fastener<?>> collectFasteners(final Level world, final AABB bounds) {
        final Set<Fastener<?>> fasteners = Sets.newLinkedHashSet();
        final CollectFastenersEvent event = new CollectFastenersEvent(world, bounds, fasteners);
        world.getEntitiesOfClass(FenceFastenerEntity.class, bounds)
            .forEach(event::accept);
        final int minX = Mth.floor(bounds.minX / 16.0D);
        final int maxX = Mth.ceil(bounds.maxX / 16.0D);
        final int minZ = Mth.floor(bounds.minZ / 16.0D);
        final int maxZ = Mth.ceil(bounds.maxZ / 16.0D);
        final ChunkSource provider = world.getChunkSource();
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                final LevelChunk chunk = provider.getChunk(x, z, false);
                if (chunk != null) {
                    event.accept(chunk);
                }
            }
        }
        CollectFastenersEvent.EVENT.invoker().onCollectFasteners(event);
        return fasteners;
    }

    @Nullable
    private static HitResult getHitConnection(final Entity viewer, final AABB bounds, final Set<Fastener<?>> fasteners) {
        if (fasteners.isEmpty()) {
            return null;
        }
        final Vec3 origin = viewer.getEyePosition(1);
        final Vec3 look = viewer.getLookAngle();
        final double reach = Minecraft.getInstance().player.getPickRadius();
        final Vec3 end = origin.add(look.x * reach, look.y * reach, look.z * reach);
        Connection found = null;
        Intersection rayTrace = null;
        double distance = Double.MAX_VALUE;
        for (final Fastener<?> fastener : fasteners) {
            for (final Connection connection : fastener.getOwnConnections()) {
                if (connection.getDestination().getType() == FastenerType.PLAYER) {
                    continue;
                }
                final Collidable collision = connection.getCollision();
                final Intersection result = collision.intersect(origin, end);
                if (result != null) {
                    final double dist = result.getResult().distanceTo(origin);
                    if (dist < distance) {
                        distance = dist;
                        found = connection;
                        rayTrace = result;
                    }
                }
            }
        }
        if (found == null) {
            return null;
        }
        return new HitResult(found, rayTrace);
    }

    private static final RenderStateDataKey<AABB> OUTLINE_KEY = RenderStateDataKey.create();
    private static final RenderStateDataKey<Vec3> POSITION_KEY = RenderStateDataKey.create();
    private static final RenderStateDataKey<CurveData> CURVE_KEY = RenderStateDataKey.create();

    private record CurveData(float radius, Curve curve) {}

    public void extractBlockHighlight(LevelExtractionContext renderContext, EntityHitResult hitResult) {
        final Entity entity = hitResult.getEntity();
        final Vec3 pos = renderContext.camera().position();
        if (entity instanceof FenceFastenerEntity fence) {
            final Player player = Minecraft.getInstance().player;
            // Check if the server will allow interaction
            if (player != null && (player.hasLineOfSight(fence) || player.distanceToSqr(fence) <= 9.0D)) {
                final AABB selection = fence.getBoundingBox().move(-pos.x, -pos.y, -pos.z).inflate(0.002D);
                renderContext.levelState().setData(POSITION_KEY, pos);
                renderContext.levelState().setData(OUTLINE_KEY, selection);
            }
        } else if (entity instanceof final HitConnection hit) {
            if (hit.result.intersection.getFeatureType() == Connection.CORD_FEATURE) {
                final Curve cat = hit.result.connection.getCatenary();
                if (cat == null) {
                    return;
                }

                final Vec3 p = hit.result.connection.getFastener().getConnectionPoint();
                renderContext.levelState().setData(POSITION_KEY, p);
                renderContext.levelState().setData(CURVE_KEY, new CurveData(hit.result.connection.getRadius(), cat));
            } else {
                final AABB bb = hit.result.intersection.getHitBox().move(-pos.x, -pos.y, -pos.z).inflate(0.002D);
                renderContext.levelState().setData(POSITION_KEY, pos);
                renderContext.levelState().setData(OUTLINE_KEY, bb);
            }
        }
    }

    private void submitHighlight(final CurveData data, final PoseStack.Pose matrix, final VertexConsumer buf) {
        final Curve cat = data.curve();

        final Vector3f p = new Vector3f();
        final Vector3f v1 = new Vector3f();
        final Vector3f v2 = new Vector3f();
        final LineBuilder builder = new LineBuilder(matrix, buf);
        final float r = data.radius() + 0.01F;
        for (int edge = 0; edge < 4; edge++) {
            p.set(cat.getX(0), cat.getY(0), cat.getZ(0));
            v1.set(cat.getDx(0), cat.getDy(0), cat.getDz(0));
            v1.normalize();
            v2.set(-v1.x(), -v1.y(), -v1.z());
            for (int n = 0; edge == 0 && n < 8; n++) {
                this.addVertex(builder, (n + 1) / 2 % 4, p, v1, v2, r);
            }
            this.addVertex(builder, edge, p, v1, v2, r);
            for (int i = 1; i < cat.getCount() - 1; i++) {
                p.set(cat.getX(i), cat.getY(i), cat.getZ(i));
                v2.set(-cat.getDx(i), -cat.getDy(i), -cat.getDz(i));
                v2.normalize();
                this.addVertex(builder, edge, p, v1, v2, r);
                this.addVertex(builder, edge, p, v1, v2, r);
                v1.set(-v2.x(), -v2.y(), -v2.z());
            }
            p.set(cat.getX(), cat.getY(), cat.getZ());
            v2.set(-v1.x(), -v1.y(), -v1.z());
            this.addVertex(builder, edge, p, v1, v2, r);
            for (int n = 0; edge == 0 && n < 8; n++) {
                this.addVertex(builder, (n + 1) / 2 % 4, p, v1, v2, r);
            }
        }
    }

    static class LineBuilder {
        final PoseStack.Pose matrix;
        final VertexConsumer buf;
        Vector3f last;

        LineBuilder(PoseStack.Pose matrix, VertexConsumer buf) {
            this.matrix = matrix;
            this.buf = buf;
        }

        void accept(Vector3f pos) {
            if (this.last == null) {
                this.last = pos;
            } else {
                Vector3f n = new Vector3f(pos);
                n.sub(this.last);
                n.normalize();
                n = this.matrix.normal().transform(n);
                this.buf.addVertex(this.matrix.pose(), this.last.x(), this.last.y(), this.last.z())
                    .setColor(0.0F, 0.0F, 0.0F, HIGHLIGHT_ALPHA)
                    .setNormal(n.x(), n.y(), n.z());
                this.buf.addVertex(this.matrix.pose(), pos.x(), pos.y(), pos.z())
                    .setColor(0.0F, 0.0F, 0.0F, HIGHLIGHT_ALPHA)
                    .setNormal(n.x(), n.y(), n.z());
                this.last = null;
            }
        }
    }

    private void addVertex(final LineBuilder builder, final int edge, final Vector3f p, final Vector3f v1, final Vector3f v2, final float r) {
        builder.accept(this.get(edge, p, v1, v2, r));
    }

    private Vector3f get(final int edge, final Vector3f p, final Vector3f v1, final Vector3f v2, final float r) {
        final Vector3f up = new Vector3f();
        final Vector3f side = new Vector3f();
        // if collinear
        if (v1.dot(v2) < -(1.0F - 1.0e-2F)) {
            final float h = Mth.sqrt(v1.x() * v1.x() + v1.z() * v1.z());
            // if vertical
            if (h < 1.0e-2F) {
                up.set(-1.0F, 0.0F, 0.0F);
            } else {
                up.set(-v1.x() / h * -v1.y(), -h, -v1.z() / h * -v1.y());
            }
        } else {
            up.set(v2.x(), v2.y(), v2.z());
            up.lerp(v1, 0.5F);
        }
        up.normalize();
        side.set(v1.x(), v1.y(), v1.z());
        side.cross(up);
        side.normalize();
        side.mul(edge == 0 || edge == 3 ? -r : r);
        up.mul(edge < 2 ? -r : r);
        up.add(side);
        up.add(p);
        return up;
    }

    static class HitConnection extends Entity {
        final ClientEventHandler.HitResult result;

        HitConnection(final Level world, final ClientEventHandler.HitResult result) {
            super(EntityTypes.ITEM, world);
            this.setId(-1);
            this.result = result;
            this.setPos(result.intersection.getResult());
        }

        @Override
        public boolean hurtServer(ServerLevel level, final DamageSource source, final float amount) {
            if (source.getEntity() == Minecraft.getInstance().player) {
                this.processAction(PlayerAction.ATTACK);
                return true;
            }
            return false;
        }

        @Override
        public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
            if (player == Minecraft.getInstance().player) {
                this.processAction(PlayerAction.INTERACT);
                return InteractionResult.SUCCESS;
            }
            return super.interact(player, hand, location);
        }

        private void processAction(final PlayerAction action) {
            this.result.connection.processClientAction(Minecraft.getInstance().player, action, this.result.intersection);
        }

        @Override
        public @Nullable ItemStack getPickResult() {
            return this.result.connection.getItemStack();
        }

        @Override
        protected void defineSynchedData(SynchedEntityData.Builder builder) {
        }

        @Override
        protected void addAdditionalSaveData(ValueOutput output) {
        }

        @Override
        protected void readAdditionalSaveData(ValueInput input) {
        }

        @Override
        public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
            return new Packet<ClientGamePacketListener>() {
                @Override
                public PacketType<? extends Packet<ClientGamePacketListener>> type() {
                    return null;
                }

                @Override
                public void handle(ClientGamePacketListener handler) {

                }
            };
        }
    }

    private static final class HitResult {
        private final Connection connection;

        private final Intersection intersection;

        public HitResult(final Connection connection, final Intersection intersection) {
            this.connection = connection;
            this.intersection = intersection;
        }
    }
}
