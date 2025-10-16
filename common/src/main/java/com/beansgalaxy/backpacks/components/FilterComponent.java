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
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record FilterComponent(Iterable<Holder<Item>> items, @Nullable TagKey<Item> tag) {
      public static final String NAME = "filter";
      public static final FilterComponent EMPTY = new FilterComponent(List.of(), null);
      
      public FilterComponent(TagKey<Item> tag) {
            this(BuiltInRegistries.ITEM.getTagOrEmpty(tag), tag);
      }
      
      public FilterComponent(List<Holder<Item>> items) {
            this(items, null);
      }
      
      public boolean isEmpty() {
            return this == EMPTY || !items.iterator().hasNext();
      }
      
      public boolean test(ItemStack stack) {
            for (Holder<Item> item : items) {
                  if (stack.is(item))
                        return true;
            }
            
            return false;
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
      
      public static final Codec<FilterComponent> CODEC = new Codec<>() {
            @Override public <T> DataResult<Pair<FilterComponent, T>> decode(DynamicOps<T> ops, T input) {
                  DataResult<Pair<TagKey<Item>, T>> decodeTag = TagKey.hashedCodec(Registries.ITEM).decode(ops, input);
                  if (decodeTag.isSuccess())
                        return decodeTag.map(pair -> pair.mapFirst(FilterComponent::new));
                  
                  DataResult<Pair<List<Holder<Item>>, T>> decodeItem = ItemStack.ITEM_NON_AIR_CODEC.listOf().decode(ops, input);
                  if (decodeItem.isSuccess())
                        return decodeItem.map(pair -> pair.mapFirst(FilterComponent::new));
                  
                  return DataResult.error(() -> "Neither item nor tag was parsed correctly; item=" + decodeItem.error().get() + "; tag=" + decodeTag.error().get());
            }
            
            @Override public <T> DataResult<T> encode(FilterComponent input, DynamicOps<T> ops, T prefix) {
                  if (input.tag == null)
                        return ItemStack.ITEM_NON_AIR_CODEC.listOf().encode(Lists.newArrayList(input.items.iterator()), ops, prefix);
                  else
                        return TagKey.hashedCodec(Registries.ITEM).encode(input.tag, ops, prefix);
            }
      };
      
      public static final StreamCodec<RegistryFriendlyByteBuf, FilterComponent> STREAM_CODEC = StreamCodec.of(
            (buf, filter) -> {
                  ArrayList<Holder<Item>> items = Lists.newArrayList(filter.items.iterator());
                  int size = items.size();
                  buf.writeInt(size);
                  for (Holder<Item> item : items)
                        ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, item);
            },
            buf -> {
                  int size = buf.readInt();
                  ArrayList<Holder<Item>> items = Lists.newArrayList();
                  for (int i = 0; i < size; i++) {
                        Holder<Item> item = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                        items.add(item);
                  }
                  
                  return new FilterComponent(items);
            }
      );
}
