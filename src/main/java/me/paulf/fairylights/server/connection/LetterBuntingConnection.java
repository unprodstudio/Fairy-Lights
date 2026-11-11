package me.paulf.fairylights.server.connection;

import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.server.collision.Intersection;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.feature.Letter;
import me.paulf.fairylights.server.item.components.FLComponents;
import me.paulf.fairylights.server.net.clientbound.OpenEditLetteredConnectionScreenMessage;
import me.paulf.fairylights.util.Catenary;
import me.paulf.fairylights.util.Curve;
import me.paulf.fairylights.util.Utils;
import me.paulf.fairylights.util.styledstring.StylingPresence;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

public final class LetterBuntingConnection extends Connection implements Lettered {
    public static final SymbolSet SYMBOLS = new SymbolSet.Builder(7, "0-9, A-Z, &, !, ?")
        .add(" 0123456789ABCDEFGHJKLMNOPQRSTUVWXYZ&?", 6)
        .add("I", 4)
        .add("!", 2)
        .build();

    private static final float TRACKING = 1.0F / 16.0F;

    private static final StylingPresence SUPPORTED_STYLING = new StylingPresence(true, false, false, false, false, false);

    private Component text;

    private Letter[] letters = new Letter[0];

    public LetterBuntingConnection(final ConnectionType<? extends LetterBuntingConnection> type, final Level world, final Fastener<?> fastener, final UUID uuid) {
        super(type, world, fastener, uuid);
        this.text = Component.empty();
    }

    @Override
    public float getRadius() {
        return 0.9F / 32;
    }

    public Letter[] getLetters() {
        return this.letters;
    }

    @Override
    public void processClientAction(final Player player, final PlayerAction action, final Intersection intersection) {
        if (this.openTextGui(player, action, intersection)) {
            super.processClientAction(player, action, intersection);
        }
    }

    @Override
    public void onConnect(final Level world, final Player user, final ItemStack heldStack) {
        if (Utils.isEmpty(this.text)) {
            ServerPlayNetworking.send((ServerPlayer) user, new OpenEditLetteredConnectionScreenMessage<>(this));
        }
    }

    @Override
    protected void onUpdate() {
        for (final Letter letter : this.letters) {
            letter.tick(this.world);
        }
    }

    @Override
    protected void onCalculateCatenary(final boolean relocated) {
        this.updateLetters();
    }

    private void updateLetters() {
        if (Utils.isEmpty(this.text)) {
            this.letters = new Letter[0];
        } else {
            final Curve catenary = this.getCatenary();
            float textWidth = 0;
            final MutableInt textLen = new MutableInt();
            final String text = this.text.getString();
            final int textLength = text.length();
            final float[] pointOffsets = new float[textLength];
            final float catLength = catenary.getLength();
            for (int i = 0; i < textLength; i++) {
                final float w = SYMBOLS.getWidth(text.charAt(i));
                pointOffsets[i] = textWidth + w / 2.0F;
                textWidth += w + TRACKING;
                if (textWidth > catLength) {
                    break;
                }
                textLen.increment();
            }
            final float offset = catLength / 2 - textWidth / 2;
            for (int i = 0; i < textLen.intValue(); i++) {
                pointOffsets[i] += offset;
            }
            final MutableInt pointIdx = new MutableInt(0);
            final Letter[] prevLetters = this.letters;
            final List<Letter> letters = new ArrayList<>();
            final Catenary.SegmentIterator it = catenary.iterator();
            final MutableFloat distance = new MutableFloat(0f);
            while (it.next()) {
                final float length = it.getLength();
                this.text.getVisualOrderText().accept((position, style, codepoint) -> {
                    if (position >= pointIdx.intValue() && position < textLen.intValue()) {
                        final float pointOffset = pointOffsets[position];
                        if (pointOffset < distance.floatValue() + length) {
                            final float t = (pointOffset - distance.floatValue()) / length;
                            final Vec3 point = new Vec3(it.getX(t), it.getY(t), it.getZ(t));
                            final Letter letter;
                            if (prevLetters != null && pointIdx.intValue() < prevLetters.length) {
                                letter = prevLetters[pointIdx.intValue()];
                                letter.set(point, it.getYaw(), it.getPitch());
                                letter.set(codepoint, style);
                            } else {
                                letter = new Letter(pointIdx.intValue(), point, it.getYaw(), it.getPitch(), SYMBOLS, text.charAt(pointIdx.intValue()), Utils.styleAt(this.text, pointIdx.intValue()));
                            }
                            letters.add(letter);
                            pointIdx.increment();
                        } else {
                            return false;
                        }
                    }

                    return true;
                });
                if (pointIdx.intValue() == textLen.intValue()) {
                    break;
                }
                distance.add(length);
            }
            this.letters = letters.toArray(new Letter[0]);
        }
    }

    @Override
    public StylingPresence getSupportedStyling() {
        return SUPPORTED_STYLING;
    }

    @Override
    public boolean isSupportedCharacter(final int chr) {
        return SYMBOLS.contains(chr);
    }

    @Override
    public boolean isSupportedText(final Component text) {
        final MutableFloat len = new MutableFloat(0);
        final float available = this.getCatenary().getLength();

        if (!text.getVisualOrderText().accept((_, style, codepoint) -> {
            final float w = SYMBOLS.getWidth(codepoint);
            if (len.addAndGet(w + TRACKING) > available) {
                return false;
            }

            return Utils.isPlain(style);
        })) {
            return false;
        }

        return Lettered.super.isSupportedText(text);
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
    public Function<String, String> getInputTransformer() {
        return str -> Normalizer.normalize(str, Normalizer.Form.NFKD).replaceAll("[\\p{Mn}\\p{Sk}]", "").toUpperCase(Locale.ROOT);
    }

    @Override
    public String getAllowedDescription() {
        return SYMBOLS.getDescription();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Screen createTextGUI() {
        return new EditLetteredConnectionScreen<>(this);
    }

    @Override
    public void serializeLogic(ValueOutput output) {
        super.serializeLogic(output);
        output.store("text", ComponentSerialization.CODEC, this.text);
    }

    @Override
    public DataComponentMap serializeItem() {
        return DataComponentMap.composite(
            super.serializeItem(),
            DataComponentMap.builder()
                .set(FLComponents.STYLED_STRING, this.text)
                .build()
        );
    }

    @Override
    public void deserializeLogic(final ValueInput input) {
        super.deserializeLogic(input);
        this.text = input.read("text", ComponentSerialization.CODEC).orElseGet(Component::empty);
    }

    @Override
    public void deserializeLogic(DataComponentMap components) {
        super.deserializeLogic(components);
        this.text = components.getOrDefault(FLComponents.STYLED_STRING, Component.empty());
    }
}
