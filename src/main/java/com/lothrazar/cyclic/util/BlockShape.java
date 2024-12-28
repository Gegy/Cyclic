package com.lothrazar.cyclic.util;

import com.lothrazar.library.util.ShapeUtil;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public record BlockShape(List<BlockPos> blocks, List<BlockPos> visualBlocks) {
  public BlockShape {
    blocks = List.copyOf(blocks);
    visualBlocks = List.copyOf(visualBlocks);
  }

  public static BlockShape create(BlockPos center, int radius, int heightWithDirection, @Nullable BlockPos visualTargetPos) {
    List<BlockPos> blocks = ShapeUtil.squareHorizontalFull(center, radius);
    List<BlockPos> visualBlocks = ShapeUtil.squareHorizontalHollow(center, radius);
    if (heightWithDirection != 0) {
      blocks = ShapeUtil.repeatShapeByHeight(blocks, heightWithDirection);
      visualBlocks = ShapeUtil.repeatShapeByHeight(visualBlocks, heightWithDirection);
    }
    if (visualTargetPos != null) {
      visualBlocks.add(visualTargetPos);
    }
    return new BlockShape(blocks, visualBlocks);
  }
}
