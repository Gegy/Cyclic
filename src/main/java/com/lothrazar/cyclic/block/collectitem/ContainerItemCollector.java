package com.lothrazar.cyclic.block.collectitem;

import com.lothrazar.cyclic.base.ContainerBase;
import com.lothrazar.cyclic.data.Const;
import com.lothrazar.cyclic.registry.BlockRegistry;
import com.lothrazar.cyclic.registry.ContainerScreenRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerItemCollector extends ContainerBase {

  TileItemCollector tile;

  public ContainerItemCollector(int windowId, World world, BlockPos pos, PlayerInventory playerInventory, PlayerEntity player) {
    super(ContainerScreenRegistry.collector, windowId);
    tile = (TileItemCollector) world.getTileEntity(pos);
    this.playerEntity = player;
    this.playerInventory = playerInventory;
    this.endInv = tile.inventory.getSlots();
    final int numRows = 2;
    for (int j = 0; j < numRows; ++j) {
      for (int k = 0; k < 9; ++k) {
        this.addSlot(new SlotItemHandler(tile.inventory,
            k + j * 9,
            8 + k * Const.SQ,
            82 + j * Const.SQ));
      }
    }
    addSlot(new SlotItemHandler(tile.filter, 0, 152, 9));
    layoutPlayerInventorySlots(8, 132);
    this.trackAllIntFields(tile, TileItemCollector.Fields.values().length);
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    return isWithinUsableDistance(IWorldPosCallable.of(tile.getWorld(), tile.getPos()), playerEntity, BlockRegistry.collector);
  }
}
