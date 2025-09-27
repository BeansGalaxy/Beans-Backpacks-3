package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TinyChestClick implements Packet2S {
      private final int containerId;
      private final int containerSlot;
      private final int index;
      private final TinyClickType clickType;

      public TinyChestClick(RegistryFriendlyByteBuf buf) {
            this.containerId = buf.readInt();
            this.containerSlot = buf.readInt();
            this.index = buf.readInt();
            clickType = buf.readEnum(TinyClickType.class);
      }

      public TinyChestClick(int containerId, int containerSlot, int index, TinyClickType clickType) {
            this.containerId = containerId;
            this.containerSlot = containerSlot;
            this.index = index;
            this.clickType = clickType;
      }

      public static void send(int containerId, Slot slot, int index, TinyClickType clickType) {
            new TinyChestClick(containerId, slot.index, index, clickType).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TINY_SUB_CHEST_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(containerId);
            buf.writeInt(containerSlot);
            buf.writeInt(index);
            buf.writeEnum(clickType);
      }

      @Override
      public void handle(Player sender) {
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu.containerId != containerId)
                  return;

            Slot slot = menu.getSlot(containerSlot);
            ComponentHolder holder = ComponentHolder.of(slot);
            Optional<ChestTraits> optional = ChestTraits.get(holder);
            ChestTraits chestTraits;
            if (optional.isEmpty()) {
                  Optional<EnderTraits> optionalEnder = EnderTraits.get(slot.getItem());
                  if (optionalEnder.isEmpty())
                        return;

                  EnderTraits enderTraits = optionalEnder.get();
                  GenericTraits trait = enderTraits.getTrait(sender.level());
                  if (trait instanceof ChestTraits chest) {
                        holder = enderTraits;
                        chestTraits = chest;
                  } else
                        return;
            }
            else {
                  chestTraits = optional.get();
            }


            SlotAccess carriedAccess = new SlotAccess() {
                  public ItemStack get() {
                        return menu.getCarried();
                  }

                  public boolean set(ItemStack p_150452_) {
                        menu.setCarried(p_150452_);
                        return true;
                  }
            };

            chestTraits.tinySubMenuClick(holder, index, clickType, carriedAccess, sender);
      }

      public static Type<TinyChestClick> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":tiny_sub_chest_click_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
