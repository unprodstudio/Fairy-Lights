package me.paulf.fairylights.server.fastener;

import com.google.common.collect.ImmutableList;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.ConnectionType;
import me.paulf.fairylights.server.fastener.accessor.FastenerAccessor;
import me.paulf.fairylights.util.AABBBuilder;
import me.paulf.fairylights.util.Curve;
import me.paulf.fairylights.util.RegistryObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.Map.Entry;

public abstract class AbstractFastener<F extends FastenerAccessor> implements Fastener<F> {
    public static final AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    private final Map<UUID, Connection> outgoing = new HashMap<>();

    private final Map<UUID, Incoming> incoming = new HashMap<>();

    protected AABB bounds = INFINITE_EXTENT_AABB;

    @Nullable
    private Level world;

    private boolean dirty;

    @Override
    public Optional<Connection> get(final UUID id) {
        return Optional.ofNullable(this.outgoing.get(id));
    }

    @Override
    public List<Connection> getOwnConnections() {
        return ImmutableList.copyOf(this.outgoing.values());
    }

    @Override
    public List<Connection> getAllConnections() {
        final ImmutableList.Builder<Connection> list = new ImmutableList.Builder<>();
        list.addAll(this.outgoing.values());
        if (this.world != null) {
            this.incoming.values().forEach(i -> i.get(this.world).ifPresent(list::add));
        }
        return list.build();
    }

    @Override
    public AABB getBounds() {
        return this.bounds;
    }

    @Override
    public abstract BlockPos getPos();

    @Override
    public void setWorld(final Level world) {
        this.world = world;
        this.outgoing.values().forEach(c -> c.setWorld(world));
    }

    @Nullable
    @Override
    public Level getWorld() {
        return this.world;
    }

    @Override
    public boolean update() {
        final Iterator<Connection> it = this.outgoing.values().iterator();
        final Vec3 fromOffset = this.getConnectionPoint();
        boolean dirty = this.dirty;
        this.dirty = false;
        while (it.hasNext()) {
            final Connection connection = it.next();
            if (connection.update(fromOffset)) {
                dirty = true;
            }
            if (connection.isRemoved()) {
                dirty = true;
                it.remove();
                this.incoming.remove(connection.getUUID());
                if (this.world != null) {
                    this.drop(this.world, this.getPos(), connection);
                }
            }
        }
        if (this.world != null) {
            this.incoming.values().removeIf(incoming -> incoming.gone(this.world));
        }
        if (dirty) {
            this.calculateBoundingBox();
        }
        return dirty;
    }

    @Override
    public void setDirty() {
        this.dirty = true;
    }

    protected void calculateBoundingBox() {
        if (this.outgoing.isEmpty()) {
            this.bounds = new AABB(this.getPos());
            return;
        }
        final AABBBuilder builder = new AABBBuilder();
        for (final Connection connection : this.outgoing.values()) {
            final Curve catenary = connection.getCatenary();
            if (catenary == null) {
                continue;
            }
            final Curve.SegmentIterator it = catenary.iterator();
            while (it.next()) {
                builder.include(it.getX(0.0F), it.getY(0.0F), it.getZ(0.0F));
                if (!it.hasNext()) {
                    builder.include(it.getX(1.0F), it.getY(1.0F), it.getZ(1.0F));
                }
            }
        }
        this.bounds = builder.add(this.getConnectionPoint()).build();
    }

    @Override
    public void dropItems(final Level world, final BlockPos pos) {
        for (final Connection connection : this.getAllConnections()) {
            this.drop(world, pos, connection);
        }
    }

    private void drop(final Level world, final BlockPos pos, final Connection connection) {
        if (!connection.shouldDrop()) return;
        final float offsetX = world.getRandom().nextFloat() * 0.8F + 0.1F;
        final float offsetY = world.getRandom().nextFloat() * 0.8F + 0.1F;
        final float offsetZ = world.getRandom().nextFloat() * 0.8F + 0.1F;
        final ItemStack stack = connection.getItemStack();
        final ItemEntity entityItem = new ItemEntity(world, pos.getX() + offsetX, pos.getY() + offsetY, pos.getZ() + offsetZ, stack);
        final float scale = 0.05F;
        entityItem.setDeltaMovement(
            world.getRandom().nextGaussian() * scale,
            world.getRandom().nextGaussian() * scale + 0.2F,
            world.getRandom().nextGaussian() * scale
        );
        world.addFreshEntity(entityItem);
        connection.noDrop();
    }

    @Override
    public void remove() {
        this.outgoing.values().forEach(Connection::remove);
    }

    @Override
    public boolean hasNoConnections() {
        return this.outgoing.isEmpty() && this.incoming.isEmpty();
    }

    @Override
    public boolean hasConnectionWith(final Fastener<?> fastener) {
        return this.getConnectionTo(fastener.createAccessor()) != null;
    }

