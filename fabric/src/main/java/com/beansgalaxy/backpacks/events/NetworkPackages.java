package com.beansgalaxy.backpacks.events;

import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class NetworkPackages {
      public static void registerCommon() {
            for (Network2S value : Network2S.values()) {
                  register2S(value.packet);
            }
            for (Network2C value : Network2C.values()) {
                  registerCommon2C(value.packet);
            }
      }

      public static void registerClient() {
            for (Network2C value : Network2C.values()) {
                  registerClient(value.packet);
            }
      }

      private static <T extends Packet2S> void register2S(Network2S.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, T> packet) {
            PayloadTypeRegistry.playC2S().register(packet.type, packet);
            ServerPlayNetworking.registerGlobalReceiver(packet.type, (payload, context) ->
                        context.server().execute(() -> packet.handle(payload, context.player()))
            );
      }

      private static <T extends Packet2C> void registerClient(Network2C.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, T> packet) {
            ClientPlayNetworking.registerGlobalReceiver(packet.type, (payload, context) ->
                        context.client().execute(() -> packet.handle(payload)));
      }

      private static <T extends Packet2C> void registerCommon2C(Network2C.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, T> packet) {
            PayloadTypeRegistry.playS2C().register(packet.type, packet);
      }
}
