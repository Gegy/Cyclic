package com.lothrazar.cyclic.block.collectfluid;

import com.lothrazar.cyclic.render.PreviewOutlineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RenderFluidCollect implements BlockEntityRenderer<TileFluidCollect> {

  public RenderFluidCollect(BlockEntityRendererProvider.Context d) {}

  @Override
  public void render(TileFluidCollect te, float v, PoseStack matrix, MultiBufferSource ibuffer, int partialTicks, int destroyStage) {
    PreviewOutlineRenderer.render(matrix, te, te.getField(TileFluidCollect.Fields.RENDER.ordinal()), te.getShape());
  }
}
