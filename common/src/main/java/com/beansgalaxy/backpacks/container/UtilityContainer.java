package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class UtilityContainer implements Container {
      private final BackData owner;
      public byte size;

      public UtilityContainer(BackData owner) {
            this.owner = owner;
      }

      public static UtilityContainer get(Player player) {
            BackData backData = BackData.get(player);
            return backData.getUtility();
      }

      @Override
      public int getContainerSize() {
            return size;
      }

      @Override
      public boolean isEmpty() {
            return getUtility().isEmpty();
      }

      @Override
      public ItemStack getItem(int i) {
            return getUtility().get(i);
      }

      @Override
      public ItemStack removeItem(int i, int amount) {
            return mapMutable(mute -> {
                  ItemStack itemStack = mute.slots.get(i);
                  if (itemStack.isEmpty()) {
                        return mute.slots.remove(i);
                  }

                  int count = itemStack.getCount();
                  if (count <= amount) {
                        return mute.slots.remove(i);
                  }

                  ItemStack copy = itemStack.copyWithCount(amount);
                  itemStack.shrink(amount);
                  return copy;
            }, ItemStack.EMPTY);
      }

      @Override
      public ItemStack removeItemNoUpdate(int i) {
            return mapMutable(mute -> mute.slots.remove(i), ItemStack.EMPTY);
      }

      @Override
      public void setItem(int i, ItemStack stack) {
            getMutable().ifPresent(mute -> {
                  mute.slots.put(i, stack);
                  mute.freeze();
            });
      }

      private Optional<UtilityComponent.Mutable> getMutable() {
            ItemStack backpack = owner.beans_Backpacks_3$getBody().getFirst();
            if (backpack.isEmpty())
                  return Optional.empty();

            return UtilityComponent.get(backpack);
      }

      private <T> T mapMutable(Function<UtilityComponent.Mutable, T> map, T orElse) {
            Optional<UtilityComponent.Mutable> mutable = getMutable();
            if (mutable.isEmpty())
                  return orElse;

            UtilityComponent.Mutable mute = mutable.get();
            T apply = map.apply(mute);
            mute.freeze();
            return apply;
      }

      private UtilityComponent getUtility() {
            ItemStack backpack = owner.beans_Backpacks_3$getBody().getFirst();
            return backpack.getOrDefault(ITraitData.UTILITIES, UtilityComponent.BLANK);
      }

      @Override
      public void setChanged() {

      }

      @Override
      public boolean stillValid(Player player) {
            return true;
      }

      @Override
      public void clearContent() {
            ItemStack backpack = owner.beans_Backpacks_3$getBody().getFirst();
            backpack.remove(ITraitData.UTILITIES);
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UtilityContainer container)) return false;
            return size == container.size && Objects.equals(owner, container.owner);
      }

      @Override
      public int hashCode() {
            return Objects.hash(owner, size);
      }
}
