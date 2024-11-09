package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.client.CommonAtClient;
import com.beansgalaxy.backpacks.components.reference.ReferenceFields;
import com.beansgalaxy.backpacks.components.reference.ReferenceTraitRegistry;
import com.beansgalaxy.backpacks.network.clientbound.SendEnderEntry;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnderStorage {
      private final HashMap<UUID, PlayerEntry> storage = new HashMap<>();

      public static EnderStorage get(Level level) {
            MinecraftServer server = level.getServer();
            if (server == null) {
                  return CommonAtClient.getEnderStorage();
            }
            else return ServerSave.getSave(server, true).enderStorage;
      }

      public PlayerEntry get(UUID uuid) {
            return storage.computeIfAbsent(uuid, key -> new PlayerEntry());
      }

      public GenericTraits getTrait(UUID uuid, ResourceLocation location) {
            return get(uuid).get(location).trait;
      }

      public Reference2ObjectOpenHashMap<DataComponentType<?>, Object> get(UUID uuid, ResourceLocation location) {
            return get(uuid).get(location).data;
      }

      public Reference2ObjectOpenHashMap<DataComponentType<?>, Object> get(Player player, ResourceLocation location) {
            PlayerEntry entry = get(player.getUUID());
            entry.displayName = player.getName();
            return entry.get(location).data;
      }

      public <T> T remove(UUID uuid, ResourceLocation location, DataComponentType<? extends T> type) {
            return (T) get(uuid).get(location).data.remove(type);
      }

      public <T> void set(UUID uuid, ResourceLocation location, DataComponentType<? super T> type, T trait) {
            TraitEntry<?> entry = get(uuid).traits.computeIfAbsent(location, PlayerEntry::newMapFromLocation);
            if (entry != null) {
                  entry.data.put(type, trait);
            }
      }

      public <T extends GenericTraits> void set(UUID uuid, ResourceLocation location, TraitComponentKind<T> kind, T trait, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> data, Component name) {
            PlayerEntry entry = get(uuid);
            TraitEntry<?> newEntry = new TraitEntry<>(kind, trait, data);
            entry.traits.put(location, newEntry);
            entry.displayName = name;
      }

      public void set(UUID uuid, ResourceLocation location, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> traits, Component name) {
            PlayerEntry entry = get(uuid);
            TraitEntry<?> traitEntry = entry.traits.get(location);
            if (traitEntry != null) {
                  TraitEntry<?> newEntry = new TraitEntry<>(traitEntry, traits);
                  entry.traits.put(location, newEntry);
                  entry.displayName = name;
            }
      }

      public void set(UUID uuid, ResourceLocation location, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> traits) {
            PlayerEntry entry = get(uuid);
            TraitEntry<?> traitEntry = entry.traits.get(location);
            if (traitEntry != null) {
                  TraitEntry<?> newEntry = new TraitEntry<>(traitEntry, traits);
                  entry.traits.put(location, newEntry);
            }
      }
      
      public void save(CompoundTag tag) {
            storage.forEach((uuid, entry) -> {
                  DataResult<Tag> encodedStart = PlayerEntry.ENTRY_CODEC.encodeStart(NbtOps.INSTANCE, entry);
                  encodedStart.ifSuccess(entryTag ->
                              tag.put(uuid.toString(), entryTag)
                  );
            });
      }
      
      public void load(CompoundTag tag) {
            for (String key : tag.getAllKeys()) {
                  UUID uuid = UUID.fromString(key);
                  CompoundTag entryTag = tag.getCompound(key);
                  PlayerEntry.ENTRY_CODEC.decode(NbtOps.INSTANCE, entryTag).ifSuccess(pair ->
                              storage.put(uuid, pair.getFirst())
                  );
            }
      }

      public HashSet<ServerPlayer> getListeners(UUID uuid) {
            return get(uuid).listeners;
      }

      public Component getDisplayName(UUID uuid) {
            return get(uuid).displayName;
      }

      public static void sendEntry(ServerPlayer serverPlayer, UUID uuid, ResourceLocation location) {
            PlayerEntry entry = EnderStorage.get(serverPlayer.level()).get(uuid);
            TraitEntry traitEntry = entry.get(location);
            SendEnderEntry.send(serverPlayer, uuid, location, traitEntry.kind, traitEntry.trait, traitEntry.data, entry.displayName);
      }

      private static final class PlayerEntry {
            private final HashMap<ResourceLocation, TraitEntry> traits = new HashMap<>();
            private final HashSet<ServerPlayer> listeners = new HashSet<>();
            private Component displayName = Component.empty();

            private TraitEntry get(ResourceLocation location) {
                  return traits.computeIfAbsent(location, PlayerEntry::newMapFromLocation);
            }

            private static @Nullable TraitEntry newMapFromLocation(ResourceLocation key) {
                  ReferenceFields reference = ReferenceTraitRegistry.get(key);
                  if (reference == null)
                        return null;

                  GenericTraits fields = reference.traits();
                  Reference2ObjectOpenHashMap<DataComponentType<?>, Object> map = new Reference2ObjectOpenHashMap<>();
                  return new TraitEntry(fields.kind(), fields, map);
            }

            private static final MapCodec<GenericTraits> DISPATCHED_TRAIT_CODEC = TraitComponentKind.CODEC.dispatchMap("kind", GenericTraits::kind, kind -> kind.codec().fieldOf("trait"));

            private static final Codec<Pair<ResourceLocation, TraitEntry>> START_CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    ResourceLocation.CODEC.fieldOf("location").forGetter(Pair::first),
                                    DISPATCHED_TRAIT_CODEC.forGetter(pair -> pair.second().trait),
                                    DataComponentType.VALUE_MAP_CODEC.fieldOf("data").forGetter(pair -> pair.second().data)
                        ).apply(in, ((location, traits, data) -> {
                              Reference2ObjectOpenHashMap<DataComponentType<?>, Object> map = new Reference2ObjectOpenHashMap<>(data);
                              TraitEntry traitEntry = new TraitEntry(traits.kind(), traits, map);
                              return Pair.of(location, traitEntry);
                        }))
            );

            private static final Codec<PlayerEntry> ENTRY_CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    START_CODEC.listOf().fieldOf("entries").forGetter(entry -> entry.traits.entrySet().stream().map(e ->
                                                Pair.of(e.getKey(), e.getValue())).toList()
                                    ),
                                    ComponentSerialization.CODEC.fieldOf("name").forGetter(entry -> entry.displayName)
                        ).apply(in, (trait, name) -> {
                              PlayerEntry entry = new PlayerEntry();
                              entry.displayName = name;

                              trait.forEach(reference -> {
                                    entry.traits.put(reference.first(), reference.second());
                              });

                              return entry;
                        })
            );
      }

      private record TraitEntry<T extends GenericTraits>(TraitComponentKind<T> kind, T trait, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> data) {

            public TraitEntry(TraitEntry<T> entry, Reference2ObjectOpenHashMap<DataComponentType<?>, Object> traits) {
                  this(entry.kind, entry.trait, traits);
            }
      }


      private static <T> void encodeMap(RegistryFriendlyByteBuf buf, DataComponentType<T> key, Object value) {
            DataComponentType.STREAM_CODEC.encode(buf, key);
            key.streamCodec().encode(buf, (T) value);
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, Reference2ObjectOpenHashMap<DataComponentType<?>, Object>> ENTRY_MAP_STREAM_CODEC = StreamCodec.of(
                  (buf, map) -> {
                        int size = map.size();
                        buf.writeInt(size);
                        map.forEach((key, value) -> encodeMap(buf, key, value));
                  }, buf -> {
                        Reference2ObjectOpenHashMap<DataComponentType<?>, Object> map = new Reference2ObjectOpenHashMap<>();

                        int size = buf.readInt();
                        for (int i = 0; i < size; i++) {
                              DataComponentType<?> key = DataComponentType.STREAM_CODEC.decode(buf);
                              Object decode = key.streamCodec().decode(buf);
                              map.put(key, decode);
                        }

                        return map;
                  }
      );
}
