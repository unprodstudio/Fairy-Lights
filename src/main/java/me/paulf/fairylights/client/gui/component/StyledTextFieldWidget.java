package me.paulf.fairylights.client.gui.component;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.client.renderer.FLRenderPipelines;
import me.paulf.fairylights.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class StyledTextFieldWidget extends AbstractWidget {
    private static final Predicate<String> ALWAYS_TRUE = str -> true;

    private static final Function<String, String> IDENTITY_CHARACTER_TRANSFORMER = c -> c;

    private final Font font;

    private final int multiClickInterval = getMultiClickInterval();

    private final ColorButton colorBtn;

    private final ToggleButton boldBtn;

    private final ToggleButton italicBtn;

    private final ToggleButton underlineBtn;

    private final ToggleButton strikethroughBtn;

    private Component value;

    private int maxLength = 32;

    private int tick;

    private boolean hasBackground = true;

    private boolean isBlurable = true;

    private boolean isFocused;

    private boolean isWritable = true;

    private boolean isVisible = true;

    private int lineScrollOffset;

    private int caret;

    private int selectionEnd;

    private int writableTextColor = 0xE0E0E0;

    private int readonlyTextColor = 0x707070;

    private boolean isDraggingSelection;

    private boolean hasDraggedSelecton;

    private boolean isPressed;

    private long lastClickTime;

    private int multiClicks;

    private Function<String, String> charInputTransformer = IDENTITY_CHARACTER_TRANSFORMER;

    private Predicate<String> validator = ALWAYS_TRUE;

    private final List<ChangeListener> changeListeners = new ArrayList<>();

    private Style currentStyle;

    public StyledTextFieldWidget(
        final Font font,
        final ColorButton colorBtn,
        final ToggleButton boldBtn,
        final ToggleButton italicBtn,
        final ToggleButton underlineBtn,
        final ToggleButton strikethroughBtn,
        final int x, final int y, final int width, final int height,
        final Component msg
    ) {
        super(x, y, width, height, msg);
        this.font = font;
        this.colorBtn = colorBtn;
        this.boldBtn = boldBtn;
        this.italicBtn = italicBtn;
        this.underlineBtn = underlineBtn;
        this.strikethroughBtn = strikethroughBtn;
        this.setValue0(Component.empty());
        this.setStyle(Style.EMPTY);
    }

    public void setIsBlurable(final boolean isBlurable) {
        this.isBlurable = isBlurable;
    }

    public void setVisible(final boolean isVisible) {
        this.isVisible = isVisible;
    }

    public void updateStyling(final ChatFormatting styling) {
        int start = this.caret;
        int end = this.selectionEnd;
        if (start == end) {
            this.setStyle(this.currentStyle.applyFormat(styling));
        } else {
            if (end < start) {
                final int t = start;
                start = end;
                end = t;
            }
            this.setValue0(Utils.withStyling(this.value, start, end, Style.EMPTY.applyFormat(styling)));
            this.updateSelectionControls();
        }
    }

    public void updateStyling(final TextColor styling) {
        int start = this.caret;
        int end = this.selectionEnd;
        if (start == end) {
            this.withStyling(styling);
        } else {
            if (end < start) {
                final int t = start;
                start = end;
                end = t;
            }
            this.setValue0(Utils.withStyling(this.value, start, end, Style.EMPTY.withColor(styling)));
            this.updateSelectionControls();
        }
    }

    public void setColor(final TextColor color) {
        this.setStyle(this.currentStyle.withColor(color));
    }

    public void withStyling(final TextColor styling) {
        this.setStyle(this.currentStyle.withColor(styling));
    }

    public void setStyle(final Style style) {
        this.currentStyle = style;
        this.colorBtn.setDisplayColor(this.currentStyle.getColor());
        this.boldBtn.setValue(style.isBold());
        this.italicBtn.setValue(style.isItalic());
        this.underlineBtn.setValue(style.isUnderlined());
        this.strikethroughBtn.setValue(style.isStrikethrough());
    }

    public TextColor getColor() {
        return this.currentStyle.getColor();
    }

    public void setValue(final String value) {
        this.setValue(Component.literal(value).withStyle(this.currentStyle));
    }

    public void setValue(final Component value) {
        if (this.validator.test(value.getString())) {
            if (value.getString().length() > this.maxLength) {
                this.setValue0(trimToWidth(value, Minecraft.getInstance().font, this.maxLength));
            } else {
                this.setValue0(value);
            }
        }
    }

    private void setValue0(final Component value) {
        this.value = value;
        for (final ChangeListener listener : this.changeListeners) {
            listener.onChange(value);
        }
    }

    public String getUnstyledValue() {
        return this.value.getString();
    }

    public Component getValue() {
        return this.value;
    }

    public String getUnstyledSelectedText() {
        return this.getSelectedText().getString();
    }

    public Component getSelectedText() {
        int start = this.caret, end = this.selectionEnd;
        if (end < start) {
            final int t = start;
            start = end;
            end = t;
        }
        return Utils.substr(this.value, start, end);
    }

    public void setCaretStart() {
        this.setCaret(0);
    }

    public void setCaretEnd() {
        this.setCaret(this.value.getString().length());
    }

    public void setCaret(final int pos) {
        this.setCaret(pos, true);
    }

    public void setCaret(final int pos, final boolean changeColor) {
        this.caret = Mth.clamp(pos, 0, this.value.getString().length());
        this.setSelectionPos(this.caret);
        if (changeColor) {
            this.setCurrentStyleByIndex(this.caret);
        }
        this.tick = 0;
    }

    private void setCurrentStyleByIndex(final int index) {
        if (this.value.getString().length() > 0) {
            this.setStyle(Utils.styleAt(this.value, index <= 0 ? 0 : index - 1));
        }
    }

    public int getCaret() {
        return this.caret;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public Style getStyle() {
        return this.currentStyle;
    }

    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }

    public void setHasBackground(final boolean hasBackground) {
        this.hasBackground = hasBackground;
    }

    public void setTextColor(final int writableTextColor) {
        this.writableTextColor = writableTextColor & 0xFFFFFF;
    }

    public void setReadonlyTextColor(final int readonlyTextColor) {
        this.readonlyTextColor = readonlyTextColor & 0xFFFFFF;
    }

    @Override
    public void setFocused(final boolean isFocused) {
        if (isFocused) {
            if (!this.isFocused) {
                this.tick = 0;
            }
        } else if (this.selectionEnd != this.caret) {
            this.setSelectionPos(this.caret);
        }
        this.isFocused = isFocused;
    }

    @Override
    public boolean isFocused() {
        return this.isFocused;
    }

    public void setWritable(final boolean isWritable) {
        this.isWritable = isWritable;
    }

    public void setCharInputTransformer(final Function<String, String> charTransformer) {
        this.charInputTransformer = charTransformer;
    }

    public void setValidator(final Predicate<String> validator) {
        this.validator = validator;
    }

    public int getInnerWidth() {
        return this.hasBackground ? this.width - 8 : this.width;
    }

    public void setSelectionPos(final int pos) {
        final int len = this.value.getString().length();
        this.selectionEnd = Mth.clamp(pos, 0, len);
        if (this.lineScrollOffset > len) {
            this.lineScrollOffset = len;
        }
        final int w = trimToWidth(Utils.substr(this.value, this.lineScrollOffset), this.font, this.getInnerWidth(), true).getString().length();
        if (this.selectionEnd > w + this.lineScrollOffset) {
            this.lineScrollOffset = this.selectionEnd - w;
        } else if (this.selectionEnd <= this.lineScrollOffset) {
            this.lineScrollOffset = this.selectionEnd;
        }
        this.lineScrollOffset = Mth.clamp(this.lineScrollOffset, 0, len);
        if (this.caret != this.selectionEnd) {
            this.updateSelectionControls();
        }
    }

    private void updateSelectionControls() {
        final Component selected = this.getSelectedText();
        TextColor color = null;
        boolean consistantColor = true;
        boolean bold = true, italic = true, underline = true, strikethrough = true;
        for (int i = 0; i < selected.getString().length(); i++) {
            final Style s = Utils.styleAt(selected, i);
            if (color != null && color != s.getColor()) {
                color = null;
                consistantColor = false;
            }
            if (consistantColor) {
                color = s.getColor();
            }
            if (!s.isBold()) {
                bold = false;
            }
            if (!s.isItalic()) {
                italic = false;
            }
            if (!s.isUnderlined()) {
                underline = false;
            }
            if (!s.isStrikethrough()) {
                strikethrough = false;
            }
        }
        this.setStyle(Style.EMPTY
            .withColor(color == null ? this.currentStyle.getColor() : color)
            .withBold(bold)
            .withStrikethrough(strikethrough)
            .withUnderlined(underline)
            .withItalic(italic));
        if (!consistantColor) {
            this.colorBtn.removeDisplayColor();
        }
    }

    public void registerChangeListener(final ChangeListener listener) {
        if (!this.changeListeners.contains(listener)) {
            this.changeListeners.add(listener);
        }
        listener.onChange(this.value);
    }

    public boolean removeChangeListener(final ChangeListener listener) {
        return this.changeListeners.remove(listener);
    }

    public void tick() {
        this.tick++;
    }

    @Override
    public void mouseMoved(final double x, final double y) {
        final int mouseX = (int) x;
        if (this.isPressed && this.caret != this.selectionEnd) {
            final int lower;
            final int upper;
            if (this.hasBackground) {
                lower = this.getX() + 15;
                upper = this.getX() + this.width - 16;
            } else {
                lower = this.getX() + 11;
                upper = this.getX() + this.width - 12;
            }
            boolean scrolled = false;
            if (mouseX < lower) {
                if (this.lineScrollOffset > 0) {
                    final int rate = (2 - (mouseX - this.getX()) / 5) * 2 + 2;
                    this.lineScrollOffset -= rate;
                    if (this.lineScrollOffset < 0) {
                        this.lineScrollOffset = 0;
                    }
                    scrolled = true;
                }
            } else if (mouseX > upper) {
                final int max = this.value.getString().length() - trimToWidth(this.value, this.font, this.getInnerWidth(), true).getString().length();
                if (this.lineScrollOffset < max) {
                    final int rate = (2 + (mouseX - this.getX() - this.width + 1) / 5) * 2 + 2;
                    this.lineScrollOffset += rate;
                    if (this.lineScrollOffset > max) {
                        this.lineScrollOffset = max;
                    }
                    scrolled = true;
                }
            }
            if (scrolled && !this.hasDraggedSelecton) {
                int relativeX = mouseX - this.getX();
                if (this.hasBackground) {
                    relativeX -= 2;
                }
                this.setSelectionPos(this.getIndexInTextByX(relativeX));
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.isFocused) {
            return false;
        }
        if (event.isSelectAll()) {
            this.setCaretEnd();
            this.setSelectionPos(0);
        } else if (event.isCopy()) {
            this.setClipboardString(this.getSelectedText());
        } else if (event.isPaste()) {
            if (this.isWritable) {
                final Component str = this.getClipboardString();
                if (event.hasShiftDown()) {
                    this.writeText(str.getString());
                } else {
                    this.writeText(str);
                }
            }
        } else if (event.isCut()) {
            this.setClipboardString(this.getSelectedText());
            if (this.isWritable) {
                this.writeText("");
            }
        } else if (EditLetteredConnectionScreen.isControlOp(event, InputConstants.KEY_BACKSLASH)) {
            if (this.caret == this.selectionEnd) {
                this.resetCurrentFormatting();
            } else {
                this.resetSelectedFormatting();
            }
        } else {
            switch (event.key()) {
                case InputConstants.KEY_BACKSPACE:
                    if (event.hasControlDown()) {
                        if (this.isWritable) {
                            this.deleteWords(-1);
                        }
                    } else if (this.isWritable) {
                        this.deleteFromCursor(-1);
                    }
                    break;
                case InputConstants.KEY_HOME:
                    if (event.hasShiftDown()) {
                        this.setSelectionPos(0);
                    } else {
                        this.setCaretStart();
                    }
                    break;
                case InputConstants.KEY_LEFT:
                    if (event.hasShiftDown()) {
                        if (event.hasControlDown()) {
                            this.setSelectionPos(this.skipWords(-1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() - 1);
                        }
                    } else if (event.hasControlDown()) {
                        this.setCaret(this.skipWords(-1));
                    } else {
                        if (Utils.isEmpty(this.getSelectedText())) {
                            this.moveCursorBy(-1);
                        } else {
                            this.setCaret(Math.min(this.caret, this.selectionEnd));
                        }
                    }
                    break;
                case InputConstants.KEY_RIGHT:
                    if (event.hasShiftDown()) {
                        if (event.hasControlDown()) {
                            this.setSelectionPos(this.skipWords(1, this.getSelectionEnd()));
                        } else {
                            this.setSelectionPos(this.getSelectionEnd() + 1);
                        }
                    } else if (event.hasControlDown()) {
                        this.setCaret(this.skipWords(1));
                    } else {
                        if (Utils.isEmpty(this.getSelectedText())) {
                            this.moveCursorBy(1);
                        } else {
                            this.setCaret(Math.max(this.caret, this.selectionEnd));
                        }
                    }
                    break;
                case InputConstants.KEY_END:
                    if (event.hasShiftDown()) {
                        this.setSelectionPos(this.value.getString().length());
                    } else {
                        this.setCaretEnd();
                    }
                    break;
                case InputConstants.KEY_DELETE:
                    if (event.hasControlDown()) {
                        if (this.isWritable) {
                            this.deleteWords(1);
                        }
                    } else if (this.isWritable) {
                        this.deleteFromCursor(1);
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.isFocused) {
            return false;
        }
        if (StringUtil.isAllowedChatCharacter(event.codepoint())) {
            final String writeChar = this.charInputTransformer.apply(event.codepointAsString());
            if (this.isWritable) {
                this.writeText(writeChar);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        final boolean hovered = event.x() >= this.getX() && event.x() < this.getX() + this.width && event.y() >= this.getY() && event.y() < this.getY() + this.height;
        if (this.isBlurable) {
            this.setFocused(hovered);
        } else if (!hovered) {
            this.setCaret(Math.min(this.caret, this.selectionEnd));
        }
        if (this.isFocused && hovered && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            int relativeX = Mth.floor(event.x() - this.getX());
            if (this.hasBackground) {
                relativeX -= 2;
            }
            final int idx = this.getIndexInTextByX(relativeX);
            final long now = Util.getMillis();
            if (now - this.lastClickTime <= this.multiClickInterval) {
                this.multiClicks++;
                if (this.multiClicks > 3) {
                    this.multiClicks = 1;
                }
            } else {
                this.multiClicks = 1;
            }
            this.lastClickTime = now;
            if (event.hasShiftDown()) {
                final int end = this.selectionEnd;
                this.setCaret(idx);
                this.setSelectionPos(end);
            } else {
                this.clickIndex(idx);
            }
            this.isPressed = true;
            return true;
        }
        return false;
    }

    private void clickIndex(final int pos) {
        switch (this.multiClicks) {
            case 1: {
                int start = this.caret, end = this.selectionEnd;
                if (end < start) {
                    final int t = start;
                    start = end;
                    end = t;
                }
                this.isDraggingSelection = this.caret != this.selectionEnd && pos >= start && pos < end;
                if (!this.isDraggingSelection) {
                    this.setCaret(pos);
                    this.hasDraggedSelecton = false;
                }
                break;
            }
            case 2: {
                if (pos < this.value.getString().length() && this.value.getString().charAt(pos) == ' ') {
                    int low = pos - 1;
                    int high = pos;
                    final int max = this.value.getString().length() - 1;
                    while (low >= 0) {
                        if (this.value.getString().charAt(low) != ' ') {
                            low++;
                            break;
                        }
                        low--;
                    }
                    while (true) {
                        if (high < max) {
                            if (this.value.getString().charAt(++high) != ' ') {
                                break;
                            }
                        } else {
                            high++;
                            break;
                        }
                    }
                    this.setCaret(high);
                    this.setSelectionPos(low);
                } else {
                    final int low = this.value.getString().lastIndexOf(' ', pos);
                    final int high = this.value.getString().indexOf(' ', pos);
                    this.setCaret(high == -1 ? this.value.getString().length() : high);
                    this.setSelectionPos(low + 1);
                }
                break;
            }
            case 3:
                this.setCaretEnd();
                this.setSelectionPos(0);
                break;
            default:
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            this.isPressed = false;
            if (this.isDraggingSelection) {
                final boolean hovered = event.x() >= this.getX() && event.x() < this.getX() + this.width && event.y() >= this.getY() && event.y() < this.getY() + this.height;
                if (hovered) {
                    int relativeX = Mth.floor(event.x() - this.getX());
                    if (this.hasBackground) {
                        relativeX -= 2;
                    }
                    int pos = this.getIndexInTextByX(relativeX);
                    // Is pos outside of selection
                    if ((pos - this.caret) * (pos - this.selectionEnd) > 0) {
                        final Component selection = this.getSelectedText();
                        this.writeText("");
                        // If pos after selection
                        if ((pos - this.caret) - (this.selectionEnd - pos) > 0) {
                            pos -= selection.getString().length();
                        }
                        this.setCaret(pos);
                        this.writeText(selection);
                        this.setCaret(pos);
                        this.setSelectionPos(pos + selection.getString().length());
                    } else {
                        this.setCaret(pos);
                    }
                }
                this.isDraggingSelection = false;
                this.hasDraggedSelecton = false;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.isFocused && this.isPressed && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            int relativeX = Mth.floor(event.x() - this.getX());
            if (this.hasBackground) {
                relativeX -= 2;
            }
            if (this.isDraggingSelection) {
                this.hasDraggedSelecton = true;
            } else {
                this.setSelectionPos(this.getIndexInTextByX(relativeX));
            }
            return true;
        }
        return false;
    }

    public void writeText(final String input) {
        if (!this.colorBtn.hasDisplayColor()) {
            this.setCurrentStyleByIndex(Math.min(this.selectionEnd, this.caret));
        }
        this.writeText(Component.literal(input).withStyle(this.currentStyle), false);
    }

    public void writeText(final Component input) {
        this.writeText(input, true);
    }

    public void writeText(final Component input, final boolean changeColor) {
        final MutableComponent val = Component.empty();
        final Component finput = filterAllowedCharacters(input);
        int startIdx = this.caret, endIdx = this.selectionEnd;
        if (endIdx < startIdx) {
            final int t = startIdx;
            startIdx = endIdx;
            endIdx = t;
        }
        final int available = this.maxLength - this.value.getString().length() - (startIdx - endIdx);
        if (!this.value.getString().isEmpty()) {
            val.append(Utils.substr(this.value, 0, startIdx));
        }
        final int length;
        if (available < finput.getString().length()) {
            val.append(Utils.substr(finput, 0, available));
            length = available;
        } else {
            val.append(finput);
            length = finput.getString().length();
        }
        if (!this.value.getString().isEmpty() && endIdx < this.value.getString().length()) {
            val.append(Utils.substr(this.value, endIdx));
        }
        if (this.validator.test(val.getString())) {
            this.setValue0(val);
            this.moveCursorBy(startIdx - this.selectionEnd + length, changeColor);
        }
    }

    public static boolean isAllowedCharacter(final char character) {
        return character >= ' ' && character != '\u007F';
    }

    private static Component filterAllowedCharacters(final Component str) {
        final MutableComponent bldr = Component.empty();
        final String string = str.getString();
        for (int i = 0; i < string.length(); i++) {
            final char chr = string.charAt(i);
            if (isAllowedCharacter(chr)) {
                bldr.append(Component.literal(Character.toString(chr)).withStyle(Utils.styleAt(str, i)));
            }
        }
        return Utils.flatten(bldr);
    }

    public void deleteWords(final int num) {
        this.deleteFromCursor(this.skipWords(num) - this.caret);
    }

    public void deleteFromCursor(final int num) {
        if (this.value.getString().length() > 0) {
            if (this.selectionEnd != this.caret) {
                this.writeText("");
            } else {
                final boolean reverse = num < 0;
                final int endIdx = reverse ? this.caret + num : this.caret;
                final int startIdx = reverse ? this.caret : this.caret + num;
                final MutableComponent val = Component.empty();
                Style style = null;
                if (endIdx > 0) {
                    if (endIdx < this.value.getString().length()) {
                        style = Utils.styleAt(this.value, endIdx);
                    }
                    val.append(Utils.substr(this.value, 0, endIdx));
                }
                if (startIdx < this.value.getString().length()) {
                    if (startIdx > 0 && style == null) {
                        style = Utils.styleAt(this.value, startIdx - 1);
                    }
                    val.append(Utils.substr(this.value, startIdx));
                }
                if (this.validator.test(val.getString())) {
                    this.setValue0(val);
                    if (reverse) {
                        this.moveCursorBy(num, false);
                    }
                    if (style == null) {
                        style = this.currentStyle;
                    }
                    this.setStyle(style);
                }
            }
        }
    }

    public int skipWords(final int n) {
        return this.skipWords(n, n < 0 ? Math.min(this.selectionEnd, this.caret) : Math.max(this.selectionEnd, this.caret));
    }

    public int skipWords(final int n, final int pos) {
        int idx = pos;
        final boolean reverse = n < 0;
        final String str = this.value.getString();
        for (int word = 0, len = str.length(), count = Math.abs(n); word < count; word++) {
            if (reverse) {
                while (idx > 0 && str.charAt(idx - 1) == ' ') {
                    idx--;
                }
                while (idx > 0 && str.charAt(idx - 1) != ' ') {
                    idx--;
                }
                if (idx == 0) {
                    break;
                }
            } else {
                while (idx < len && str.charAt(idx) == ' ') {
                    idx++;
                }
                while (idx < len && str.charAt(idx) != ' ') {
                    idx++;
                }
                if (idx == len) {
                    break;
                }
            }
        }
        return idx;
    }

    public void moveCursorBy(final int num) {
        this.setCaret(this.selectionEnd + num, true);
    }

    public void moveCursorBy(final int num, final boolean changeColor) {
        this.setCaret(this.selectionEnd + num, changeColor);
    }

    private int getIndexInTextByX(final int x) {
        final Component s = trimToWidth(Utils.substr(this.value, this.lineScrollOffset), this.font, this.getInnerWidth());
        return trimToWidth(s, this.font, x).getString().length() + this.lineScrollOffset;
    }

    private void resetCurrentFormatting() {
        this.setStyle(Style.EMPTY);
        this.tick = 0;
    }

    private void resetSelectedFormatting() {
        int start = this.caret, end = this.selectionEnd;
        if (end < start) {
            final int t = start;
            start = end;
            end = t;
        }
        this.setValue0(Utils.withStyling(this.value, start, end, Style.EMPTY));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor stack, int mouseX, int mouseY, float a) {
        if (!this.isVisible) {
            return;
        }
        if (this.hasBackground) {
            stack.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, 0xAAA0A0A0);
            stack.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFF000000);
        }
        final int textColor = this.isWritable ? this.writableTextColor : this.readonlyTextColor;
        final int visibleCaret = this.caret - this.lineScrollOffset;
        int visibleSelectionEnd = this.selectionEnd - this.lineScrollOffset;
        final Component visibleText = trimToWidth(Utils.substr(this.value, this.lineScrollOffset), this.font, this.getInnerWidth());
        final boolean isCaretVisible = visibleCaret >= 0 && visibleCaret <= visibleText.getString().length();
        final boolean drawSelection = visibleSelectionEnd != visibleCaret;
        final boolean drawCaret = !drawSelection && this.isFocused && this.tick / 6 % 2 == 0 && isCaretVisible;
        final int offsetX = this.hasBackground ? this.getX() + 4 : this.getX();
        final int offsetY = this.hasBackground ? this.getY() + (this.height - 8) / 2 : this.getY();
        int textX = offsetX;
        if (visibleSelectionEnd > visibleText.getString().length()) {
            visibleSelectionEnd = visibleText.getString().length();
        }
        if (!visibleText.getString().isEmpty()) {
            final Component beforeCaret = (isCaretVisible ? Utils.substr(visibleText, 0, visibleCaret) : visibleText);
            textX = stack.textWithWordWrap(this.font, beforeCaret, offsetX, offsetY, this.getWidth(), textColor, true);
        }
        final int caretX;
        if (isCaretVisible) {
            caretX = --textX;
        } else {
            caretX = visibleCaret > 0 ? offsetX + this.width - 6 : offsetX;
        }
        if (!visibleText.getString().isEmpty() && isCaretVisible && visibleCaret < visibleText.getString().length()) {
            textX = stack.textWithWordWrap(this.font, Utils.substr(visibleText, visibleCaret), textX, offsetY, this.getWidth(), textColor, true);
        }
        if (drawCaret) {
            final int rgb = ARGB.opaque(this.currentStyle.getColor().getValue());
            if (this.currentStyle.isItalic()) {
                stack.fill(caretX - 1, caretX + 1, offsetY - 2, offsetY + 1 + this.font.lineHeight, rgb);
            } else {
                stack.fill(caretX, offsetY - 2, caretX + 1, offsetY + 1 + this.font.lineHeight, 0xFF000000 | rgb);
            }
        }
        if (drawSelection) {
            final int selectionX = offsetX + (visibleSelectionEnd < 0 ? 0 : getWidth(Utils.substr(visibleText, 0, visibleSelectionEnd), this.font));
            int start = caretX, end = selectionX;
            if (end < start) {
                final int t = start;
                start = end;
                end = t;
            }
            this.drawSelectionHighlight(stack, start - 1, offsetY - 2, end, offsetY + 1 + this.font.lineHeight);
        }
        if (this.hasDraggedSelecton) {
            if (this.isHovered) {
                int relativeX = mouseX - this.getX();
                if (this.hasBackground) {
                    relativeX -= 2;
                }
                final int pos = this.getIndexInTextByX(relativeX) - this.lineScrollOffset;
                if (pos >= 0 && pos <= visibleText.getString().length()) {
                    final int x = getWidth(Utils.substr(visibleText, 0, pos), this.font);
                    final int rgb = ARGB.opaque(this.currentStyle.getColor().getValue());
                    stack.fill(offsetX + x, offsetY - 2, offsetX + x + 1, offsetY + 1 + this.font.lineHeight, 0xFF000000 | rgb);
                }
            }
            stack.text(this.font, this.getSelectedText(), mouseX + 5, mouseY + 5, textColor | 0xBF000000, true);
        }
    }

    private void drawSelectionHighlight(final GuiGraphicsExtractor stack, int startX, int startY, int endX, int endY) {
        if (endX > this.getX() + this.width) {
            endX = this.getX() + this.width;
        }
        if (startX > this.getX() + this.width) {
            startX = this.getX() + this.width;
        }
        stack.fill(FLRenderPipelines.GUI_HIGHLIGHT, startX, startY, endX, endY, 0x33FFFFFF);
    }

    public Component getClipboardString() {
        final String str = Minecraft.getInstance().keyboardHandler.getClipboard();
        if (str.indexOf('§') == -1) {
            return Component.literal(str).withStyle(this.currentStyle);
        } else {
            return Component.literal(str);
        }
    }

    private void setClipboardString(final Component value) {
        Minecraft.getInstance().keyboardHandler.setClipboard(value.getString());
    }

    private static int getMultiClickInterval() {
        return 250;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.editBox", this.getMessage(), this.value.getString());
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output)
    {
        output.add(NarratedElementType.TITLE, Component.translatable("narration.edit_box", this.value.getString()));
    }

    @FunctionalInterface
    public interface ChangeListener {
        void onChange(Component value);
    }

    private static int getWidth(final Component styledString, final Font font) {
        return font.width(styledString);
    }

    private static Component trimToWidth(final Component styledString, final Font font, final int width) {
        return trimToWidth(styledString, font, width, false);
    }

    private static Component trimToWidth(final Component styledString, final Font font, final int width, final boolean reverse) {
        final List<Style> styles = new ArrayList<>();
        styles.add(Style.EMPTY);
        final List<String> strings = new ArrayList<>();
        final MutableInt w = new MutableInt(0);

        styledString.visit((style, str) -> {
            if (style != styles.getFirst()) {
                if (reverse) {
                    styles.addFirst(style);
                } else {
                    styles.add(style);
                }

                strings.add(str);
            }

            w.add(font.width(str));
            if (style.isBold())
                w.increment();

            if (w.toInteger() > width)
                return Optional.of(Unit.INSTANCE);

            if (reverse) {
                strings.addFirst(Utils.reverse(str));
            } else {
                strings.add(str);
            }

            return Optional.empty();
        }, Style.EMPTY);

        final MutableComponent shortened = Component.empty();
        for (int i = 0; i < strings.size(); i++) {
            String str = strings.get(i);
            Style style = styles.size() > strings.size() ? styles.get(i + 1) : styles.get(i);
            shortened.append(Component.literal(str).withStyle(style));
        }

        return shortened;
    }
}
