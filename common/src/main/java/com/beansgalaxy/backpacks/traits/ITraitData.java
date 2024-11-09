package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.bulk.BulkMutable;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SlotSelection;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public abstract class ITraitData<T> {
      public static final TraitDataComponentType<List<ItemStack>>
                  ITEM_STACKS = register("data_item_list", ItemStack.CODEC.listOf(), ItemStack.LIST_STREAM_CODEC, ItemList::new);

      public static final TraitDataComponentType<List<ItemStack>>
                  NON_EDIBLES = register("data_non_edible", ItemStack.CODEC.listOf(), ItemStack.LIST_STREAM_CODEC, NonEdibles::new);

      public static final DataComponentType<ItemContainerContents>
                  CHEST = Traits.register("data_chest", ItemContainerContents.CODEC, ItemContainerContents.STREAM_CODEC);

      public static final TraitDataComponentType<ItemStack>
                  SOLO_STACK = register("data_solo_item", ItemStack.OPTIONAL_CODEC, ItemStack.OPTIONAL_STREAM_CODEC, SoloItem::new);

      public static final DataComponentType<Long>
                  LONG = Traits.register("data_long", Codec.LONG.validate(aLong -> aLong >= 0 ? DataResult.success(aLong) : DataResult.error(() -> "data_long cannot must be non-negative: " + aLong)), ByteBufCodecs.VAR_LONG);

      public static final TraitDataComponentType<Integer>
                  AMOUNT = register("data_amount", ExtraCodecs.NON_NEGATIVE_INT, ByteBufCodecs.INT, Amount::new);

      public static final TraitDataComponentType<BulkMutable.BulkStacks>
                  BULK_STACKS = register("data_bulk_list", BulkMutable.BulkStacks.CODEC, BulkMutable.BulkStacks.STREAM_CODEC, BulkList::new);

      public static final DataComponentType<SlotSelection>
                  SLOT_SELECTION = Traits.register("data_selection", Codec.unit(SlotSelection::new), SlotSelection.STREAM_CODEC);

      public static void register() {

      }

      public static <T> TraitDataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Function<PatchedComponentHolder, ITraitData<T>> getData) {
            TraitDataComponentType<T> type = new TraitDataComponentType<>(codec, streamCodec, getData);
            return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                        ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                        type);
      }

      public @Nullable T remove() {
            return holder().remove(type());
      }

      public static class TraitDataComponentType<T> implements DataComponentType<T> {
            private final Codec<T> codec;
            private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
            private final Function<PatchedComponentHolder, ITraitData<T>> getData;

            public TraitDataComponentType(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec, Function<PatchedComponentHolder, ITraitData<T>> getData) {
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

            public ITraitData<T> get(PatchedComponentHolder holder) {
                  return getData.apply(holder);
            }
      }

      public abstract DataComponentType<T> type();

      public abstract PatchedComponentHolder holder();

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

      static class SoloItem extends ITraitData<ItemStack> {
            private final PatchedComponentHolder holder;

            public SoloItem(PatchedComponentHolder holder) {
                  this.holder = holder;
            }

            @Override
            public DataComponentType<ItemStack> type() {
                  return SOLO_STACK;
            }

            @Override
            public PatchedComponentHolder holder() {
                  return holder;
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
            private final PatchedComponentHolder holder;

            public ItemList(PatchedComponentHolder holder) {
                  this.holder = holder;
            }

            @Override
            public DataComponentType<List<ItemStack>> type() {
                  return ITEM_STACKS;
            }

            @Override
            public PatchedComponentHolder holder() {
                  return holder;
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

                  if (stacks.isEmpty()) {
                        holder().remove(type());
                  }
                  else if (isDirty) {
                        holder().set(type(), stacks);
                  }
            }

            @Override
            public boolean isEmpty(List<ItemStack> data) {
                  return data.isEmpty();
            }
      }

      static class NonEdibles extends ItemList {
            public NonEdibles(PatchedComponentHolder holder) {
                  super(holder);
            }

            @Override
            public DataComponentType<List<ItemStack>> type() {
                  return NON_EDIBLES;
            }
      }

      static class Chest extends ITraitData<ItemContainerContents> {
            private final PatchedComponentHolder holder;

            public Chest(PatchedComponentHolder holder) {
                  this.holder = holder;
            }

            @Override
            public DataComponentType<ItemContainerContents> type() {
                  return CHEST;
            }

            @Override
            public PatchedComponentHolder holder() {
                  return holder;
            }

            public ItemContainerContents get() {
                  if (value == null) {
                        markDirty();
                        ItemContainerContents t = holder().get(type());
                        value = Objects.requireNonNullElse(t, ItemContainerContents.EMPTY);
                  }
                  return value;
            }

            @Override
            public boolean isEmpty(ItemContainerContents data) {
                  return data.stream().allMatch(ItemStack::isEmpty);
            }
      }

      static class Amount extends ITraitData<Integer> {
            private final PatchedComponentHolder holder;

            public Amount(PatchedComponentHolder holder) {
                  this.holder = holder;
            }

            @Override
            public DataComponentType<Integer> type() {
                  return AMOUNT;
            }

            @Override
            public PatchedComponentHolder holder() {
                  return holder;
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

      static class BulkList extends ITraitData<BulkMutable.BulkStacks> {
            private final PatchedComponentHolder holder;

            public BulkList(PatchedComponentHolder holder) {
                  this.holder = holder;
            }

            @Override
            public DataComponentType<BulkMutable.BulkStacks> type() {
                  return BULK_STACKS;
            }

            @Override
            public PatchedComponentHolder holder() {
                  return holder;
            }

            @Override
            public boolean isEmpty(BulkMutable.BulkStacks data) {
                  return data.isEmpty();
            }

            @Override
            public BulkMutable.BulkStacks get() {
                  if (value == null) {
                        markDirty();
                        BulkMutable.BulkStacks bulkStacks = holder.get(type());
                        value = Objects.requireNonNullElseGet(bulkStacks,
                                    () -> new BulkMutable.BulkStacks(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), List.of())
                        );
                  }
                  return value;
            }
      }
}
