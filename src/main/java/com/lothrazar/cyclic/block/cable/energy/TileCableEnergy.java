package com.lothrazar.cyclic.block.cable.energy;

import com.google.common.collect.Maps;
import com.lothrazar.cyclic.ModCyclic;
import com.lothrazar.cyclic.block.cable.EnumConnectType;
import com.lothrazar.cyclic.block.cable.TileCableBase;
import com.lothrazar.cyclic.capability.CustomEnergyStorage;
import com.lothrazar.cyclic.net.PacketEnergySync;
import com.lothrazar.cyclic.registry.PacketRegistry;
import com.lothrazar.cyclic.registry.TileRegistry;
import com.lothrazar.cyclic.util.UtilDirection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileCableEnergy extends TileCableBase implements ITickableTileEntity {

  //
  public static IntValue BUFFERSIZE;
  public static IntValue TRANSFER_RATE;
  //
  final CustomEnergyStorage energy;// = new CustomEnergyStorage(MAX, MAX);
  private final LazyOptional<IEnergyStorage> energyCap;// = LazyOptional.of(() -> energy);
  private final ConcurrentHashMap<Direction, LazyOptional<IEnergyStorage>> flow = new ConcurrentHashMap<>();
  private final Map<Direction, Integer> mapIncomingEnergy = Maps.newHashMap();
  private int energyLastSynced = -1; //fluid tanks have 'onchanged', energy caps do not

  public TileCableEnergy() {
    super(TileRegistry.energy_pipeTile);
    for (Direction f : Direction.values()) {
      mapIncomingEnergy.put(f, 0);
    }
    energy = new CustomEnergyStorage(BUFFERSIZE.get(), TRANSFER_RATE.get());
    energyCap = LazyOptional.of(() -> energy);
  }

  @Override
  public void updateConnection(final Direction side, final EnumConnectType connectType) {
    final EnumConnectType oldConnectType = getConnectionType(side);
    if (connectType == EnumConnectType.BLOCKED && oldConnectType != EnumConnectType.BLOCKED) {
      final LazyOptional<IEnergyStorage> sidedCap = flow.get(side);
      if (sidedCap != null) {
        sidedCap.invalidate();
        flow.remove(side);
      }
    }
    else if (oldConnectType == EnumConnectType.BLOCKED && connectType != EnumConnectType.BLOCKED) {
      flow.put(side, LazyOptional.of(() -> energy));
    }
    super.updateConnection(side, connectType);
  }

  @Override
  public void tick() {
    this.syncEnergy();
    this.tickDownIncomingPowerFaces();
    this.tickCableFlow();
    for (final Direction extractSide : Direction.values()) {
      final EnumConnectType connection = getConnectionType(extractSide);
      if (connection.isExtraction()) {
        tryExtract(extractSide);
      }
    }
  }

  private void tryExtract(Direction extractSide) {
    if (extractSide == null) {
      return;
    }
    final BlockPos posTarget = this.pos.offset(extractSide);
    final TileEntity tile = world.getTileEntity(posTarget);
    if (tile == null) {
      return;
    }
    final IEnergyStorage itemHandlerFrom = tile
        .getCapability(CapabilityEnergy.ENERGY, extractSide.getOpposite())
        .orElse(null);
    if (itemHandlerFrom == null) {
      return;
    }
    final int capacity = energy.getMaxEnergyStored() - energy.getEnergyStored();
    if (capacity <= 0) {
      return;
    }
    //first we simulate
    final int energyToExtract = itemHandlerFrom.extractEnergy(capacity, true);
    if (energyToExtract <= 0) {
      return;
    }
    final int energyReceived = energy.receiveEnergy(energyToExtract, false);
    if (energyReceived <= 0) {
      return;
    }
    final int energyExtracted = itemHandlerFrom.extractEnergy(energyReceived, false);
    //sanity check
    if (energyExtracted != energyReceived) {
      ModCyclic.LOGGER.error("Imbalance extracting energy, extracted " + energyExtracted + " received " + energyReceived);
    }
  }

  private void tickCableFlow() {
    for (final Direction outgoingSide : UtilDirection.getAllInDifferentOrder()) {
      final EnumConnectType connection = getConnectionType(outgoingSide);
      if (connection.isExtraction() || connection.isBlocked()) {
        continue;
      }
      if (!this.isEnergyIncomingFromFace(outgoingSide)) {
        moveEnergy(outgoingSide, TRANSFER_RATE.get());
      }
    }
  }

  public void tickDownIncomingPowerFaces() {
    for (final Direction incomingDirection : Direction.values()) {
      mapIncomingEnergy.computeIfPresent(incomingDirection, (direction, amount) -> {
        if (amount > 0) {
          amount -= 1;
        }
        return amount;
      });
    }
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
    if (cap == CapabilityEnergy.ENERGY) {
      if (side == null) {
        return energyCap.cast();
      }
      LazyOptional<IEnergyStorage> sidedCap = flow.get(side);
      if (sidedCap == null) {
        if (getConnectionType(side) != EnumConnectType.BLOCKED) {
          sidedCap = LazyOptional.of(() -> energy);
          flow.put(side, sidedCap);
          return sidedCap.cast();
        }
      }
      else {
        return sidedCap.cast();
      }
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void invalidateCaps() {
    energyCap.invalidate();
    for (final LazyOptional<IEnergyStorage> sidedCap : flow.values()) {
      sidedCap.invalidate();
    }
    super.invalidateCaps();
  }

  @Override
  public void read(BlockState bs, CompoundNBT tag) {
    for (Direction f : Direction.values()) {
      mapIncomingEnergy.put(f, tag.getInt(f.getString() + "_incenergy"));
    }
    energy.deserializeNBT(tag.getCompound(NBTENERGY));
    super.read(bs, tag);
  }

  @Override
  public CompoundNBT write(CompoundNBT tag) {
    for (Direction f : Direction.values()) {
      tag.putInt(f.getString() + "_incenergy", mapIncomingEnergy.get(f));
    }
    tag.put(NBTENERGY, energy.serializeNBT());
    return super.write(tag);
  }

  private static final int TIMER_SIDE_INPUT = 15;

  private boolean isEnergyIncomingFromFace(Direction face) {
    return mapIncomingEnergy.get(face) > 0;
  }

  public void updateIncomingEnergyFace(Direction inputFrom) {
    mapIncomingEnergy.put(inputFrom, TIMER_SIDE_INPUT);
  }

  @Override
  public void setField(int field, int value) {}

  @Override
  public int getField(int field) {
    return 0;
  }

  @Override
  protected void syncEnergy() {
    //skip if clientside
    if (world.isRemote || world.getGameTime() % 20 != 0) {
      return;
    }
    final int currentEnergy = energy.getEnergyStored();
    if (currentEnergy != energyLastSynced) {
      final PacketEnergySync packetEnergySync = new PacketEnergySync(this.getPos(), currentEnergy);
      PacketRegistry.sendToAllClients(world, packetEnergySync);
      energyLastSynced = currentEnergy;
    }
  }
}
