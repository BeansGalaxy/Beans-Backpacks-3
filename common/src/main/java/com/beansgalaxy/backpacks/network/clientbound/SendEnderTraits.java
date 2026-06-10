package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.network.Network2C;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SendEnderTraits implements Packet2C {
      private final UUID owner;
      private final ResourceLocation location;
      private final Reference2ObjectOpenHashMap<DataComponentType<?>, Object> map;

      public SendEnderTraits(RegistryFriendlyByteBuf buf) {
            this.owner = buf.readUUID();
            location = ResourceLocation.STREAM_CODEC.decode(buf);

            map = EnderStorage.ENTRY_MAP_STREAM_CODEC.decode(buf);
      }

      private SendEnderTraits(UUID owner, ResourceLocation location, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> map) {
            this.owner = owner;
            this.location = location;
            this.map = map;
      }

      public static void send(ServerPlayer sender, UUID owner, ResourceLocation location) {
            Reference2ObjectOpenHashMap<DataComponentType<?>, Object> traits = EnderStorage.get(sender.level()).get(owner, location);
            new SendEnderTraits(owner, location, traits).send2C(sender);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.ENDER_TRAIT_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(owner);
            ResourceLocation.STREAM_CODEC.encode(buf, location);

            EnderStorage.ENTRY_MAP_STREAM_CODEC.encode(buf, map);
      }

      @Override
      public void handle() {
            CommonClient.getEnderStorage().set(owner, location, map);
      }

      public static Type<SendEnderTraits> ID = new Type<>(Constants.defaultLocation("send_ender_traits_c"));

      @Override
      public Type<SendEnderTraits> type() {
            return ID;
      }
}
