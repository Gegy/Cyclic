package com.lothrazar.cyclic.block.cable.item;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import com.lothrazar.cyclic.block.cable.CableBase;
import com.lothrazar.cyclic.block.cable.EnumConnectType;
import com.lothrazar.cyclic.block.cable.TileCableBase;
import com.lothrazar.cyclic.registry.BlockRegistry;
import com.lothrazar.cyclic.registry.ItemRegistry;
import com.lothrazar.cyclic.registry.TileRegistry;
import com.lothrazar.cyclic.util.UtilDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileCableItem extends TileCableBase implements MenuProvider {

  private static final Direction[] DIRECTIONS = Direction.values();

  private static final int FLOW_QTY = 64; // fixed, for non-extract motion
  private int extractQty = FLOW_QTY; // default
  ItemStackHandler filter = new ItemStackHandler(1) {

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
      return stack.getItem() == ItemRegistry.FILTER_DATA.get();
    }
  };
  private final Map<Direction, LazyOptional<IItemHandler>> flow = new EnumMap<>(Direction.class);

  // Cached from BlockState - property lookups are fairly expensive
  private final EnumSet<Direction> pushToSides = EnumSet.noneOf(Direction.class);
  private final EnumSet<Direction> extractSides = EnumSet.noneOf(Direction.class);
  private final EnumSet<Direction> blockedSides = EnumSet.noneOf(Direction.class);

  public TileCableItem(BlockPos pos, BlockState state) {
    super(TileRegistry.ITEM_PIPE.get(), pos, state);
    for (Direction f : Direction.values()) {
      flow.put(f, LazyOptional.of(TileCableItem::createHandler));
    }
    onBlockStateSet(state);
  }

  public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableItem e) {
    e.tick();
  }

  public static <E extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableItem e) {
    e.tick();
  }

  private static ItemStackHandler createHandler() {
    return new ItemStackHandler(1);
  }

  @Override
  public void setBlockState(BlockState state) {
    super.setBlockState(state);
    onBlockStateSet(state);
  }

  private void onBlockStateSet(BlockState state) {
    pushToSides.clear();
    extractSides.clear();
    blockedSides.clear();
    for (Direction direction : DIRECTIONS) {
      EnumProperty<EnumConnectType> property = CableBase.FACING_TO_PROPERTY_MAP.get(direction);
      if (!state.hasProperty(property)) {
        // Should never get here with a non-cable state, but this is a Vanilla bug fixed in 1.21.1+
        continue;
      }
      EnumConnectType connection = state.getValue(property);
      if (!connection.isExtraction() && connection.isConnected()) {
        pushToSides.add(direction);
      }
      if (connection.isExtraction()) {
        extractSides.add(direction);
      }
      if (connection.isBlocked()) {
        blockedSides.add(direction);
      }
    }
  }

  public void tick() {
    for (Direction extractSide : extractSides) {
      final IItemHandler sideHandler = flow.get(extractSide).orElse(null);
      tryExtract(sideHandler, extractSide, extractQty, filter);
    }
    normalFlow();
  }

  private void normalFlow() {
    for (final Direction incomingSide : DIRECTIONS) {
      moveItemsOnSide(incomingSide);
    }
  }

  private void moveItemsOnSide(Direction incomingSide) {
    final IItemHandler sideHandler = flow.get(incomingSide).orElse(null);
    if (sideHandler.getStackInSlot(0).isEmpty()) {
      return;
    }
    for (final Direction outgoingSide : UtilDirection.getAllInDifferentOrder()) {
      if (outgoingSide == incomingSide || !pushToSides.contains(outgoingSide)) {
        continue;
      }
      if (this.moveItems(outgoingSide, FLOW_QTY, sideHandler)) {
        return;
      }
    }
    //if no items have been moved then move items in from adjacent
    this.moveItems(incomingSide, FLOW_QTY, sideHandler);
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
    if (side != null && cap == ForgeCapabilities.ITEM_HANDLER) {
      if (!blockedSides.contains(side)) {
        return flow.get(side).cast();
      }
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    super.invalidateCaps();
    for (final LazyOptional<IItemHandler> sidedCap : flow.values()) {
      sidedCap.invalidate();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void load(CompoundTag tag) {
    extractQty = tag.getInt("extractCount");
    LazyOptional<IItemHandler> item;
    for (Direction f : Direction.values()) {
      item = flow.get(f);
      item.ifPresent(h -> {
        CompoundTag itemTag = tag.getCompound("item" + f.toString());
        ((INBTSerializable<CompoundTag>) h).deserializeNBT(itemTag);
      });
    }
    filter.deserializeNBT(tag.getCompound("filter"));
    super.load(tag);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void saveAdditional(CompoundTag tag) {
    tag.put("filter", filter.serializeNBT());
    tag.putInt("extractCount", extractQty);
    LazyOptional<IItemHandler> item;
    for (Direction f : Direction.values()) {
      item = flow.get(f);
      item.ifPresent(h -> {
        CompoundTag compound = ((INBTSerializable<CompoundTag>) h).serializeNBT();
        tag.put("item" + f.toString(), compound);
      });
    }
    super.saveAdditional(tag);
  }

  @Override
  public void setField(int field, int value) {
    this.extractQty = value;
  }

  @Override
  public int getField(int field) {
    return this.extractQty;
  }

  @Override
  public Component getDisplayName() {
    return BlockRegistry.ITEM_PIPE.get().getName();
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableItem(i, level, worldPosition, playerInventory, playerEntity);
  }
}
