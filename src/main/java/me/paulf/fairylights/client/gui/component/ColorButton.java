package me.paulf.fairylights.client.gui.component;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public final class ColorButton extends Button {
    private static final Identifier COLOR = FairyLights.id("color");
    private static final Identifier COLOR_SELECTED = FairyLights.id("color_selected");

    private static final Identifier DISPLAY_COLOR = FairyLights.id("display_color");
    private static final Identifier DISPLAY_COLOR_HIGHLIGHT = FairyLights.id("display_color_highlight");

    private TextColor displayColor;

    public ColorButton(final int x, final int y, final Component msg, final Button.OnPress onPress) {
        super(x, y, 20, 20, msg, onPress, DEFAULT_NARRATION);
    }

    public void setDisplayColor(final TextColor color) {
        this.displayColor = color;
    }

    public TextColor getDisplayColor() {
        return this.displayColor;
    }

    public void removeDisplayColor() {
        this.displayColor = null;
    }

    public boolean hasDisplayColor() {
        return this.displayColor != null;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.visible) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.isHovered ? COLOR_SELECTED : COLOR, this.getX(), this.getY(), this.width, this.height);
            if (this.displayColor != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISPLAY_COLOR_HIGHLIGHT, this.getX(), this.getY(), this.width, this.height);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISPLAY_COLOR, this.getX(), this.getY(), this.width, this.height, ARGB.opaque(this.displayColor.getValue()));
            }
        }
    }
}
