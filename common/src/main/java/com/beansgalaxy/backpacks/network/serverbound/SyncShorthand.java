package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.network.Network2S;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SyncShorthand implements Packet2S {
      final boolean active;
      final int selection;

      public SyncShorthand(boolean active, int selection) {
            this.active = active;
            this.selection = selection;
      }

      public SyncShorthand(RegistryFriendlyByteBuf buf) {
            this(buf.readBoolean(), buf.readInt());
      }

      public static void send(boolean active, int selection) {
            new SyncShorthand(active, selection).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.SHORTHAND_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(active);
            buf.writeInt(selection);

      }

      @Override
      public void handle(Player sender) {
            Shorthand shorthand = Shorthand.get(sender);
            shorthand.selection = selection;
            shorthand.activateShorthand(active);
      }

      public static Type<SyncShorthand> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":sync_shorthand_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
