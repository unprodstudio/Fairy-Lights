package me.paulf.fairylights.client.gui.component;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.gui.EditLetteredConnectionScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ToggleButton extends Button {
    private boolean value;
    private boolean pressed;

    private final Identifier normal;
    private final Identifier hover;
    private final Identifier toggled;

    public ToggleButton(final int x, final int y, String name, final Component msg, final Button.OnPress pressable) {
        super(x, y, 20, 20, msg, pressable, DEFAULT_NARRATION);
        this.normal = FairyLights.id("style/" + name);
        this.hover = this.normal.withSuffix("_hover");
        this.toggled = this.normal.withSuffix("_toggled");
    }

    public void setValue(final boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.value = !this.value;
        this.pressed = true;
        super.onPress(input);
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.pressed = false;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (this.visible) {
            final Identifier sprite;
            if (this.isHovered) {
                if (this.pressed) {
                    sprite = this.toggled;
                } else {
                    sprite = this.hover;
                }
            } else {
                if (this.value) {
                    sprite = this.toggled;
                } else {
                    sprite = this.normal;
                }
            }
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);
        }
    }
}
