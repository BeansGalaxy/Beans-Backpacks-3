package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FilterComponent(Iterable<Holder<Item>> items, @Nullable TagKey<Item> tag, boolean blacklist) {
      public static final String NAME = "filter";
      public static final FilterComponent EMPTY = new FilterComponent(List.of(), null, false);
      
      public FilterComponent(TagKey<Item> tag, boolean blacklist) {
            this(BuiltInRegistries.ITEM.getTagOrEmpty(tag), tag, blacklist);
      }
      
      public FilterComponent(List<Holder<Item>> items) {
            this(items, null, false);
      }
      
      public boolean isEmpty() {
            return this == EMPTY || !items.iterator().hasNext();
      }
      
      public boolean passes(ItemStack stack) {
            for (Holder<Item> item : items) {
                  if (stack.is(item))
                        return !blacklist;
            }
            
            return blacklist;
      }
      
      @Nullable
      public static FilterComponent get(ComponentHolder stack) {
            FilterComponent component = stack.get(Traits.FILTER);
            if (component != null) {
                  return component;
            }
            
            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference == null)
                  return null;
            
            return reference.getFilter().orElse(null);
      }
      
      private static final Codec<FilterComponent> TAG_CODEC = Codec.STRING.comapFlatMap((string) -> {
            boolean blacklist;
            String path;
            if (string.startsWith("!#")) {
                  blacklist = true;
                  path = string.substring(2);
            }
            else if (string.startsWith("#")) {
                  blacklist = false;
                  path = string.substring(1);
            }
            else return DataResult.error(() -> "Not a tag id");
            
            return ResourceLocation.read(path).map(location -> {
                  TagKey<Item> key = TagKey.create(Registries.ITEM, location);
                  return new FilterComponent(key, blacklist);
            });
      }, (filter) -> {
            String prefix = filter.blacklist ? "!#" : "#";
            return prefix + filter.tag.location();
      });
      
      public static final Codec<FilterComponent> CODEC = new Codec<>() {
            @Override public <T> DataResult<Pair<FilterComponent, T>> decode(DynamicOps<T> ops, T input) {
                  DataResult<Pair<FilterComponent, T>> decodeTag = TAG_CODEC.decode(ops, input);
                  if (decodeTag.isSuccess())
                        return decodeTag;
                  
                  DataResult<Pair<List<Holder<Item>>, T>> decodeItem = ItemStack.ITEM_NON_AIR_CODEC.listOf().decode(ops, input);
                  if (decodeItem.isSuccess())
                        return decodeItem.map(pair -> pair.mapFirst(FilterComponent::new));
                  
                  return DataResult.error(() -> "Neither item nor tag was parsed correctly; item=" + decodeItem.error().get() + "; tag=" + decodeTag.error().get());
            }
            
            @Override public <T> DataResult<T> encode(FilterComponent filter, DynamicOps<T> ops, T prefix) {
                  if (filter.tag == null) {
                        List<Holder<Item>> items = new ArrayList<>();
                        for (Holder<Item> holder : filter.items)
                              items.add(holder);
                        
                        return ItemStack.ITEM_NON_AIR_CODEC.listOf().encode(items, ops, prefix);
                  }
                  else
                        return TagKey.hashedCodec(Registries.ITEM).encode(filter.tag, ops, prefix);
            }
      };
      
      public static final StreamCodec<RegistryFriendlyByteBuf, FilterComponent> STREAM_CODEC = StreamCodec.of(
            (buf, filter) -> {
                  List<Holder<Item>> items = new ArrayList<>();
                  for (Holder<Item> holder : filter.items)
                        items.add(holder);
                  
                  int size = items.size();
                  buf.writeInt(size);
                  for (Holder<Item> item : items)
                        ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, item);
            },
            buf -> {
                  int size = buf.readInt();
                  ArrayList<Holder<Item>> items = new ArrayList<>();
                  for (int i = 0; i < size; i++) {
                        Holder<Item> item = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                        items.add(item);
                  }
                  
                  return new FilterComponent(items);
            }
      );
}
