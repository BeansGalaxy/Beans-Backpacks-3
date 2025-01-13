package com.beansgalaxy.backpacks.traits.bulk;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.MutableItemStorage;
import com.beansgalaxy.backpacks.util.EmptyStack;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BulkMutable implements MutableItemStorage {
      private final BulkTraits traits;
      public final ITraitData<BulkStacks> bulkList;
      private final PatchedComponentHolder holder;

      public BulkMutable(BulkTraits traits, PatchedComponentHolder holder) {
            this.traits = traits;
            this.bulkList = ITraitData.BULK_STACKS.get(holder);
            this.holder = holder;
      }

      @Override
      public int getMaxAmountToAdd(ItemStack stack) {
            Fraction size = Fraction.getFraction(traits.size(), 1);

            Fraction fraction = Fraction.ZERO;
            if (!stack.isEmpty()) {
                  BulkStacks bulkStacks = this.bulkList.get();
                  for (EmptyStack emptyStack : bulkStacks.emptyStacks) {
                        Fraction stackWeight = Fraction.getFraction(emptyStack.amount(), emptyStack.getMaxStackSize(bulkStacks.itemHolder));
                        fraction = fraction.add(stackWeight);
                  }
            }

            Fraction weightLeft = size.subtract(fraction);
            return Math.max(weightLeft.divideBy(Traits.getItemWeight(stack)).intValue(), 0);
      }

      @Override @Nullable
      public ItemStack addItem(ItemStack inserted, Player player) {
            return addItem(inserted);
      }

      @Override
      public void moveItemsTo(MutableItemStorage to, Player player, boolean fullStack) {
            BulkStacks bulkStacks = bulkList.get();
            EmptyStack first = bulkStacks.emptyStacks.getFirst();
            ItemStack stack = first.withItem(bulkStacks.itemHolder);
            int toAdd = to.getMaxAmountToAdd(stack);
            if (toAdd > 0) {
                  ItemStack moved = removeItem(0);
                  to.addItem(moved, player);
                  this.push();
                  to.push();
            }
      }

      @Nullable
      public ItemStack addItem(ItemStack inserted) {
            BulkStacks bulkList = this.bulkList.get();
            if (bulkList.isEmpty()) {
                  int toAdd = Math.min(inserted.getCount(), getMaxAmountToAdd(inserted));
                  ItemStack split = inserted.split(toAdd);

                  List<EmptyStack> emptyStacks = List.of(new EmptyStack(split.getCount(), split.getComponentsPatch()));
                  BulkStacks newList = new BulkStacks(split.getItemHolder(), emptyStacks);

                  this.bulkList.set(newList);
                  return inserted;
            }
            else if (!inserted.is(bulkList.itemHolder))
                  return null;

            int toAdd = Math.min(inserted.getCount(), getMaxAmountToAdd(inserted));
            ItemStack split = inserted.split(toAdd);

            for (EmptyStack emptyStack : bulkList.emptyStacks) {
                  if (emptyStack.is(split)) {
                        emptyStack.amount += toAdd;
                        return inserted;
                  }
            }

            ArrayList<EmptyStack> emptyStacks = new ArrayList<>(bulkList.emptyStacks);
            emptyStacks.addFirst(new EmptyStack(split.getCount(), split.getComponentsPatch()));
            List<EmptyStack> list = emptyStacks.stream().toList();

            BulkStacks newList = new BulkStacks(split.getItemHolder(), list);
            this.bulkList.set(newList);
            return inserted;
      }

      @Override
      public ItemStack removeItem(int slot) {
            BulkStacks bulkList = this.bulkList.get();
            if (bulkList.isEmpty())
                  return ItemStack.EMPTY;

            List<EmptyStack> emptyStacks = bulkList.emptyStacks;
            EmptyStack first = emptyStacks.getFirst();
            Holder<Item> itemHolder = bulkList.itemHolder;
            int maxStackSize = first.getMaxStackSize(itemHolder);
            ItemStack stack;
            if (first.amount <= maxStackSize) {
                  stack = first.withItem(itemHolder);
                  ArrayList<EmptyStack> newStacks = new ArrayList<>(emptyStacks);
                  newStacks.removeFirst();
                  this.bulkList.set(new BulkStacks(itemHolder, newStacks.stream().toList()));
            } else {
                  stack = first.withItem(itemHolder, maxStackSize);
                  first.amount -= maxStackSize;
                  this.bulkList.markDirty();
            }

            return stack;
      }

      public ItemStack splitItem() {
            BulkStacks bulkList = this.bulkList.get();
            if (bulkList.isEmpty())
                  return ItemStack.EMPTY;

            List<EmptyStack> emptyStacks = bulkList.emptyStacks;
            EmptyStack first = emptyStacks.getFirst();
            Holder<Item> itemHolder = bulkList.itemHolder;
            int maxStackSize = first.getMaxStackSize(itemHolder);
            int min = Math.min(maxStackSize, first.amount);
            int count = Mth.ceil(min / 2f);
            ItemStack stack = first.splitItem(itemHolder, count);
            return stack;
      }

      @Override
      public boolean isEmpty() {
            return bulkList.isEmpty();
      }

      public void push() {
            bulkList.push();
            holder.setChanged();
      }

      @Override
      public ModSound sound() {
            return traits.sound();
      }

      @Override
      public Fraction fullness() {
            BulkStacks bulkStacks = this.bulkList.get();
            if (bulkStacks.isEmpty())
                  return Fraction.ZERO;

            return bulkStacks.fullness(traits);
      }

      public record BulkStacks(Holder<Item> itemHolder, List<EmptyStack> emptyStacks) {
            public static final Codec<BulkStacks> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(BulkStacks::itemHolder),
                              EmptyStack.EMPTY_STACK_CODEC.listOf().fieldOf("stacks").forGetter(BulkStacks::emptyStacks)
                  ).apply(in, BulkStacks::new)
            );

            public static final StreamCodec<? super RegistryFriendlyByteBuf, BulkStacks> STREAM_CODEC = new StreamCodec<>() {
                  @Override
                  public void encode(RegistryFriendlyByteBuf buf, BulkStacks bulkStacks) {
                        ByteBufCodecs.holderRegistry(Registries.ITEM).encode(buf, bulkStacks.itemHolder);
                        EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.encode(buf, bulkStacks.emptyStacks);
                  }

                  @Override
                  public BulkStacks decode(RegistryFriendlyByteBuf buf) {
                        Holder<Item> itemHolder = ByteBufCodecs.holderRegistry(Registries.ITEM).decode(buf);
                        List<EmptyStack> emptyStacks = EmptyStack.LIST_EMPTY_STACK_STREAM_CODEC.decode(buf);
                        return new BulkStacks(itemHolder, emptyStacks);
                  }
            };

            public boolean isEmpty() {
                  return emptyStacks().isEmpty() || BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR).is(itemHolder) || emptyStacks.stream().allMatch(EmptyStack::isEmpty);
            }

            public int amount() {
                  Stream<EmptyStack> stream = emptyStacks.stream();
                  return stream.mapToInt(EmptyStack::amount).sum();
            }

            public Fraction fullness(BulkTraits bulkTraits) {
                  int defaultMaxStackSize = itemHolder.value().getDefaultMaxStackSize();
                  int summedMaximum = emptyStacks.stream().mapToInt(stack -> {
                        Optional<? extends Integer> i = stack.data().get(DataComponents.MAX_STACK_SIZE);
                        if (i == null || i.isEmpty())
                              return defaultMaxStackSize;

                        return i.get();
                  }).sum();

                  int amount = amount() * emptyStacks.size();
                  int maxAmount = summedMaximum * bulkTraits.size();
                  return Fraction.getFraction(amount, maxAmount);
            }
      }

}
