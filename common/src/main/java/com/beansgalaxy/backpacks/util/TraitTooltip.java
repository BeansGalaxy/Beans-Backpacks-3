package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record TraitTooltip<T extends GenericTraits>(T traits, ItemStack itemStack, ComponentHolder holder, Component title)
            implements TooltipComponent, ComponentHolder {

      @Override
      public <T> @Nullable T remove(DataComponentType<? extends T> type) {
            return holder.remove(type);
      }

      @Override
      public <T> void set(DataComponentType<? super T> type, T trait) {
            holder.set(type, trait);
      }

      @Override
      public <T> @Nullable T get(DataComponentType<? extends T> type) {
            return holder.get(type);
      }
}
