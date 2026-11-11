package me.paulf.fairylights.server.jingle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.sound.FLSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JinglePlayer {
    public static final Codec<JinglePlayer> CODEC = StateType.CODEC.dispatch("state", State::getType, type -> type.getFactory().fieldOf("data"))
        .xmap(state -> {
            var player = new JinglePlayer();
            player.state = state;
            return player;
        }, player -> player.state);

    private State<?> state = new NotPlayingState();

    @Nullable
    public Jingle getJingle() {
        return this.state.getJingle();
    }

    public boolean isPlaying() {
        return this.state.isPlaying();
    }

    public float getProgress() {
        return this.state.getProgress();
    }

    public void play(final Jingle jingle, final int lightOffset) {
        this.state = new PlayingState(jingle, lightOffset);
    }

    public void tick(final Level world, final Vec3 origin, final Light<?>[] lights, final boolean isClient) {
        this.state = this.state.tick(world, origin, lights, isClient);
    }

    private enum StateType implements StringRepresentable {
        NOT_PLAYING("not_playing", NotPlayingState.CODEC),
        PLAYING("playing", PlayingState.CODEC);

        public static final Codec<StateType> CODEC = StringRepresentable.fromEnum(StateType::values);

        private final String serializedName;
        private final MapCodec<? extends JinglePlayer.State<?>> factory;

        StateType(final String serializedName, final MapCodec<? extends JinglePlayer.State<?>> factory) {
            this.serializedName = serializedName;
            this.factory = factory;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        public MapCodec<State<?>> getFactory() {
            return (MapCodec<State<?>>) factory;
        }
    }

    private abstract static class StateFactory<S extends State<S>> {
        public abstract String getId();

        public abstract CompoundTag serialize(S state);

        public abstract State<?> deserialize(CompoundTag compound);
    }

    private abstract static class State<S extends State<S>> {
        public abstract Jingle getJingle();

        public abstract boolean isPlaying();

        public abstract float getProgress();

        public abstract State<?> tick(Level world, Vec3 origin, Light<?>[] lights, boolean isClient);

        public abstract StateType getType();

        public abstract S resolve();
    }

    private static final class NotPlayingState extends State<NotPlayingState> {
        public static final NotPlayingState INSTANCE = new NotPlayingState();
        public static final MapCodec<NotPlayingState> CODEC = MapCodec.unit(INSTANCE);

        @Override
        public Jingle getJingle() {
            return null;
        }

        @Override
        public boolean isPlaying() {
            return false;
        }

        @Override
        public float getProgress() {
            return 0;
        }

        @Override
        public State<?> tick(final Level world, final Vec3 origin, final Light<?>[] lights, final boolean isClient) {
            return this;
        }

        @Override
        public StateType getType() {
            return StateType.NOT_PLAYING;
        }

        @Override
        public NotPlayingState resolve() {
            return this;
        }
    }

    private static final class PlayingState extends State<PlayingState> {
        public static final MapCodec<PlayingState> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                Jingle.CODEC.fieldOf("jingle")
                    .forGetter(state -> state.jingle),
                Codec.INT.optionalFieldOf("lightOffset", 0)
                    .forGetter(state -> state.lightOffset),
                Codec.INT.optionalFieldOf("index", 0)
                    .forGetter(state -> state.index),
                Codec.INT.optionalFieldOf("rest", 0)
                    .forGetter(state -> state.rest),
                Codec.INT.optionalFieldOf("time", 0)
                    .forGetter(state -> state.time)
            )
                .apply(instance, (jingle, lightOffset, index, rest, time) -> {
                    var state = new PlayingState(jingle, lightOffset);
                    state.index = index;
                    state.rest = rest;
                    state.time = time;
                    return state;
                })
        );

        private final Jingle jingle;

        private final int lightOffset;

        private final List<Jingle.PlayTick> playTicks;

        private final int length;

        private final ParticleOptions[] noteParticle;

        private int index;

        private int rest;

        private int time;

        private PlayingState(final Jingle jingle, final int lightOffset) {
            this(jingle, lightOffset, jingle.getPlayTicks(), jingle.getLength(), getParticles(jingle));
        }

        private PlayingState(final Jingle jingle, final int lightOffset, final List<Jingle.PlayTick> playTicks, final int length, final ParticleOptions[] noteParticle) {
            this.jingle = jingle;
            this.lightOffset = lightOffset;
            this.playTicks = playTicks;
            this.length = length;
            this.noteParticle = noteParticle;
        }

        @Override
        public Jingle getJingle() {
            return this.jingle;
        }

        @Override
        public boolean isPlaying() {
            return true;
        }

        @Override
        public float getProgress() {
            return this.time / (float) this.length;
        }

        @Override
        public State<?> tick(final Level world, final Vec3 origin, final Light<?>[] lights, final boolean isClient) {
            this.time++;
            if (this.rest <= 0) {
                if (this.index >= this.playTicks.size()) {
                    return new NotPlayingState();
                }
                final Jingle.PlayTick playTick = this.playTicks.get(this.index++);
                this.rest = playTick.getDuration() - 1;
                if (isClient) {
                    this.play(world, origin, lights, playTick);
                }
            } else {
                this.rest--;
            }
            return this;
        }

        private void play(final Level world, final Vec3 origin, final Light<?>[] lights, final Jingle.PlayTick playTick) {
            for (final int note : playTick.getNotes()) {
                final int idx = note - this.jingle.getLowestNote() + this.lightOffset;
                if (idx >= 0 && idx < lights.length) {
                    lights[idx].jingle(world, origin, note, FLSounds.JINGLE_BELL.get(), this.noteParticle);
                }
            }
        }

        @Override
        public StateType getType() {
            return StateType.PLAYING;
        }

        @Override
        public PlayingState resolve() {
            return this;
        }

        private static ParticleOptions[] getParticles(final Jingle jingle) {
            if (jingle.getTitle().hashCode() == 0xf2b587de) {
                return new ParticleOptions[]{ParticleTypes.NOTE, ParticleTypes.LAVA};
            }
            if (jingle.getTitle().hashCode() == 0xc3fa68bd || jingle.getTitle().hashCode() == 0x70f77bf4) {
                return new ParticleOptions[]{ParticleTypes.NOTE, ParticleTypes.HEART};
            }
            return new ParticleOptions[]{ParticleTypes.NOTE};
        }
    }
}
