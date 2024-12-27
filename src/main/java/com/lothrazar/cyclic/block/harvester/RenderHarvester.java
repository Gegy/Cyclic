package com.lothrazar.cyclic.block.harvester;

import com.lothrazar.cyclic.render.PreviewOutlineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RenderHarvester implements BlockEntityRenderer<TileHarvester> {

  public RenderHarvester(BlockEntityRendererProvider.Context d) {}

  @Override
  public void render(TileHarvester te, float v, PoseStack matrix, MultiBufferSource ibuffer, int partialTicks, int destroyStage) {
    PreviewOutlineRenderer.render(matrix, te, te.getField(TileHarvester.Fields.RENDER.ordinal()), te.getShape());
  }
}
