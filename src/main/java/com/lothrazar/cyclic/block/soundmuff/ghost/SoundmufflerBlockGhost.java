package com.lothrazar.cyclic.block.soundmuff.ghost;

import com.lothrazar.cyclic.block.soundmuff.SoundmufflerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoundmufflerBlockGhost extends SoundmufflerBlock {

  public SoundmufflerBlockGhost(Properties properties) {
    super(properties.noOcclusion());
  }

  @Override
  public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    SoundmuffTile tile = (SoundmuffTile) worldIn.getBlockEntity(pos);
    if (tile != null
        && tile.getFacadeState() != null) {
      return tile.getFacadeState().getShape(worldIn, pos, context);
    }
    return super.getShape(state, worldIn, pos, context);
  }

  @Override
  public void registerClient() {}

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SoundmuffTile(pos, state);
  }

  @Override
  public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
    ItemStack stack = player.getItemInHand(handIn);
    SoundmuffTile ent = (SoundmuffTile) world.getBlockEntity(pos);
    if (stack.isEmpty() && handIn == InteractionHand.MAIN_HAND) {
      //try to pull
      ItemStack extracted = ent.notInventory.extractItem(0, 64, false);
      if (!extracted.isEmpty()) {
        //drop it
        player.drop(extracted, true);
      }
    }
    if (Block.byItem(stack.getItem()) == null) {
      return super.use(state, world, pos, player, handIn, hit);
    }
    //replace it
    if (!ent.notInventory.getStackInSlot(0).isEmpty()) {
      //noempty so drop it first
      ItemStack pulled = ent.notInventory.extractItem(0, 64, false);
      player.drop(pulled, true);
    }
    //is it empty so now just replace every time
    ItemStack copy = new ItemStack(stack.getItem(), 1);
    ItemStack insertRemainder = ent.notInventory.insertItem(0, copy, false);
    //success means no remainder, it took 1
    if (insertRemainder.isEmpty()) {
      stack.shrink(1); //eat it!!!
      return InteractionResult.SUCCESS;
    }
    //else do we nuke it? 
    //   
    return super.use(state, world, pos, player, handIn, hit);
  }
}
