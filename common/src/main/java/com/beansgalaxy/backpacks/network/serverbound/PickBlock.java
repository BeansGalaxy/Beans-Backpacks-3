package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.backpack.BackpackMutable;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PickBlock implements Packet2S {
      public static final Type<PickBlock> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":pick_block_s"));
      private final int index;
      private final EquipmentSlot equipmentSlot;
      private final int amount;
      private final int freeSlot;

      public PickBlock(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), buf.readInt(), buf.readEnum(EquipmentSlot.class), buf.readInt());
      }

      public PickBlock(int index, int amount, EquipmentSlot equipmentSlot, int freeSlot) {
            this.index = index;
            this.amount = amount;
            this.equipmentSlot = equipmentSlot;
            this.freeSlot = freeSlot;
      }

      public static void send(int index, int amount, EquipmentSlot equipmentSlot, int freeSlot) {
            new PickBlock(index, amount, equipmentSlot, freeSlot).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.PICK_BLOCK_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(index);
            buf.writeInt(amount);
            buf.writeEnum(equipmentSlot);
            buf.writeInt(freeSlot);
      }

      @Override
      public void handle(Player sender) {
            ItemStack backpack = sender.getItemBySlot(equipmentSlot);
            ComponentHolder holder = ComponentHolder.of(backpack);
            BackpackTraits traits = BackpackTraits.get(backpack);
            if (traits == null)
                  return;

            BackpackMutable mutable = traits.mutable(holder);
            mutable.pickBlock(sender, index, amount, freeSlot);
      }

      @Override
      public Type<PickBlock> type() {
            return ID;
      }
}
