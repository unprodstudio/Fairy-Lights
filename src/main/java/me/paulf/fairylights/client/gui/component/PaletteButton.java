package me.paulf.fairylights.client.gui.component;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import me.paulf.fairylights.client.renderer.FLRenderPipelines;
import me.paulf.fairylights.util.FLMth;
import me.paulf.fairylights.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

import static net.minecraft.network.chat.TextColor.*;

public class PaletteButton extends Button {
    private static final Identifier PALETTE = FairyLights.id("palette");
    private static final Identifier SELECT = FairyLights.id("palette_color_select");
    private static final Identifier COLOR = FairyLights.id("palette_color");

    private static final int COLOR_WIDTH = 6;

    private static final int COLOR_HEIGHT = 6;

    private static final List<TextColor> IDX_COLOR = List.of(WHITE, GRAY, DARK_GRAY, BLACK, RED, DARK_RED, YELLOW, GOLD, LIGHT_PURPLE, DARK_PURPLE, GREEN, DARK_GREEN, BLUE, DARK_BLUE, AQUA, DARK_AQUA);

    private final ColorButton colorBtn;

    public PaletteButton(final int x, final int y, final ColorButton colorBtn, final Component msg, final Button.OnPress pressable) {
        super(x, y, 28, 28, msg, pressable, DEFAULT_NARRATION);
        this.colorBtn = colorBtn;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.colorBtn.setDisplayColor(IDX_COLOR.get((IDX_COLOR.indexOf(this.colorBtn.getDisplayColor()) + 1) % IDX_COLOR.size()));
        super.onPress(input);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        final int idx = this.getMouseOverIndex(event.x(), event.y());
        if (idx > -1) {
            this.colorBtn.setDisplayColor(IDX_COLOR.get(idx));
            super.onPress(event);
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.visible) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PALETTE, this.getX(), this.getY(), this.width, this.height);
            if (this.colorBtn.hasDisplayColor()) {
                final int idx = IDX_COLOR.indexOf(this.colorBtn.getDisplayColor());
                final int selectX = this.getX() + 2 + (idx % 4) * 6;
                final int selectY = this.getY() + 2 + (idx / 4) * 6;
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECT, selectX, selectY, COLOR_WIDTH, COLOR_HEIGHT);
            }
            for (int i = 0; i < IDX_COLOR.size(); i++) {
                final TextColor color = IDX_COLOR.get(i);
                final int rgb = ARGB.opaque(color.getValue());
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, COLOR, this.getX() + 2 + (i % 4) * 6, this.getY() + 2 + i / 4 * 6, COLOR_WIDTH, COLOR_HEIGHT, rgb);
            }
            final int selectIndex = this.getMouseOverIndex(mouseX, mouseY);
            if (selectIndex > -1) {
                final int hoverSelectX = this.getX() + 2 + selectIndex % 4 * 6;
                final int hoverSelectY = this.getY() + 2 + selectIndex / 4 * 6;
                graphics.blitSprite(FLRenderPipelines.GUI_TEXTURED_HIGHLIGHT, SELECT, hoverSelectX, hoverSelectY, COLOR_WIDTH, COLOR_HEIGHT, ARGB.colorFromFloat(0.5f, 1f, 1f, 1f));
            }
        }
    }

    private int getMouseOverIndex(final double mouseX, final double mouseY) {
        final int relX = Mth.floor(mouseX - this.getX() - 3);
        final int relY = Mth.floor(mouseY - this.getY() - 3);
        if (relX < 0 || relY < 0 || relX > 22 || relY > 22) {
            return -1;
        }
        final int bucketX = relX % 6;
        final int bucketY = relY % 6;
        if (bucketX > 3 || bucketY > 3) {
            return -1;
        }
        final int x = relX / 6;
        final int y = relY / 6;
        return x + y * 4;
    }
}
