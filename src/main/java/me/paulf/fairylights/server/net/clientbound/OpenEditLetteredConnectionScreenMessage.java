package me.paulf.fairylights.server.net.clientbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.Lettered;
import me.paulf.fairylights.server.net.ConnectionInfo;
import me.paulf.fairylights.server.net.ConnectionMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class OpenEditLetteredConnectionScreenMessage<C extends Connection & Lettered> extends ConnectionMessage {
    public static final Type<OpenEditLetteredConnectionScreenMessage<?>> TYPE = new Type(FairyLights.id("open_edit_lettered_screen"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEditLetteredConnectionScreenMessage<?>> CODEC = StreamCodec.composite(
            ConnectionInfo.STREAM_CODEC, OpenEditLetteredConnectionScreenMessage::getData,
            OpenEditLetteredConnectionScreenMessage::new
    );

    public OpenEditLetteredConnectionScreenMessage(ConnectionInfo data) {
        super(data);
    }

    public OpenEditLetteredConnectionScreenMessage(final C connection) {
        super(connection);
    }

    public static final class Handler implements ClientPlayNetworking.PlayPayloadHandler<OpenEditLetteredConnectionScreenMessage<?>> {
        @Override
        public void receive(final OpenEditLetteredConnectionScreenMessage<?> message, final ClientPlayNetworking.Context context) {
            this.accept(message);
        }

        private <C extends Connection & Lettered> void accept(final OpenEditLetteredConnectionScreenMessage<C> message) {
            ConnectionMessage.<C>getConnection(message, c -> c instanceof Lettered, Minecraft.getInstance().level).ifPresent(connection -> {
                Minecraft.getInstance().gui.setScreen(new EditLetteredConnectionScreen<>(connection));
            });
        }
    }
}
