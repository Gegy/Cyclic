package com.lothrazar.cyclic.block.peatfarm;

import com.lothrazar.cyclic.render.PreviewOutlineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RenderPeatFarm implements BlockEntityRenderer<TilePeatFarm> {

  public RenderPeatFarm(BlockEntityRendererProvider.Context d) {}

  @Override
  public void render(TilePeatFarm te, float v, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int partialTicks, int destroyStage) {
    PreviewOutlineRenderer.render(matrixStack, te, te.getField(TilePeatFarm.Fields.RENDER.ordinal()), te.getOuterShape());
  }
}
