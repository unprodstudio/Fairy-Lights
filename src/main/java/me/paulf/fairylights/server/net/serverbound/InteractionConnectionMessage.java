package me.paulf.fairylights.server.net.serverbound;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.collision.Intersection;
import me.paulf.fairylights.server.connection.Connection;
import me.paulf.fairylights.server.connection.PlayerAction;
import me.paulf.fairylights.server.feature.FeatureType;
import me.paulf.fairylights.server.net.ConnectionInfo;
import me.paulf.fairylights.server.net.ConnectionMessage;
import me.paulf.fairylights.server.net.ExtraStreamCodecs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class InteractionConnectionMessage extends ConnectionMessage {
    public static final Type<InteractionConnectionMessage> TYPE = new Type(FairyLights.id("interaction_connection"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, InteractionConnectionMessage> CODEC = StreamCodec.composite(
            ConnectionInfo.STREAM_CODEC, ConnectionMessage::getData,
            PlayerAction.STREAM_CODEC, msg -> msg.type,
            ExtraStreamCodecs.VEC3, msg -> msg.hit,
            FeatureType.STREAM_CODEC, msg -> msg.featureType,
            ByteBufCodecs.VAR_INT, msg -> msg.featureId,
            InteractionConnectionMessage::new
    );

    private static final float RANGE = (Connection.MAX_LENGTH + 1) * (Connection.MAX_LENGTH + 1);

    private static final float REACH = 6 * 6;

    private PlayerAction type;

    private Vec3 hit;

    private FeatureType featureType;

    private int featureId;

    public InteractionConnectionMessage(ConnectionInfo data, PlayerAction type, Vec3 hit, FeatureType featureType, int featureId) {
        super(data);
        this.type = type;
        this.hit = hit;
        this.featureType = featureType;
        this.featureId = featureId;
    }

    public InteractionConnectionMessage(final Connection connection, final PlayerAction type, final Intersection intersection) {
        super(connection);
        this.type = type;
        this.hit = intersection.getResult();
        this.featureType = intersection.getFeatureType();
        this.featureId = intersection.getFeature().getId();
    }

    public static final class Handler implements ServerPlayNetworking.PlayPayloadHandler<InteractionConnectionMessage> {
        @Override
        public void receive(final InteractionConnectionMessage message, final ServerPlayNetworking.Context context) {
            final ServerPlayer player = context.player();
            getConnection(message, c -> true, player.level()).ifPresent(connection -> {
                if (connection.isModifiable(player) &&
                    player.distanceToSqr(Vec3.atLowerCornerOf(connection.getFastener().getPos())) < RANGE &&
                    player.distanceToSqr(message.hit.x, message.hit.y, message.hit.z) < REACH
                ) {
                    if (message.type == PlayerAction.ATTACK) {
                        connection.disconnect(player, message.hit);
                    } else {
                        this.interact(message, player, connection, message.hit);
                    }
                }
            });
        }

        private void interact(final InteractionConnectionMessage message, final Player player, final Connection connection, final Vec3 hit) {
            for (final InteractionHand hand : InteractionHand.values()) {
                final ItemStack stack = player.getItemInHand(hand);
                final ItemStack oldStack = stack.copy();
                if (connection.interact(player, hit, message.featureType, message.featureId, stack, hand)) {
                    this.updateItem(player, oldStack, stack, hand);
                    break;
                }
            }
        }

        private void updateItem(final Player player, final ItemStack oldStack, final ItemStack stack, final InteractionHand hand) {
            if (stack.getCount() <= 0 && !player.getAbilities().instabuild) {
                //ForgeEventFactory.onPlayerDestroyItem(player, stack, hand);
                player.setItemInHand(hand, ItemStack.EMPTY);
            } else if (stack.getCount() < oldStack.getCount() && player.getAbilities().instabuild) {
                stack.setCount(oldStack.getCount());
            }
        }
    }
}
