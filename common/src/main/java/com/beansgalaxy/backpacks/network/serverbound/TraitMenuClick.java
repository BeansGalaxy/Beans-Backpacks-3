package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TraitMenuClick implements Packet2S {
      private final int containerId;
      private final int containerSlot;
      private final int index;
      private final Kind clickType;

      public TraitMenuClick(RegistryFriendlyByteBuf buf) {
            this.containerId = buf.readInt();
            this.containerSlot = buf.readInt();
            this.index = buf.readInt();
            clickType = buf.readEnum(Kind.class);
      }

      public TraitMenuClick(int containerId, int containerSlot, int index, Kind clickType) {
            this.containerId = containerId;
            this.containerSlot = containerSlot;
            this.index = index;
            this.clickType = clickType;
      }

      public static void send(int containerId, Slot slot, int index, Kind clickType) {
            new TraitMenuClick(containerId, slot.index, index, clickType).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TRAIT_MENU_CLICK;
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
            PatchedComponentHolder holder = PatchedComponentHolder.of(slot);
            Optional<GenericTraits> optional = Traits.get(holder);
            GenericTraits traits;
            if (optional.isEmpty()) {
                  Optional<EnderTraits> optionalEnder = EnderTraits.get(slot.getItem());
                  if (optionalEnder.isEmpty())
                        return;

                  EnderTraits enderTraits = optionalEnder.get();
                  traits = enderTraits.getTrait(sender.level());
            }
            else {
                  traits = optional.get();
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

            traits.menuClick(holder, index, clickType, carriedAccess, sender);
      }

      public static Type<TraitMenuClick> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":trait_menu_click_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }

      public enum Kind {
            LEFT,
            RIGHT,
            SHIFT,
            ACTION,
            DROP
            ;

            public boolean isShift() {
                  return this == SHIFT;
            }

            public boolean isAction() {
                  return this == ACTION;
            }

            public boolean isDrop() {
                  return this == DROP;
            }

            public boolean isRight() {
                  return this == RIGHT;
            }
      }
}
