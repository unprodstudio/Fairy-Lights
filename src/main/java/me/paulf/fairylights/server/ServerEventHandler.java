package me.paulf.fairylights.server;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.TickEvent;
import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.FastenerBlock;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.entity.FenceFastenerEntity;
import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.item.ConnectionItem;
import me.paulf.fairylights.server.jingle.Jingle;
import me.paulf.fairylights.server.jingle.JingleLibrary;
import me.paulf.fairylights.server.jingle.JingleManager;
import me.paulf.fairylights.server.net.clientbound.JingleMessage;
import me.paulf.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import me.paulf.fairylights.server.sound.FLSounds;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

public final class ServerEventHandler {
    public ServerEventHandler() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Player || entity instanceof FenceFastenerEntity) {
                CapabilityHandler.FASTENER_CAP.maybeGet(entity).ifPresent(f -> f.setWorld(world));
            }
        });

        TickEvent.PLAYER_POST.register(this::onTick);

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, direction) -> {
            return onRightClickBlock(player, hand, pos, direction);
        });
    }

    public void onTick(final Player player) {
        CapabilityHandler.FASTENER_CAP.maybeGet(player).ifPresent(fastener -> {
            if (fastener.update() && !player.level().isClientSide()) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
                fastener.serializeNBT(output);
                ServerProxy.sendToPlayersWatchingEntity(new UpdateEntityFastenerMessage(player, output.buildResult()), player);
            }
        });
    }

    public boolean onNoteBlockPlay(Level world, BlockPos pos, int note, NoteBlockInstrument instrument) {
        final Block noteBlock = world.getBlockState(pos).getBlock();
        final BlockState below = world.getBlockState(pos.below());
        if (below.getBlock() == FLBlocks.FASTENER.get() && below.getValue(FastenerBlock.FACING) == Direction.DOWN) {
            final float pitch = (float) Math.pow(2, (note - 12) / 12D);
            world.playSound(null, pos, FLSounds.JINGLE_BELL.get(), SoundSource.RECORDS, 3, pitch);
            world.addParticle(ParticleTypes.NOTE, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, note / 24D, 0, 0);
            if (!world.isClientSide()) {
                final Packet<?> pkt = new ClientboundBlockEventPacket(pos, noteBlock, instrument.ordinal(), note);
                final PlayerList players = world.getServer().getPlayerList();
                players.broadcast(null, pos.getX(), pos.getY(), pos.getZ(), 64, world.dimension(), pkt);
            }
            return true;
        }

        return false;
    }

    public EventResult onRightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction direction) {
        final Level world = player.level();
        if (!(world.getBlockState(pos).getBlock() instanceof FenceBlock)) {
            return EventResult.pass();
        }
        final ItemStack stack = player.getItemInHand(hand);
        boolean checkHanging = stack.getItem() == Items.LEAD;
        boolean shouldCancelUse = false;
        if (hand == InteractionHand.MAIN_HAND) {
            final ItemStack offhandStack = player.getOffhandItem();
            if (offhandStack.getItem() instanceof ConnectionItem) {
                if (checkHanging) {
                    return EventResult.interruptFalse();
                } else {
                    shouldCancelUse = true;
                }
            }
        }
        if (!checkHanging && !world.isClientSide()) {
            final double range = 7;
            final int x = pos.getX();
            final int y = pos.getY();
            final int z = pos.getZ();
            final AABB area = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
            for (final Mob entity : world.getEntitiesOfClass(Mob.class, area)) {
                if (entity.isLeashed() && entity.getLeashHolder() == player) {
                    checkHanging = true;
                    break;
                }
            }
        }
        if (checkHanging) {
            final BlockAttachedEntity entity = FenceFastenerEntity.findHanging(world, pos);
            if (entity != null && !(entity instanceof LeashFenceKnotEntity)) {
                if (shouldCancelUse)
                    return EventResult.interruptTrue();
                else
                    return EventResult.interruptFalse();
            }
        }

        if (shouldCancelUse)
            return EventResult.interruptTrue();

        return EventResult.pass();
    }

    public static boolean tryJingle(final Level world, final HangingLightsConnection hangingLights) {
        String lib;
        if (FairyLights.CHRISTMAS.isOccurringNow()) {
            lib = JingleLibrary.CHRISTMAS;
        } else if (FairyLights.HALLOWEEN.isOccurringNow()) {
            lib = JingleLibrary.HALLOWEEN;
        } else {
            lib = JingleLibrary.RANDOM;
        }
        return tryJingle(world, hangingLights, lib);
    }

    public static boolean tryJingle(final Level world, final HangingLightsConnection hangingLights, final String lib) {
        if (world.isClientSide()) return false;
        final Light<?>[] lights = hangingLights.getFeatures();
        final Jingle jingle = JingleManager.INSTANCE.get(lib).getRandom(world.getRandom(), lights.length);
        if (jingle != null) {
            final int lightOffset = lights.length / 2 - jingle.getRange() / 2;
            hangingLights.play(jingle, lightOffset);
            ServerProxy.sendToPlayersWatchingChunk(new JingleMessage(hangingLights, lightOffset, jingle), world, hangingLights.getFastener().getPos());
            return true;
        }
        return false;
    }
}
