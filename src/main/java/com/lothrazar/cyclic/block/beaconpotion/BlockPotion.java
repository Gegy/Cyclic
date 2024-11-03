package com.lothrazar.cyclic.block.beaconpotion;

import com.lothrazar.cyclic.base.BlockBase;
import com.lothrazar.cyclic.registry.ContainerScreenRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.fluid.FluidState;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;

public class BlockPotion extends BlockBase {

  public BlockPotion(Properties properties) {
    super(properties.hardnessAndResistance(1.8F)
        .notSolid()//trying to get transparency
    );
    this.setHasGui();
  }

  @Override
  public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos) {
    return false;
  }

  @Override
  public boolean shouldDisplayFluidOverlay(BlockState state, IBlockDisplayReader world, BlockPos pos, FluidState fluidState) {
    return true;
  }

  @Override
  public void registerClient() {
    RenderTypeLookup.setRenderLayer(this, RenderType.getCutoutMipped());
    ScreenManager.registerFactory(ContainerScreenRegistry.BEACON, ScreenPotion::new);
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TilePotionBeacon();
  }

  @Override
  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(LIT);
  }
}
