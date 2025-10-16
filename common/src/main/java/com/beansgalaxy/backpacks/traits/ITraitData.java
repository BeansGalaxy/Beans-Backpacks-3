package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class ITraitData<T> {
      public static final TraitDataComponentType<List<ItemStack>>
                  ITEM_STACKS = register("data_item_list", Traits.STACKS_CODEC, ItemStack.LIST_STREAM_CODEC, ItemList::new);

      public static final TraitDataComponentType<List<ItemStack>>
                  NON_EDIBLES = register("data_non_edible", ItemStack.CODEC.listOf(), ItemStack.LIST_STREAM_CODEC,  NonEdibles::new);

      public static final DataComponentType<ItemContainerContents>
                  CHEST = Traits.register("data_chest", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);

      public static final TraitDataComponentType<ItemStack>
                  SOLO_STACK = register("data_solo_item", ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC, SoloItem::new);

      public static final TraitDataComponentType<Integer>
                  AMOUNT = register("data_amount", ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.INT, Amount::new);

      public static final DataComponentType<SlotSelection>
                  SLOT_SELECTION = Traits.register("data_selection", Codec.unit(SlotSelection::new), SlotSelection.STREAM_CODEC);
      
      public static final TraitDataComponentType<SlotSelection>
                  NEW_SLOT_SELECTION = register("new_data_selection", Codec.unit(SlotSelection::new), SlotSelection.STREAM_CODEC, SlotSelector::new);

      public static final DataComponentType<UtilityComponent>
                  UTILITIES = Traits.register("utility_slots", UtilityComponent.CODEC, UtilityComponent.STREAM_CODEC);

      public static void register() {

      }

      public static <T> TraitDataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Function<ComponentHolder, ITraitData<T>> getData) {
            TraitDataComponentType<T> type = new TraitDataComponentType<>(codec, streamCodec, getData);
            Services.PLATFORM.register(name, type);
            return type;
      }

      public @Nullable T remove() {
            return holder().remove(type());
      }

      public static class TraitDataComponentType<T> implements DataComponentType<T> {
            private final Codec<T> codec;
            private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
            private final Function<ComponentHolder, ITraitData<T>> getData;

            public TraitDataComponentType(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Function<ComponentHolder, ITraitData<T>> getData) {
                  this.codec = codec;
                  this.streamCodec = streamCodec;
                  this.getData = getData;
            }

            @Nullable @Override
            public Codec<T> codec() {
                  return this.codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                  return this.streamCodec;
            }

            public ITraitData<T> get(ComponentHolder holder) {
                  return getData.apply(holder);
            }
      }

      public abstract DataComponentType<T> type();

      public abstract boolean isEmpty(T data);

      public boolean isEmpty() {
            if (value != null) {
                  return isEmpty(value);
            }
            T data = holder().get(type());
            return data == null || isEmpty(data);
      }

      protected T value = null;
      boolean isDirty = false;

      protected final ComponentHolder holder;

      protected ITraitData(ComponentHolder holder) {
            this.holder = holder;
      }

      public ComponentHolder holder() {
            return holder;
      }

      public abstract T get();

      public void markDirty() {
            isDirty = true;
      }

      public void push() {
            if (isEmpty()) {
                  holder().remove(type());
            }
            else if (isDirty) {
                  holder().set(type(), value);
            }
      }

      public ITraitData<T> set(T value) {
            markDirty();
            this.value = value;
            return this;
      }

      @Override
      public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ITraitData<?>) obj;
            return Objects.equals(this.holder(), that.holder()) && Objects.equals(this.value, that.value);
      }

// ===================================================================================================================== TRAIT DATA

      static class SlotSelector extends ITraitData<SlotSelection> {
            SlotSelector(ComponentHolder holder) {
                  super(holder);
            }

            @Override
            public DataComponentType<SlotSelection> type() {
                  return NEW_SLOT_SELECTION;
            }

            @Override
            public boolean isEmpty(SlotSelection data) {
                  List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                  return stacks == null || stacks.isEmpty();
            }

            @Override
            public SlotSelection get() {
                  if (value == null) {
                        markDirty();
                        SlotSelection t = holder().get(type());
                        value = Objects.requireNonNullElse(t, new SlotSelection());
                  }
                  return value;
            }
            
            @Override
            public void push() {
                  List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty()) {
                        holder().remove(type());
                  }
                  else if (isDirty) {
                        int size = stacks.size();
                        if (value != null)
                              value.ceil(size);
                        
                        holder().set(type(), value);
                  }
            }
      }

      static class SoloItem extends ITraitData<ItemStack> {
            public SoloItem(ComponentHolder holder) {
                  super(holder);
            }

            @Override
            public DataComponentType<ItemStack> type() {
                  return SOLO_STACK;
            }

            @Override
            public ItemStack get() {
                  if (value == null) {
                        markDirty();
                        ItemStack t = holder().get(type());
                        value = Objects.requireNonNullElse(t, ItemStack.EMPTY);
                  }
                  return value;
            }

            @Override
            public boolean isEmpty(ItemStack data) {
                  return data.isEmpty();
            }
      }

      static class ItemList extends ITraitData<List<ItemStack>> {
            public ItemList(ComponentHolder holder) {
                  super(holder);
            }

            @Override
            public DataComponentType<List<ItemStack>> type() {
                  return ITEM_STACKS;
            }

            public List<ItemStack> get() {
                  if (value == null) {
                        markDirty();
                        List<ItemStack> t = holder().get(type());
                        if (t == null)
                              value = new ArrayList<>();
                        else
                              value = new ArrayList<>(t);
                  }
                  return value;
            }

            @Override
            public void push() {
                  List<ItemStack> stacks = value == null
                              ? holder.get(type())
                              : value.stream().filter(itemStack -> !itemStack.isEmpty()).toList();

                  if (stacks == null)
                        return;

                  value = stacks;

                  if (value.isEmpty()) {
                        holder.remove(type());
                  }
                  else if (isDirty) {
                        holder.set(type(), value);
                  }
            }

            @Override
            public boolean isEmpty(List<ItemStack> data) {
                  return data.isEmpty();
            }
      }

      static class NonEdibles extends ItemList {
            public NonEdibles(ComponentHolder holder) {
                  super(holder);
            }

            @Override
            public DataComponentType<List<ItemStack>> type() {
                  return NON_EDIBLES;
            }
      }

      static class Amount extends ITraitData<Integer> {
            public Amount(ComponentHolder holder) {
                  super(holder);
            }

            @Override
            public DataComponentType<Integer> type() {
                  return AMOUNT;
            }

            @Override
            public boolean isEmpty() {
                  if (value != null) {
                        markDirty();
                        return value == 0;
                  }

                  Integer amount = holder.get(type());
                  return amount == null || amount == 0;
            }

            @Override
            public boolean isEmpty(Integer data) {
                  return data == 0;
            }

            @Override
            public Integer get() {
                  if (value == null) {
                        markDirty();
                        Integer t = holder().get(type());
                        value = Objects.requireNonNullElse(t, 0);
                  }
                  return value;
            }
      }

}
