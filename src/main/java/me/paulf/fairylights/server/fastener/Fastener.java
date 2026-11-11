package me.paulf.fairylights.server.fastener;

import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.ConnectionType;
import me.paulf.fairylights.server.fastener.accessor.FastenerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface Fastener<F extends FastenerAccessor> extends Component {
    void serializeNBT(ValueOutput output);
    void deserializeNBT(ValueInput input);

    @Override
    default void readData(ValueInput readView) {
        this.deserializeNBT(readView.childOrEmpty("data"));
    }

    @Override
    default void writeData(ValueOutput writeView) {
        this.serializeNBT(writeView.child("data"));
    }

    Optional<Connection> get(final UUID id);

    List<Connection> getOwnConnections();

    List<Connection> getAllConnections();

    default Optional<Connection> getFirstConnection() {
        return this.getAllConnections().stream().findFirst();
    }

    AABB getBounds();

    Vec3 getConnectionPoint();

    BlockPos getPos();

    Direction getFacing();

    void setWorld(Level world);

    @Nullable
    Level getWorld();

    F createAccessor();

    boolean isMoving();

    default void resistSnap(final Vec3 from) {}

    boolean update();

    void setDirty();

    void dropItems(Level world, BlockPos pos);

    void remove();

    boolean hasNoConnections();

    boolean hasConnectionWith(Fastener<?> fastener);

    @Nullable
    Connection getConnectionTo(FastenerAccessor destination);

    boolean removeConnection(UUID uuid);

    boolean removeConnection(Connection connection);

    boolean reconnect(final Level world, Connection connection, Fastener<?> newDestination);

    Connection connect(Level world, Fastener<?> destination, ConnectionType<?> type, DataComponentMap components, final boolean drop);

    Connection createOutgoingConnection(Level world, UUID uuid, Fastener<?> destination, ConnectionType<?> type, DataComponentMap components, final boolean drop);

    void createIncomingConnection(Level world, UUID uuid, Fastener<?> destination, ConnectionType<?> type);
}
