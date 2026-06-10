package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.platform.Services;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public interface Packet2C extends CustomPacketPayload {
      Network2C getNetwork();

      default void send2C(ServerPlayer to) {
            Network2C network = getNetwork();
            network.debugMsgEncode();
            Services.PLATFORM.send(network, this, to);
      }

      default void send2A(MinecraftServer server) {
            Network2C network = getNetwork();
            network.debugMsgEncode();
            Services.PLATFORM.send(network, this, server);
      }

      default void send2A(ServerPlayer player) {
            Network2C network = getNetwork();
            network.debugMsgEncode();
            Services.PLATFORM.send(network, this, player.level().getServer(), player);
      }

      void encode(RegistryFriendlyByteBuf buf);

      void handle();
}
