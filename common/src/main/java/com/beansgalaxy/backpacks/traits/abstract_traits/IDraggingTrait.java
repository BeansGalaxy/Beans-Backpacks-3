package com.beansgalaxy.backpacks.traits.abstract_traits;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.DraggingContainer;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface IDraggingTrait {

      static void runIfPresent(ItemStack backpack, Level level, BiConsumer<IDraggingTrait, PatchedComponentHolder> consumer) {
            Optional<ItemStorageTraits> optionalStorage = ItemStorageTraits.get(backpack);
            if (optionalStorage.isPresent()) {
                  consumer.accept(optionalStorage.get(), PatchedComponentHolder.of(backpack));
                  return;
            }

            Optional<EnderTraits> optionalEnder = EnderTraits.get(backpack);
            if (optionalEnder.isPresent()) {
                  EnderTraits enderTraits = optionalEnder.get();
                  GenericTraits trait = enderTraits.getTrait(level);
                  if (trait instanceof ItemStorageTraits storageTraits)
                        consumer.accept(storageTraits, enderTraits);
            }
      }

      void clickSlot(DraggingContainer container, Player player, PatchedComponentHolder holder);
}
