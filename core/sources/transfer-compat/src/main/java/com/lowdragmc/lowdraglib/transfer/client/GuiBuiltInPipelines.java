package com.lowdragmc.lowdraglib.transfer.client;

import java.util.List;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

/** Constructed once; actual shader compilation/reloading belongs to the native pipeline lifecycle. */
public final class GuiBuiltInPipelines {
    private static final List<RenderPipeline> PIPELINES = List.of(GuiRoundedBoxState.PIPELINE, GuiRoundedFrameState.PIPELINE,
            GuiProgressBoxState.PIPELINE, GuiCircleState.PIPELINE, GuiRoundLineState.PIPELINE, GuiPanelState.PIPELINE);
    private GuiBuiltInPipelines() {}
    public static List<RenderPipeline> values() { return PIPELINES; }
    public static void initialize() { values(); }
}
