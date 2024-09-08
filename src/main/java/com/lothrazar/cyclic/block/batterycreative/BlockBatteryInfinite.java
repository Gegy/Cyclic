package com.lothrazar.cyclic.block.batterycreative;

import com.lothrazar.cyclic.block.BlockCyclic;
import com.lothrazar.cyclic.registry.TileRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockBatteryInfinite extends BlockCyclic {

  public BlockBatteryInfinite(Properties properties) {
    super(properties.strength(1.8F));
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new TileBatteryInfinite(pos, state);
  }

  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, TileRegistry.BATTERY_INFINITE.get(), world.isClientSide ? TileBatteryInfinite::clientTick : TileBatteryInfinite::serverTick);
  }
}
