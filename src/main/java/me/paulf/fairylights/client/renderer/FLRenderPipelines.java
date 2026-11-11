package me.paulf.fairylights.client.renderer;

import com.mojang.renderpearl.api.pipeline.BlendFactor;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import me.paulf.fairylights.FairyLights;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class FLRenderPipelines {
    public static final RenderPipeline GUI_HIGHLIGHT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
        .withLocation(FairyLights.id("pipeline/gui_highlight"))
        .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)))
        .build());

    public static final RenderPipeline GUI_TEXTURED_HIGHLIGHT = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(FairyLights.id("pipeline/gui_textured_highlight"))
        .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA)))
        .build());

    public static void init() {
    }
}
