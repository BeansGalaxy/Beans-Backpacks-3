package com.beansgalaxy.backpacks.network;

import com.beansgalaxy.backpacks.network.clientbound.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public enum Network2C {
      ENDER_TRAIT_2C(SendEnderTraits.class, SendEnderTraits.ID, SendEnderTraits::encode, SendEnderTraits::new, SendEnderTraits::handle),
      ENDER_ENTRY_2C(SendEnderEntry.class, SendEnderEntry.ID, SendEnderEntry::encode, SendEnderEntry::decode, SendEnderEntry::handle),
      CONFIG_REFERENCES_2C(ConfigureReferences.class, ConfigureReferences.ID, ConfigureReferences::encode, ConfigureReferences::new, ConfigureReferences::handle),
      CONFIG_COMMON_2C(ConfigureConfig.class, ConfigureConfig.ID, ConfigureConfig::encode, ConfigureConfig::new, ConfigureConfig::handle),
      WEAPON_SLOT_2C(SendWeaponSlot.class, SendWeaponSlot.ID, SendWeaponSlot::encode, SendWeaponSlot::new, SendWeaponSlot::handle),
      ;

      public final DynamicLoaderPacket<? super RegistryFriendlyByteBuf, ?> packet;
      <T extends Packet2C> Network2C(Class<T> clazz, CustomPacketPayload.Type<T> id, BiConsumer<T, RegistryFriendlyByteBuf> encoder, Function<RegistryFriendlyByteBuf, T> decoder, Consumer<T> handle) {
            this.packet = new DynamicLoaderPacket<>(clazz, id, encoder, decoder, handle);
      }

      public void debugMsgEncode() {
//            System.out.println("encode = " + packet);
      }

      public void debugMsgDecode() {
//            System.out.println("decode = " + packet);
      }

      public class DynamicLoaderPacket<B extends RegistryFriendlyByteBuf, T extends Packet2C> implements StreamCodec<B, T> {
            public final Class<T> clazz;
            public final CustomPacketPayload.Type<T> type;
            private final BiConsumer<T, B> encoder;
            private final Function<B, T> decoder;
            private final Consumer<T> handle;

            private DynamicLoaderPacket(Class<T> clazz, CustomPacketPayload.Type<T> type, BiConsumer<T, B> encoder, Function<B, T> decoder, Consumer<T> handle) {
                  this.clazz = clazz;
                  this.type = type;
                  this.encoder = encoder;
                  this.decoder = decoder;
                  this.handle = handle;
            }

            @Override @NotNull
            public T decode(@NotNull B buf) {
                  debugMsgDecode();
                  return decoder.apply(buf);
            }

            @Override
            public void encode(@NotNull B buf, @NotNull T msg) {
                  debugMsgEncode();
                  encoder.accept(msg, buf);
            }

            public void handle(T msg) {
                  handle.accept(msg);
            }

            @Override
            public String toString() {
                  return getClass().getName() + " [" + type.id() + ']';
            }
      }

}
