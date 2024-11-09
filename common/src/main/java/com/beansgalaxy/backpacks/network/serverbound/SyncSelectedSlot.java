package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SyncSelectedSlot implements Packet2S {
      final int containerId;
      final int slotIndex;
      final int selectedSlot;

      private SyncSelectedSlot(int containerId, int slotIndex, int selectedSlot) {
            this.containerId = containerId;
            this.slotIndex = slotIndex;
            this.selectedSlot = selectedSlot;
      }

      public SyncSelectedSlot(RegistryFriendlyByteBuf buf) {
            this.containerId = buf.readInt();
            this.slotIndex = buf.readInt();
            this.selectedSlot = buf.readInt();
      }

      public static void send(int containerId, int slotIndex, int selectedSlot) {
            new SyncSelectedSlot(containerId, slotIndex, selectedSlot).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.SYNC_SELECTED_SLOT_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(containerId);
            buf.writeInt(slotIndex);
            buf.writeInt(selectedSlot);
      }

      @Override
      public void handle(Player sender) {
            AbstractContainerMenu containerMenu = sender.containerMenu;
            if (containerMenu.containerId != containerId) {
                  return;
            }

            Slot slot = containerMenu.getSlot(slotIndex);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                  return;
            }

            EnderTraits enderTraits = stack.get(Traits.ENDER);
            PatchedComponentHolder holder;
            if (enderTraits != null) {
                  holder = enderTraits;
                  enderTraits.reload(sender.level());
            }
            else holder = PatchedComponentHolder.of(stack);

            SlotSelection slotSelection = holder.get(ITraitData.SLOT_SELECTION);
            if (slotSelection == null) {
                  slotSelection = new SlotSelection();
                  holder.set(ITraitData.SLOT_SELECTION, slotSelection);
            }

            slotSelection.setSelectedSlot(sender, selectedSlot);
      }

      public static Type<SyncSelectedSlot> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":sync_selected_slot_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
