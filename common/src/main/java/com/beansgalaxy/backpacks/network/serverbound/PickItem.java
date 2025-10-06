package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.backpack.BackpackMutable;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class PickItem implements Packet2S {
      private final int containerId;
      private final int index;
      private final int amount;
      private final EquipmentSlot slot;

      public PickItem(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), buf.readInt(), buf.readInt(), buf.readEnum(EquipmentSlot.class));
      }

      public PickItem(int containerId, int index, int amount, EquipmentSlot slot) {
            this.containerId = containerId;
            this.index = index;
            this.amount = amount;
            this.slot = slot;
      }


      public static void send(int containerId, int index, int amount, EquipmentSlot slot) {
            new PickItem(containerId, index, amount, slot).send2S();
      }

      @Override public Network2S getNetwork() {
            return Network2S.INSTANT_KEY_2S;
      }

      @Override public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(containerId);
            buf.writeInt(index);
            buf.writeInt(amount);
            buf.writeEnum(slot);
      }

      @Override public void handle(Player sender) {
            AbstractContainerMenu menu = sender.containerMenu;
            if (menu.containerId != containerId)
                  return;

            ItemStack backpack = sender.getItemBySlot(slot);
            BackpackTraits traits = BackpackTraits.get(backpack);
            if (traits == null)
                  return;

            BackpackMutable mutable = traits.mutable(ComponentHolder.of(backpack));
            SlotAccess access = SlotAccess.of(menu::getCarried, menu::setCarried);

            mutable.pickItem(index, amount, access);
      }

      public static Type<PickItem> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":pick_item_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
