package com.beansgalaxy.backpacks.components.ender;

import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.network.clientbound.SendEnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class EnderTraits implements ComponentHolder {
      private final UUID uuid;
      private final ResourceLocation location;
      private @Nullable Level level = null;

      public static Optional<EnderTraits> get(ItemStack stack) {
            if (!ModItems.ENDER_POUCH.is(stack))
                  return Optional.empty();

            EnderTraits enderTraits = stack.get(Traits.ENDER);
            return Optional.ofNullable(enderTraits);
      }

      public EnderTraits(UUID uuid, ResourceLocation location) {
            this.uuid = uuid;
            this.location = location;
      }

      public void reload(Level level) {
            this.level = level;
      }

      public boolean isLoaded() {
            return level != null;
      }

      public <T> Optional<T> mapLoaded(Function<Level, T> map) {
            return isLoaded()
                        ? Optional.ofNullable(map.apply(level))
                        : Optional.empty();
      }

      public void runLoaded(Consumer<Level> run) {
            if (isLoaded())
                  run.accept(level);
      }

      public static Optional<ItemStorageTraits> getItemStorage(ItemStack stack) {
            EnderTraits enderTraits = stack.get(Traits.ENDER);
            if (enderTraits != null) {
                  return enderTraits.getTrait().map(traits -> {
                        if (traits instanceof ItemStorageTraits storageTraits)
                              return storageTraits;
                        else
                              return null;
                  });
            }

            return Optional.empty();
      }

      public static ItemStack createItem(Player player, ResourceLocation location) {
            Item enderItem = ModItems.ENDER_POUCH.get();
            Level level = player.level();
            UUID uuid = player.getUUID();

            EnderStorage.get(level).get(player, location);
            EnderTraits traits = new EnderTraits(uuid, location);
            ItemStack defaultInstance = enderItem.getDefaultInstance();
            defaultInstance.set(Traits.ENDER, traits);

            return defaultInstance;
      }

      public UUID owner() {
            return uuid;
      }

      public ResourceLocation trait() {
            return location;
      }

      public Optional<GenericTraits> getTrait() {
            return mapLoaded((level) ->
                        EnderStorage.get(level).getTrait(uuid, location)
            );
      }

      public GenericTraits getTrait(Level level) {
            if (this.level == null)
                  this.level = level;
            return EnderStorage.get(this.level).getTrait(uuid, location);
      }

      @Override
      public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EnderTraits) obj;
            return Objects.equals(this.uuid, that.uuid) &&
                        Objects.equals(this.location, that.location);
      }

      @Override
      public int hashCode() {
            return Objects.hash(uuid, location);
      }

      @Override
      public String toString() {
            return "EnderTraits[" +
                        "owner=" + uuid + ", " +
                        "trait=" + location + ']';
      }

      public static final Codec<EnderTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              UUIDUtil.STRING_CODEC.fieldOf("owner").forGetter(EnderTraits::owner),
                              ResourceLocation.CODEC.fieldOf("location").forGetter(EnderTraits::trait)
                  ).apply(in, EnderTraits::new)
      );

      public static final StreamCodec<? super RegistryFriendlyByteBuf, EnderTraits> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, EnderTraits traits) {
                  ResourceLocation.STREAM_CODEC.encode(buf, traits.location);
                  UUIDUtil.STREAM_CODEC.encode(buf, traits.uuid);
            }

            @Override
            public EnderTraits decode(RegistryFriendlyByteBuf buf) {
                  ResourceLocation location = ResourceLocation.STREAM_CODEC.decode(buf);
                  UUID uuid = UUIDUtil.STREAM_CODEC.decode(buf);
                  EnderTraits enderTraits = new EnderTraits(uuid, location);
                  return enderTraits;
            }
      };

      @Override
      public <T> @Nullable T remove(DataComponentType<? extends T> type) {
            return mapLoaded((level) ->
                        EnderStorage.get(level).remove(uuid, location, type)
            ).orElse(null);
      }

      @Override
      public <T> void set(DataComponentType<? super T> type, T trait) {
            runLoaded((level) ->
                        EnderStorage.get(level).set(uuid, location, type, trait)
            );
      }

      @Override
      public <T> T get(DataComponentType<? extends T> type) {
            return mapLoaded((level) -> {
                  EnderStorage enderStorage = EnderStorage.get(level);
                  return (T) enderStorage.get(uuid, location).get(type);
            }).orElse(null);
      }

      @Override
      public void setChanged() {
            if (!isLoaded() || level.isClientSide)
                  return;

            Predicate<ItemStack> matchTraits = stack -> this.equals(stack.get(Traits.ENDER));
            HashSet<ServerPlayer> listeners = getListeners(level);
            Iterator<ServerPlayer> iterator = listeners.iterator();

            while (iterator.hasNext()) {
                  ServerPlayer listener = iterator.next();
                  if (listener.getInventory().contains(matchTraits) || matchTraits.test(listener.inventoryMenu.getCarried())) {
                        SendEnderTraits.send(listener, uuid, location);
                  }
                  else iterator.remove();
            }
      }

      public void broadcastChanges() {

      }

      public void broadcastChanges(ServerPlayer player) {
            Predicate<ItemStack> matchTraits = stack -> this.equals(stack.get(Traits.ENDER));
            HashSet<ServerPlayer> listeners = getListeners(player.level());
            Iterator<ServerPlayer> iterator = listeners.iterator();

            while (iterator.hasNext()) {
                  ServerPlayer listener = iterator.next();
                  if (!listener.equals(player)) {
                        if (listener.getInventory().contains(matchTraits) || matchTraits.test(listener.inventoryMenu.getCarried())) {
                              SendEnderTraits.send(listener, uuid, location);
                        }
                        else iterator.remove();
                  }
            }
      }

      private HashSet<ServerPlayer> getListeners(Level level) {
            return EnderStorage.get(level).getListeners(uuid);
      }

      public void addListener(ServerPlayer serverPlayer) {
            HashSet<ServerPlayer> listeners = getListeners(serverPlayer.level());
            if (!listeners.contains(serverPlayer)) {
                  listeners.add(serverPlayer);
                  EnderStorage.sendEntry(serverPlayer, uuid, location);
            }
      }

      public Component getDisplayName() {
            return mapLoaded((level) ->
                        EnderStorage.get(level).getDisplayName(uuid)
            ).orElse(Component.literal("§kPlayer"));
      }
}
