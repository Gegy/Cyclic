package com.lothrazar.cyclic.render;

import com.lothrazar.cyclic.config.ClientConfigCyclic;
import com.lothrazar.cyclic.data.PreviewOutlineType;
import com.lothrazar.cyclic.util.BlockShape;
import com.lothrazar.library.util.RenderBlockUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class PreviewOutlineRenderer {
  public static void render(PoseStack poseStack, BlockEntity blockEntity, int previewType, BlockShape shape) {
    if (PreviewOutlineType.SHADOW.ordinal() == previewType) {
      RenderBlockUtils.renderOutline(blockEntity.getBlockPos(), shape.visualBlocks(), poseStack, 0.9F, ClientConfigCyclic.getColor(blockEntity));
    }
    else if (PreviewOutlineType.WIREFRAME.ordinal() == previewType) {
      for (BlockPos crd : shape.visualBlocks()) {
        RenderBlockUtils.createBox(poseStack, crd, Vec3.atLowerCornerOf(blockEntity.getBlockPos()));
      }
    }
  }
}
