package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.network.Network2S;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SyncShorthand implements Packet2S {
      final int selection;

      public SyncShorthand(int selection) {
            this.selection = selection;
      }

      public SyncShorthand(RegistryFriendlyByteBuf buf) {
            this(buf.readInt());
      }

      public static void send(int selection) {
            new SyncShorthand(selection).send2S();
      }

      public static void send(Shorthand shorthand) {
            send(shorthand.selection);
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.SHORTHAND_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(selection);

      }

      @Override
      public void handle(Player sender) {
            Shorthand shorthand = Shorthand.get(sender);
            shorthand.selection = selection;
      }

      public static Type<SyncShorthand> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":sync_shorthand_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
