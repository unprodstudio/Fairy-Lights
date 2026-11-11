package me.paulf.fairylights.server.connection;

import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.server.collision.Intersection;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.feature.FeatureType;
import me.paulf.fairylights.server.feature.Pennant;
import me.paulf.fairylights.server.item.DyeableItem;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.sound.FLSounds;
import me.paulf.fairylights.util.OreDictUtils;
import me.paulf.fairylights.util.fabric.ItemHandlerHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PennantBuntingConnection extends HangingFeatureConnection<Pennant> implements Lettered {
    private List<ItemStack> pattern;

    private Component text;

    public PennantBuntingConnection(final ConnectionType<? extends PennantBuntingConnection> type, final Level world, final Fastener<?> fastener, final UUID uuid) {
        super(type, world, fastener, uuid);
        this.pattern = new ArrayList<>();
        this.text = Component.empty();
    }

    @Override
    public float getRadius() {
        return 0.045F;
    }

    @Override
    public void processClientAction(final Player player, final PlayerAction action, final Intersection intersection) {
        if (this.openTextGui(player, action, intersection)) {
            super.processClientAction(player, action, intersection);
        }
    }

    @Override
    public boolean interact(final Player player, final Vec3 hit, final FeatureType featureType, final int feature, final ItemStack heldStack, final InteractionHand hand) {
        if (featureType == FEATURE && OreDictUtils.isDye(heldStack)) {
            final int index = feature % this.pattern.size();
            final ItemStack pennant = this.pattern.get(index);
            if (!ItemStack.matches(pennant, heldStack)) {
                final ItemStack placed = heldStack.split(1);
                this.pattern.set(index, placed);
                ItemHandlerHelper.giveItemToPlayer(player, pennant);
                this.computeCatenary();
                heldStack.shrink(1);
                this.world.playSound(null, hit.x, hit.y, hit.z, FLSounds.FEATURE_COLOR_CHANGE.get(), SoundSource.BLOCKS, 1, 1);
                return true;
            }
        }
        return super.interact(player, hit, featureType, feature, heldStack, hand);
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        for (final Pennant light : this.features) {
            light.tick(this.world);
        }
    }

    @Override
    protected Pennant[] createFeatures(final int length) {
        return new Pennant[length];
    }

    @Override
    protected Pennant createFeature(final int index, final Vec3 point, final float yaw, final float pitch) {
        final ItemStack data = this.pattern.isEmpty() ? ItemStack.EMPTY : this.pattern.get(index % this.pattern.size());
        return new Pennant(index, point, yaw, pitch, DyeableItem.getColor(data), data.getItem());
    }

    @Override
    protected float getFeatureSpacing() {
        return 0.6875F;
    }

    @Override
    public boolean isSupportedText(final Component text) {
        return text.getString().length() <= this.features.length && Lettered.super.isSupportedText(text);
    }

    @Override
    public void setText(final Component text) {
        this.text = text;
        this.computeCatenary();
    }

    @Override
    public Component getText() {
        return this.text;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Screen createTextGUI() {
        return new EditLetteredConnectionScreen<>(this);
    }

    @Override
    public void serializeLogic(ValueOutput output) {
        super.serializeLogic(output);
        final var patternList = output.list("pattern", ItemStack.OPTIONAL_CODEC);
        for (final ItemStack entry : this.pattern) {
            patternList.add(entry);
        }
        output.store("text", ComponentSerialization.CODEC, this.text);
    }

    @Override
    public DataComponentMap serializeItem() {
        return DataComponentMap.composite(
            super.serializeItem(),
            DataComponentMap.builder()
                .set(FLComponents.PATTERN, List.copyOf(this.pattern))
                .set(FLComponents.STYLED_STRING, this.text)
                .build()
        );
    }

    @Override
    public void deserializeLogic(ValueInput input) {
        super.deserializeLogic(input);
        this.pattern = new ArrayList<>();
        final var patternList = input.listOrEmpty("pattern", ItemStack.OPTIONAL_CODEC);
        for (ItemStack stack : patternList) {
            this.pattern.add(stack);
        }
        this.text = input.read("text", ComponentSerialization.CODEC).orElse(null);
    }

    @Override
    public void deserializeLogic(DataComponentMap components) {
        super.deserializeLogic(components);
        this.pattern = components.getOrDefault(FLComponents.PATTERN, new ArrayList<>());
        this.text = components.getOrDefault(FLComponents.STYLED_STRING, Component.empty());
    }
}
