package com.lothrazar.cyclic.block.wireless.redstone;

import com.lothrazar.cyclic.ModCyclic;
import com.lothrazar.cyclic.base.TileEntityBase;
import com.lothrazar.cyclic.config.ConfigRegistry;
import com.lothrazar.cyclic.data.BlockPosDim;
import com.lothrazar.cyclic.data.PreviewOutlineType;
import com.lothrazar.cyclic.item.datacard.LocationGpsCard;
import com.lothrazar.cyclic.registry.TileRegistry;
import com.lothrazar.cyclic.util.UtilWorld;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileWirelessTransmit extends TileEntityBase implements INamedContainerProvider, ITickableTileEntity {

  private static final String REDSTONE_ID = "redstone_id";

  static enum Fields {
    RENDER;
  }

  public TileWirelessTransmit() {
    super(TileRegistry.wireless_transmitter);
  }

  ItemStackHandler inventory = new ItemStackHandler(9) {

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return stack.getItem() instanceof LocationGpsCard;
    }
  };
  private LazyOptional<IItemHandler> inventoryCap = LazyOptional.of(() -> inventory);
  private UUID id;

  @Override
  public ITextComponent getDisplayName() {
    return new StringTextComponent(getType().getRegistryName().getPath());
  }

  @Override
  public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
    return new ContainerTransmit(i, world, pos, playerInventory, playerEntity);
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      return inventoryCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    inventoryCap.invalidate();
    super.invalidateCaps();
  }

  @Override
  public void read(BlockState bs, CompoundNBT tag) {
    inventory.deserializeNBT(tag.getCompound(NBTINV));
    if (tag.hasUniqueId(REDSTONE_ID)) {
      this.id = tag.getUniqueId(REDSTONE_ID);
    }
    else {
      this.id = UUID.randomUUID();
    }
    super.read(bs, tag);
  }

  @Override
  public CompoundNBT write(CompoundNBT tag) {
    tag.put(NBTINV, inventory.serializeNBT());
    if (this.id == null) {
      this.id = UUID.randomUUID();
    }
    tag.putUniqueId(REDSTONE_ID, id);
    return super.write(tag);
  }

  @SuppressWarnings("deprecation")
  private void toggleTarget(BlockPosDim dimPos) {
    if (dimPos == null || world.isRemote) {
      return;
    }
    BlockPos targetPos = dimPos.getPos();
    ServerWorld serverLevel = dimPos.getServerLevel(world.getServer()); // world.getServer().getWorld(UtilWorld.stringToDimension(dimPos.getDimension()));
    if (serverLevel == null) {
      ModCyclic.LOGGER.info("Dimension not found " + dimPos.getDimension());
      return;
    }
    if (!serverLevel.isAreaLoaded(targetPos, targetPos)) {
      ModCyclic.LOGGER.info("DimPos is unloaded" + dimPos);
      return;
    }
    boolean isPowered = world.isBlockPowered(pos);
    if (serverLevel.getTileEntity(targetPos) instanceof TileWirelessRec) {
      TileWirelessRec receiver = (TileWirelessRec) serverLevel.getTileEntity(targetPos);
      //am I powered?
      if (isPowered) {
        //    ModCyclic.LOGGER.info(" POWER UP target" + dimPos);
        receiver.putPowerSender(this.id);
      }
      else {
        //  ModCyclic.LOGGER.info(" turn off target" + dimPos);
        receiver.removePowerSender(this.id);
      }
    }
    world.setBlockState(pos, world.getBlockState(pos).with(BlockStateProperties.POWERED, isPowered));
  }

  @Override
  public void tick() {
    for (int s = 0; s < inventory.getSlots(); s++) {
      BlockPosDim targetPos = getTargetInSlot(s);
      if (!UtilWorld.dimensionIsEqual(targetPos, world) && !ConfigRegistry.TRANSFER_NODES_DIMENSIONAL.get()) {
        //if dimensions dont match, AND config disables x-dimension communication, then skip this one
        continue;
      }
      //else gogogo
      toggleTarget(targetPos);
    }
  }

  BlockPosDim getTargetInSlot(int s) {
    ItemStack stack = inventory.getStackInSlot(s);
    return LocationGpsCard.getPosition(stack);
  }

  @Override
  public void setField(int field, int value) {
    switch (Fields.values()[field]) {
      case RENDER:
        this.render = value % PreviewOutlineType.values().length;
      break;
    }
  }

  @Override
  public int getField(int field) {
    switch (Fields.values()[field]) {
      case RENDER:
        return render;
    }
    return 0;
  }

  public float getRed() {
    return 0.89F;
  }

  public float getBlue() {
    return 0;
  }

  public float getGreen() {
    return 0.12F;
  }

  public float getAlpha() {
    return 0.9F;
  }

  public float getThick() {
    return 0.065F;
  }
}
