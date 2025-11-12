package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.math.Fraction;

import java.util.ArrayList;
import java.util.List;

public record BulkComponent(Holder<Item> item, List<ItemlessStack> stacks, Fraction weight) {
      
      public BulkComponent(Holder<Item> item, List<ItemlessStack> stacks) {
            this(item, stacks, getWeight(item, stacks));
      }
      
      private static Fraction getWeight(Holder<Item> item, List<ItemlessStack> stacks) {
            Fraction fraction = Fraction.ZERO;
            for (ItemlessStack stack : stacks) {
                  Fraction stackWeight = Traits.getStackWeight(stack.withItem(item));
                  fraction = fraction.add(stackWeight);
            }
            
            return fraction.reduce();
      }
      
      public static final Codec<BulkComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                  BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(BulkComponent::item),
                  ItemlessStack.CODEC.listOf().fieldOf("stacks").forGetter(BulkComponent::stacks)
            ).apply(instance, BulkComponent::new)
      );
      
      public static final StreamCodec<RegistryFriendlyByteBuf, BulkComponent> STREAM_CODEC = StreamCodec.of(
            (buf, bulk) -> {
                  ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, bulk.item);
                  List<ItemlessStack> stacks = bulk.stacks;
                  int size = stacks.size();
                  buf.writeInt(size);
                  
                  for (int i = 0; i < size; i++) {
                        ItemlessStack stack = stacks.get(i);
                        ItemlessStack.STREAM_CODEC.encode(buf, stack);
                  }
            }, buf -> {
                  Holder<Item> item = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                  int size = buf.readInt();
                  
                  ArrayList<ItemlessStack> stacks = new ArrayList<>();
                  for (int i = 0; i < size; i++) {
                        ItemlessStack stack = ItemlessStack.STREAM_CODEC.decode(buf);
                        stacks.add(stack);
                  }
                  
                  return new BulkComponent(item, stacks);
            }
      );
      
      public boolean isEmpty() {
            return stacks.isEmpty() || Items.AIR.equals(item.value());
      }
      
      public record ItemlessStack(DataComponentPatch patch, int count) {
            
            public static final Codec<ItemlessStack> CODEC = RecordCodecBuilder.create(instance ->
                  instance.group(
                        DataComponentPatch.CODEC.fieldOf("components").forGetter(ItemlessStack::patch),
                        Codec.INT.fieldOf("count").forGetter(ItemlessStack::count)
                  ).apply(instance, ItemlessStack::new)
            );
            
            public static final StreamCodec<RegistryFriendlyByteBuf, ItemlessStack> STREAM_CODEC = StreamCodec.of(
                  (buf, stack) -> {
                        DataComponentPatch.STREAM_CODEC.encode(buf, stack.patch());
                        buf.writeInt(stack.count());
                  }, buf -> {
                        DataComponentPatch components = DataComponentPatch.STREAM_CODEC.decode(buf);
                        int count = buf.readInt();
                        
                        return new ItemlessStack(components, count);
                  }
            );
            
            public ItemStack withItem(Holder<Item> item) {
                  return new ItemStack(item, count, patch);
            }
      }
}
