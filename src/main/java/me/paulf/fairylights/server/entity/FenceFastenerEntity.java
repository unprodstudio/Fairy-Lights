package me.paulf.fairylights.server.entity;

import me.paulf.fairylights.server.ServerProxy;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.item.ConnectionItem;
import me.paulf.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class FenceFastenerEntity extends HangingEntity {
    private static final EntityDataAccessor<Optional<CompoundTag>> FASTENER = SynchedEntityData.defineId(FenceFastenerEntity.class, FLEntityDataSerializers.COMPOUND_TAG);

    private int surfaceCheckTime;

    public FenceFastenerEntity(final EntityType<? extends FenceFastenerEntity> type, final Level world) {
        super(type, world);
    }

    public FenceFastenerEntity(final Level world) {
        this(FLEntities.FASTENER.get(), world);
    }

    public FenceFastenerEntity(final Level world, final BlockPos pos) {
        this(world);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    /*@Override
    public int getWidth() {
        return 9;
    }

    @Override
    public int getHeight() {
        return 9;
    }*/

    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        return distance < 4096;
    }

    @Override
    public boolean survives() {
        return !this.level().isLoaded(this.pos) || ConnectionItem.isFence(this.level().getBlockState(this.pos));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(FASTENER, Optional.empty());
    }

    @Override
    public void remove(final RemovalReason reason) {
        this.getFastener().ifPresent(Fastener::remove);
        super.remove(reason);
    }

    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableToBase(source) || source.is(DamageTypeTags.IS_EXPLOSION);
    }

    // Copy from super but remove() moved to after onBroken()
    @Override
    public boolean hurtServer(ServerLevel level, final DamageSource source, final float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.level().isClientSide() && this.isAlive()) {
            this.markHurt();
            this.dropItem(level, source.getEntity());
            this.remove(RemovalReason.KILLED);
        }
        return true;
    }

    @Override
    public void dropItem(ServerLevel level, @Nullable final Entity breaker) {
        this.getFastener().ifPresent(fastener -> fastener.dropItems(this.level(), this.pos));
        if (breaker != null) {
            this.level().levelEvent(2001, this.pos, Block.getId(FLBlocks.FASTENER.get().defaultBlockState()));
        }
    }

    @Override
    public void playPlacementSound() {
        final SoundType sound = FLBlocks.FASTENER.get().defaultBlockState().getSoundType();
        this.playSound(sound.getPlaceSound(), (sound.getVolume() + 1) / 2, sound.getPitch() * 0.8F);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.BLOCKS;
    }

    @Override
    public void setPos(final double x, final double y, final double z) {
        super.setPos(Mth.floor(x) + 0.5, Mth.floor(y) + 0.5, Mth.floor(z) + 0.5);
    }

    @Override
    public void setDirection(final Direction facing) {}

    @Override
    protected AABB calculateBoundingBox(BlockPos pos, Direction direction) {
        final double posX = this.pos.getX() + 0.5;
        final double posY = this.pos.getY() + 0.5;
        final double posZ = this.pos.getZ() + 0.5;
        //this.setPosRaw(posX, posY, posZ);
        final float w = 3 / 16F;
        final float h = 3 / 16F;
        return new AABB(posX - w, posY - h, posZ - w, posX + w, posY + h, posZ + w);
    }

    // FIXME 26.3
//    @Override
//    public AABB getBoundingBoxForCulling() {
//        return this.getFastener().map(fastener -> fastener.getBounds().inflate(1)).orElseGet(super::getBoundingBoxForCulling);
//    }

    @Override
    public void tick() {
        this.getFastener().ifPresent(fastener -> {
            if (!this.level().isClientSide() && (fastener.hasNoConnections() || this.checkSurface())) {
                this.dropItem((ServerLevel) this.level(), null);
                this.remove(RemovalReason.DISCARDED);
            } else if (fastener.update() && !this.level().isClientSide()) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, this.registryAccess());
                fastener.serializeNBT(output);
                final UpdateEntityFastenerMessage msg = new UpdateEntityFastenerMessage(this, output.buildResult());
                ServerProxy.sendToPlayersWatchingEntity(msg, this);
            }
        });
    }

    private boolean checkSurface() {
        if (this.surfaceCheckTime++ == 100) {
            this.surfaceCheckTime = 0;
            return !this.survives();
        }
        return false;
    }

    @Override
    public InteractionResult interact(final Player player, final InteractionHand hand, Vec3 location) {
        final ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ConnectionItem) {
            if (this.level().isClientSide()) {
                player.swing(hand, SwingAnimation.DEFAULT, false);
            } else {
                this.getFastener().ifPresent(fastener -> ((ConnectionItem) stack.getItem()).connect(stack, player, this.level(), fastener));
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand, location);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("pos", BlockPos.CODEC, this.pos);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.pos = input.read("pos", BlockPos.CODEC).orElseThrow();
    }

    /*@Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }*/

    private Optional<Fastener<?>> getFastener() {
        return CapabilityHandler.FASTENER_CAP.maybeGet(this);
    }

    public static FenceFastenerEntity create(final Level world, final BlockPos fence) {
        final FenceFastenerEntity fastener = new FenceFastenerEntity(world, fence);
        //fastener.forceSpawn = true;
        world.addFreshEntity(fastener);
        fastener.playPlacementSound();
        return fastener;
    }

    @Nullable
    public static FenceFastenerEntity find(final Level world, final BlockPos pos) {
        final BlockAttachedEntity entity = findHanging(world, pos);
        if (entity instanceof FenceFastenerEntity) {
            return (FenceFastenerEntity) entity;
        }
        return null;
    }

    @Nullable
    public static BlockAttachedEntity findHanging(final Level world, final BlockPos pos) {
        for (final BlockAttachedEntity e : world.getEntitiesOfClass(BlockAttachedEntity.class, new AABB(pos).inflate(2))) {
            if (e.getPos().equals(pos)) {
                return e;
            }
        }
        return null;
    }
}
