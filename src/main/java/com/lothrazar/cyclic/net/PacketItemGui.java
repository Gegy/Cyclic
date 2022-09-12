package com.lothrazar.cyclic.net;

import java.util.function.Supplier;
import com.lothrazar.cyclic.item.crafting.CraftingBagContainer;
import com.lothrazar.cyclic.item.crafting.CraftingBagContainerProvider;
import com.lothrazar.cyclic.item.crafting.simple.CraftingStickContainer;
import com.lothrazar.cyclic.item.crafting.simple.CraftingStickContainerProvider;
import com.lothrazar.cyclic.item.storagebag.ContainerStorageBag;
import com.lothrazar.cyclic.item.storagebag.StorageBagContainerProvider;
import com.lothrazar.cyclic.registry.ItemRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

public class PacketItemGui extends PacketBaseCyclic {

  private int slot;
  private Item item;

  public PacketItemGui(int slot, Item item) {
    this.slot = slot;
    this.item = item;
  }

  public static void handle(PacketItemGui message, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayer player = ctx.get().getSender();
      if (message.item == ItemRegistry.STORAGE_BAG.get() && (player.containerMenu instanceof ContainerStorageBag) == false) {
        NetworkHooks.openGui(player, new StorageBagContainerProvider(message.slot), buf -> buf.writeInt(message.slot));
      }
      else if (message.item == ItemRegistry.CRAFTING_BAG.get() && (player.containerMenu instanceof CraftingBagContainer) == false) {
        NetworkHooks.openGui(player, new CraftingBagContainerProvider(message.slot), buf -> buf.writeInt(message.slot));
      }
      else if (message.item == ItemRegistry.CRAFTING_STICK.get() && (player.containerMenu instanceof CraftingStickContainer) == false) {
        NetworkHooks.openGui(player, new CraftingStickContainerProvider(message.slot), buf -> buf.writeInt(message.slot));
      }
    });
    message.done(ctx);
  }

  public static PacketItemGui decode(FriendlyByteBuf buf) {
    return new PacketItemGui(buf.readInt(), buf.readItem().getItem());
  }

  public static void encode(PacketItemGui msg, FriendlyByteBuf buf) {
    buf.writeInt(msg.slot);
    buf.writeItem(new ItemStack(msg.item));
  }
}
