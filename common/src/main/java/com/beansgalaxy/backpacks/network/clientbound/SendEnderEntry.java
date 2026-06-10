package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class SendEnderEntry<T extends GenericTraits> implements Packet2C {
      private final Reference2ObjectOpenHashMap<DataComponentType<?>, Object> map;
      private final TraitComponentKind<T> kind;
      private final T trait;
      private final ResourceLocation location;
      private final Component name;
      private final UUID owner;

      public static SendEnderEntry<?> decode(RegistryFriendlyByteBuf buf) {
            return new SendEnderEntry<>(
                        buf.readUUID(),
                        ResourceLocation.STREAM_CODEC.decode(buf),
                        TraitComponentKind.STREAM_CODEC.decode(buf)
                                    .streamCodec().decode(buf),
                        EnderStorage.ENTRY_MAP_STREAM_CODEC.decode(buf),
                        ComponentSerialization.STREAM_CODEC.decode(buf)
            );
      }

      public SendEnderEntry(UUID owner, ResourceLocation location, T trait, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> data, Component name) {
            this(owner, location, (TraitComponentKind<T>) trait.kind(), trait, data, name);
      }

      private SendEnderEntry(UUID owner, ResourceLocation location, TraitComponentKind<T> kind, T trait, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> data, Component name) {
            this.owner = owner;
            this.location = location;
            this.kind = kind;
            this.trait = trait;
            this.map = data;
            this.name = name;
      }

      public static <T extends GenericTraits> void send(ServerPlayer sender, UUID owner, ResourceLocation location, TraitComponentKind<T> kind, T trait, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> data, Component displayName) {
            new SendEnderEntry<>(owner, location, kind, trait, data, displayName).send2C(sender);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.ENDER_ENTRY_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeUUID(owner);
            ResourceLocation.STREAM_CODEC.encode(buf, location);

            TraitComponentKind.STREAM_CODEC.encode(buf, kind);
            TraitComponentKind.encode(buf, trait);

            EnderStorage.ENTRY_MAP_STREAM_CODEC.encode(buf, map);
            ComponentSerialization.STREAM_CODEC.encode(buf, name);
      }

      @Override
      public void handle() {
            EnderStorage enderStorage = CommonClient.getEnderStorage();
            enderStorage.set(owner, location, kind, trait, map, name);
      }

      public static Type<SendEnderEntry> ID = new Type<>(Constants.defaultLocation("send_ender_entry_c"));

      @Override
      public Type<SendEnderEntry> type() {
            return ID;
      }
}
