package com.lothrazar.cyclic.block.miner;

import com.lothrazar.cyclic.render.PreviewOutlineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RenderMiner implements BlockEntityRenderer<TileMiner> {

  public RenderMiner(BlockEntityRendererProvider.Context d) {}

  @Override
  public void render(TileMiner te, float v, PoseStack matrixStack, MultiBufferSource iRenderTypeBuffer, int partialTicks, int destroyStage) {
    PreviewOutlineRenderer.render(matrixStack, te, te.getField(TileMiner.Fields.RENDER.ordinal()), te.getShape());
  }
}
