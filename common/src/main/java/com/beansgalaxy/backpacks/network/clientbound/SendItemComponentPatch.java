package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2C;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SendItemComponentPatch implements Packet2C {
      private final int slot;
      private final DataComponentPatch patch;

      public SendItemComponentPatch(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), DataComponentPatch.STREAM_CODEC.decode(buf));
      }

      private SendItemComponentPatch(int slot, DataComponentPatch patch) {
            this.slot = slot;
            this.patch = patch;
      }

      public static void send(ServerPlayer player, int slot, ItemStack stack) {
            new SendItemComponentPatch(slot, stack.getComponentsPatch()).send2C(player);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.INVENTORY_ITEM_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(slot);
            DataComponentPatch.STREAM_CODEC.encode(buf, patch);
      }

      @Override
      public void handle() {
            CommonClient.handleSentItemComponentPatch(slot, patch);
      }

      public static Type<SendItemComponentPatch> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":send_item_component_patch_c"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
