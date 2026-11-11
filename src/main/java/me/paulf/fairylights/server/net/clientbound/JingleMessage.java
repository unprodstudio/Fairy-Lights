package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.jingle.Jingle;
import me.paulf.fairylights.server.net.ConnectionInfo;
import me.paulf.fairylights.server.net.ConnectionMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public final class JingleMessage extends ConnectionMessage implements CustomPacketPayload {
    public static final Type<JingleMessage> TYPE = new Type(FairyLights.id("jingle"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, JingleMessage> CODEC = StreamCodec.composite(
            ConnectionInfo.STREAM_CODEC, ConnectionMessage::getData,
            ByteBufCodecs.VAR_INT, msg -> msg.lightOffset,
            Jingle.STREAM_CODEC, msg -> msg.jingle,
            JingleMessage::new
    );

    private int lightOffset;

    public Jingle jingle;

    public JingleMessage(ConnectionInfo data, int lightOffset, Jingle jingle) {
        super(data);
        this.lightOffset = lightOffset;
        this.jingle = jingle;
    }

    public JingleMessage(final HangingLightsConnection connection, final int lightOffset, final Jingle jingle) {
        super(connection);
        this.lightOffset = lightOffset;
        this.jingle = jingle;
    }

    public static class Handler implements ClientPlayNetworking.PlayPayloadHandler<JingleMessage> {
        @Override
        public void receive(final JingleMessage message, final ClientPlayNetworking.Context context) {
            final Jingle jingle = message.jingle;
            if (jingle != null) {
                ConnectionMessage.<HangingLightsConnection>getConnection(message, c -> c instanceof HangingLightsConnection, Minecraft.getInstance().level).ifPresent(connection ->
                    connection.play(jingle, message.lightOffset));
            }
        }
    }
}
