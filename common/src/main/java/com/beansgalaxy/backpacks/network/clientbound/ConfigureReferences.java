package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.reference.ReferenceRegistry;
import com.beansgalaxy.backpacks.network.Network2C;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class ConfigureReferences implements Packet2C {
      final Map<ResourceLocation, ReferenceRegistry> references;

      private ConfigureReferences(Map<ResourceLocation, ReferenceRegistry> references) {
            this.references = references;
      }

      public ConfigureReferences(RegistryFriendlyByteBuf buf) {
            int size = buf.readInt();
            HashMap<ResourceLocation, ReferenceRegistry> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                  ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(buf);
                  ReferenceRegistry reference = ReferenceRegistry.STREAM_CODEC.decode(buf);
                  map.put(location, reference);
            }

            this.references = map;
      }

      public static void send(ServerPlayer player) {
            new ConfigureReferences(ReferenceRegistry.REFERENCES).send2C(player);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.CONFIG_REFERENCES_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            int size = references.size();
            buf.writeInt(size);
            references.forEach(((location, reference) -> {
                  ResourceLocation.STREAM_CODEC.encode(buf, location);
                  ReferenceRegistry.STREAM_CODEC.encode(buf, reference);
            }));
      }

      @Override
      public void handle() {
            ReferenceRegistry.REFERENCES.clear();
            references.forEach(ReferenceRegistry::put);
      }

      public static Type<ConfigureReferences> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":config_references_c"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
