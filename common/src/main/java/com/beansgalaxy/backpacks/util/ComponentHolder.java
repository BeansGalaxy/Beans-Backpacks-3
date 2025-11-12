package com.beansgalaxy.backpacks.util;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface ComponentHolder {

      static ComponentHolder of(Slot slot) {
            return new SlotTraitHolder(slot);
      }

      static ComponentHolder of(ItemStack stack) {
            return new ItemStackTraitHolder(stack);
      }

      static ComponentHolder of(ItemStack stack, Player player) {
            return new StackReturningTraitHolder(stack, player);
      }

      @Nullable
      <T> T remove(DataComponentType<? extends T> type);

      <T> void set(DataComponentType<? super T> type, T trait);

      @Nullable
      <T> T get(DataComponentType<? extends T> type);

      default boolean has(DataComponentType<?> type) {
            return get(type) != null;
      }

      default void setChanged() {

      }

      @NotNull
      default <T> T getOrElse(DataComponentType<? extends T> type, Supplier<T> orElse) {
            T t = get(type);
            return t == null ? orElse.get() : t;
      }

      default <T> T getOrDefault(DataComponentType<T> type, T defau) {
            T t = get(type);
            if (t == null)
                  return defau;
            return t;
      }
      
      class StackReturningTraitHolder implements ComponentHolder {
            private final ItemStack stack;
            private final Player player;

            public StackReturningTraitHolder(ItemStack stack, Player player) {
                  this.stack = stack;
                  this.player = player;
            }

            @Override @Nullable
            public <T> T remove(DataComponentType<? extends T> type) {
                  int count = stack.getCount();
                  if (count > 1) {
                        ItemStack copy = stack.copyWithCount(1);
                        T remove = copy.remove(type);
                        if (remove == null)
                              return null;

                        player.addItem(copy);
                        stack.shrink(1);
                        return remove;
                  } else
                        return stack.remove(type);
            }

            @Override
            public <T> void set(DataComponentType<? super T> type, T data) {
                  int count = stack.getCount();
                  if (count > 1) {
                        ItemStack copy = stack.copyWithCount(1);
                        copy.set(type, data);
                        player.addItem(copy);
                        stack.shrink(1);
                  } else stack.set(type, data);
            }

            @Override
            public <T> T get(DataComponentType<? extends T> pComponent) {
                  return stack.get(pComponent);
            }

      }

      class ItemStackTraitHolder implements ComponentHolder {
            private final ItemStack stack;

            private ItemStackTraitHolder(ItemStack stack) {
                  this.stack = stack;
            }

            @Override @Nullable
            public <T> T remove(DataComponentType<? extends T> type) {
                  return stack.remove(type);
            }

            @Override
            public <T> void set(DataComponentType<? super T> type, T trait) {
                  stack.set(type, trait);
            }

            @Override
            public <T> T get(DataComponentType<? extends T> type) {
                  return stack.get(type);
            }

      }

      class SlotTraitHolder implements ComponentHolder {
            private final Slot slot;

            public SlotTraitHolder(Slot slot) {
                  this.slot = slot;
            }

            @Override @Nullable
            public <T> T remove(DataComponentType<? extends T> type) {
                  return slot.getItem().remove(type);
            }

            @Override
            public <T> void set(DataComponentType<? super T> type, T trait) {
                  slot.getItem().set(type, trait);
            }

            @Override @Nullable
            public <T> T get(DataComponentType<? extends T> type) {
                  return slot.getItem().get(type);
            }
      }

}
