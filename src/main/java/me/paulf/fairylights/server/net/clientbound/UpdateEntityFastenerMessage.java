package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.net.Message;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;

public final class UpdateEntityFastenerMessage implements Message {
    public static final Type<UpdateEntityFastenerMessage> TYPE = new Type(FairyLights.id("update_entity_fastener"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateEntityFastenerMessage> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, msg -> msg.entityId,
            ByteBufCodecs.COMPOUND_TAG, msg -> msg.compound,
            UpdateEntityFastenerMessage::new
    );

    private int entityId;

    private CompoundTag compound;

    public UpdateEntityFastenerMessage(int id, CompoundTag tag) {
        this.entityId = id;
        this.compound = tag;
    }

    public UpdateEntityFastenerMessage(final Entity entity, final CompoundTag compound) {
        this.entityId = entity.getId();
        this.compound = compound;
    }

    public static final class Handler implements ClientPlayNetworking.PlayPayloadHandler<UpdateEntityFastenerMessage> {
        @Override
        public void receive(final UpdateEntityFastenerMessage message, final ClientPlayNetworking.Context context) {
            final Entity entity = context.client().level.getEntity(message.entityId);
            if (entity != null) {
                CapabilityHandler.FASTENER_CAP.maybeGet(entity).ifPresent(f -> f.deserializeNBT(TagValueInput.create(ProblemReporter.DISCARDING, context.player().registryAccess(), message.compound)));
            }
        }
    }
}
