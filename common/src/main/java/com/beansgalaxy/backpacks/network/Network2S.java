package com.beansgalaxy.backpacks.network;

import com.beansgalaxy.backpacks.network.serverbound.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum Network2S {
      HOTKEY_2S(SyncHotkey.class, SyncHotkey.ID, SyncHotkey::encode, SyncHotkey::new, SyncHotkey::handle),
      PLACE_BACKPACK_2S(BackpackUseOn.class, BackpackUseOn.ID, BackpackUseOn::encode, BackpackUseOn::new, BackpackUseOn::handle),
      PICK_BLOCK_2S(PickBlock.class, PickBlock.ID, PickBlock::encode, PickBlock::new, PickBlock::handle),
      PICK_ITEM_2S(PickItem.class, PickItem.ID, PickItem::encode, PickItem::new, PickItem::handle),
      TINY_SUB_CHEST_2S(TinyChestClick.class, TinyChestClick.ID, TinyChestClick::encode, TinyChestClick::new, TinyChestClick::handle),
      TINY_MENU_2S(TinyMenuClick.class, TinyMenuClick.ID, TinyMenuClick::encode, TinyMenuClick::new, TinyMenuClick::handle),
      TINY_HOTBAR_2S(TinyHotbarClick.class, TinyHotbarClick.ID, TinyHotbarClick::encode, TinyHotbarClick::new, TinyHotbarClick::handle),
      TINY_INTERACT_2S(TinyMenuInteract.class, TinyMenuInteract.ID, TinyMenuInteract::encode, TinyMenuInteract::new, TinyMenuInteract::handle),
      SYNC_SELECTED_SLOT_2S(SyncSelectedSlot.class, SyncSelectedSlot.ID, SyncSelectedSlot::encode, SyncSelectedSlot::new, SyncSelectedSlot::handle),
      INSTANT_KEY_2S(InstantKeyPress.class, InstantKeyPress.ID, InstantKeyPress::encode, InstantKeyPress::new, InstantKeyPress::handle),
      UTILITY_ROCKET_USE(UtilitiesUse.class, UtilitiesUse.ID, UtilitiesUse::encode, UtilitiesUse::new, UtilitiesUse::handle),
      TRAIT_MENU_CLICK(TraitMenuClick.class, TraitMenuClick.ID, TraitMenuClick::encode, TraitMenuClick::new, TraitMenuClick::handle)
      ;

      public final DynamicLoaderPacket<? super RegistryFriendlyByteBuf, ?> packet;
      <T extends Packet2S> Network2S(Class<T> clazz, CustomPacketPayload.Type<T> id, BiConsumer<T, RegistryFriendlyByteBuf> encoder, Function<RegistryFriendlyByteBuf, T> decoder, BiConsumer<T, ServerPlayer> handle) {
            this.packet = new DynamicLoaderPacket<>(clazz, id, encoder, decoder, handle);
      }

      public void debugMsgEncode() {
//            System.out.println("encode = " + packet);
      }

      public void debugMsgDecode() {
//            System.out.println("decode = " + packet);
      }

      public class DynamicLoaderPacket<B extends RegistryFriendlyByteBuf, T extends Packet2S> implements StreamCodec<B, T> {
            public final Class<T> clazz;
            public final CustomPacketPayload.Type<T> type;
            private final BiConsumer<T, B> encoder;
            private final Function<B, T> decoder;
            private final BiConsumer<T, ServerPlayer> handle;

            private DynamicLoaderPacket(Class<T> clazz, CustomPacketPayload.Type<T> type, BiConsumer<T, B> encoder, Function<B, T> decoder, BiConsumer<T, ServerPlayer> handle) {
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

            public void handle(T msg, ServerPlayer player) {
                  handle.accept(msg, player);
            }
      }

}
