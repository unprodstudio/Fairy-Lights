package me.paulf.fairylights.server.net.serverbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.Lettered;
import me.paulf.fairylights.server.net.ConnectionInfo;
import me.paulf.fairylights.server.net.ConnectionMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class EditLetteredConnectionMessage<C extends Connection & Lettered> extends ConnectionMessage {
    public static final Type<EditLetteredConnectionMessage<?>> TYPE = new Type(FairyLights.id("edit_lettered"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, EditLetteredConnectionMessage<?>> CODEC = StreamCodec.composite(
            ConnectionInfo.STREAM_CODEC, ConnectionMessage::getData,
            ComponentSerialization.STREAM_CODEC, msg -> msg.text,
            EditLetteredConnectionMessage::new
    );

    private Component text;

    public EditLetteredConnectionMessage(ConnectionInfo data, Component text) {
        super(data);
        this.text = text;
    }

    public EditLetteredConnectionMessage(final C connection, final Component text) {
        super(connection);
        this.text = text;
    }

    public static final class Handler implements ServerPlayNetworking.PlayPayloadHandler<EditLetteredConnectionMessage<?>> {
        @Override
        public void receive(final EditLetteredConnectionMessage<?> message, final ServerPlayNetworking.Context context) {
            final ServerPlayer player = context.player();
            this.accept(message, player);
        }

        private <C extends Connection & Lettered> void accept(final EditLetteredConnectionMessage<C> message, final ServerPlayer player) {
            if (player != null) {
                ConnectionMessage.<C>getConnection(message, c -> c instanceof Lettered, player.level()).ifPresent(connection -> {
                    if (connection.isModifiable(player) && connection.isSupportedText(message.text)) {
                        connection.setText(message.text);
                    }
                });
            }
        }
    }
}
