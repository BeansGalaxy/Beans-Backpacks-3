package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PickBlock implements Packet2S {
      public static final Type<PickBlock> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":pick_block_s"));
      private final int index;
      private final EquipmentSlot equipmentSlot;

      public PickBlock(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), EquipmentSlotAccess.EQUIPMENT_SLOT_STREAM_CODEC.decode(buf));
      }

      public PickBlock(int index, EquipmentSlot equipmentSlot) {
            this.index = index;
            this.equipmentSlot = equipmentSlot;
      }

      public static void send(int index, EquipmentSlot equipmentSlot) {
            new PickBlock(index, equipmentSlot).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.PICK_BLOCK_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(index);
            EquipmentSlotAccess.EQUIPMENT_SLOT_STREAM_CODEC.encode(buf, equipmentSlot);
      }

      @Override
      public void handle(Player sender) {
            ItemStack backpack = sender.getItemBySlot(equipmentSlot);
            ComponentHolder holder = ComponentHolder.of(backpack);
            ItemStorageTraits.runIfPresent(backpack, trait ->
                        trait.serverPickBlock(holder, index, (ServerPlayer) sender)
            );
      }

      @Override
      public Type<PickBlock> type() {
            return ID;
      }
}