    @Nullable
    @Override
    public Connection getConnectionTo(final FastenerAccessor destination) {
        for (final Connection connection : this.outgoing.values()) {
            if (connection.isDestination(destination)) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public boolean removeConnection(final UUID uuid) {
        final Connection connection = this.outgoing.remove(uuid);
        if (connection != null) {
            connection.remove();
            this.setDirty();
            return true;
        } else if (this.incoming.remove(uuid) != null) {
            this.setDirty();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeConnection(final Connection connection) {
        return this.removeConnection(connection.getUUID());
    }

    @Override
    public boolean reconnect(final Level world, final Connection connection, final Fastener<?> newDestination) {
        if (this.equals(newDestination) || newDestination.hasConnectionWith(this)) {
            return false;
        }
        final UUID uuid = connection.getUUID();
        if (connection.getDestination().get(world, false).filter(t -> {
            t.removeConnection(uuid);
            return true;
        }).isPresent()) {
            connection.setDestination(newDestination);
            connection.setDrop();
            newDestination.createIncomingConnection(this.world, uuid, this, connection.getType());
            this.setDirty();
            return true;
        }
        return false;
    }

    @Override
    public Connection connect(final Level world, final Fastener<?> destination, final ConnectionType<?> type, final DataComponentMap components, final boolean drop) {
        final UUID uuid = Mth.createInsecureUUID(world.getRandom());
        final Connection connection = this.createOutgoingConnection(world, uuid, destination, type, components, drop);
        destination.createIncomingConnection(world, uuid, this, type);
        return connection;
    }

    @Override
    public Connection createOutgoingConnection(final Level world, final UUID uuid, final Fastener<?> destination, final ConnectionType<?> type, final DataComponentMap components, final boolean drop) {
        final Connection c = type.create(world, this, uuid);
        c.deserialize(destination, components, drop);
        this.outgoing.put(uuid, c);
        this.setDirty();
        return c;
    }

    @Override
    public void createIncomingConnection(final Level world, final UUID uuid, final Fastener<?> destination, final ConnectionType<?> type) {
        this.incoming.put(uuid, new Incoming(destination.createAccessor(), uuid));
        this.setDirty();
    }

    @Override
    public void serializeNBT(ValueOutput output) {
        final var outgoing = output.childrenList("outgoing");
        for (final Entry<UUID, Connection> connectionEntry : this.outgoing.entrySet()) {
            final UUID uuid = connectionEntry.getKey();
            final Connection connection = connectionEntry.getValue();
            final var connectionCompound = outgoing.addChild();
            connection.serialize(output.child("connection"));
            connectionCompound.store("type", FairyLights.CONNECTION_TYPES.byNameCodec(), connection.getType());
            connectionCompound.store("uuid", UUIDUtil.CODEC, uuid);
        }
        final var incoming = output.childrenList("incoming");
        for (final Entry<UUID, Incoming> e : this.incoming.entrySet()) {
            final var tag = incoming.addChild();
            tag.store("uuid", UUIDUtil.CODEC, e.getKey());
            tag.store("fastener", FastenerType.CODEC, e.getValue().fastener);
        }
    }

    @Override
    public void deserializeNBT(ValueInput input) {
        final var listConnections = input.childrenListOrEmpty("outgoing");
        final List<UUID> nbtUUIDs = new ArrayList<>();
        for (ValueInput connectionCompound : listConnections) {
            final UUID uuid = connectionCompound.read("uuid", UUIDUtil.CODEC)
                .orElseGet(() -> Mth.createInsecureUUID(RandomSource.create()));
            nbtUUIDs.add(uuid);
            if (this.outgoing.containsKey(uuid)) {
                final Connection connection = this.outgoing.get(uuid);
                connection.deserialize(connectionCompound.childOrEmpty("connection"));
            } else {
                final ConnectionType<?> type = connectionCompound.read("type", FairyLights.CONNECTION_TYPES.byNameCodec()).orElse(null);
                if (type != null) {
                    final Connection connection = type.create(this.world, this, uuid);
                    connection.deserialize(connectionCompound.childOrEmpty("connection"));
                    this.outgoing.put(uuid, connection);
                }
            }
        }
        final Iterator<Entry<UUID, Connection>> connectionsIter = this.outgoing.entrySet().iterator();
        while (connectionsIter.hasNext()) {
            final Entry<UUID, Connection> connection = connectionsIter.next();
            if (!nbtUUIDs.contains(connection.getKey())) {
                connectionsIter.remove();
                connection.getValue().remove();
            }
        }
        this.incoming.clear();
        final var incoming = input.childrenListOrEmpty("incoming");
        for (ValueInput incomingNbt : incoming) {
            final UUID uuid = incomingNbt.read("uuid", UUIDUtil.CODEC).orElseThrow();
            final FastenerAccessor fastener = incomingNbt.read("fastener", FastenerType.CODEC).orElseThrow();
            this.incoming.put(uuid, new Incoming(fastener, uuid));
        }
        this.setDirty();
    }

    static class Incoming {
        final FastenerAccessor fastener;

        final UUID id;

        Incoming(final FastenerAccessor fastener, final UUID id) {
            this.fastener = fastener;
            this.id = id;
        }

        boolean gone(final Level world) {
            return this.fastener.isGone(world);
        }

        Optional<Connection> get(final Level world) {
            return this.fastener.get(world, false).map(Optional::of).orElse(Optional.empty()).flatMap(f -> f.get(this.id));
        }
    }
}
