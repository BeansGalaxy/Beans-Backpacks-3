package com.beansgalaxy.backpacks.traits.abstract_traits;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface IDraggingTrait {

      TraitComponentKind<? extends IDraggingTrait>[] TRAITS = new TraitComponentKind[] {
                  Traits.BUNDLE,
                  Traits.LUNCH_BOX,
                  Traits.ALCHEMY,
                  Traits.QUIVER,
                  Traits.CHEST
      };

      @Nullable
      static IDraggingTrait get(ComponentHolder backpack) {
            for (TraitComponentKind<? extends IDraggingTrait> kind : TRAITS) {
                  IDraggingTrait traits = backpack.get(kind);
                  if (traits != null)
                        return traits;
            }

            ReferenceTrait referenceTrait = backpack.get(Traits.REFERENCE);
            if (referenceTrait == null)
                  return null;

            Optional<GenericTraits> reference = referenceTrait.getTrait();
            if (!reference.isEmpty() && reference.get() instanceof IDraggingTrait draggingTrait)
                  return draggingTrait;

            return null;
      }

      static void runIfPresent(ItemStack backpack, Level level, BiConsumer<IDraggingTrait, ComponentHolder> consumer) {
            ComponentHolder holder = ComponentHolder.of(backpack);
            IDraggingTrait draggingTrait = get(holder);
            if (draggingTrait != null) {
                  consumer.accept(draggingTrait, holder);
                  return;
            }

            Optional<EnderTraits> optionalEnder = EnderTraits.get(backpack);
            if (optionalEnder.isPresent()) {
                  EnderTraits enderTraits = optionalEnder.get();
                  GenericTraits trait = enderTraits.getTrait(level);
                  if (trait instanceof IDraggingTrait storageTraits)
                        consumer.accept(storageTraits, enderTraits);
            }
      }

      default void clickSlot(DraggingContainer drag, Player player, ComponentHolder holder) {
            Slot slot = drag.firstSlot;

            if (drag.isPickup) {
                  ItemStack stack = slot.getItem();
                  boolean mayPickup = slot.mayPickup(player);
                  boolean hasItem = slot.hasItem();
                  boolean canFit = canItemFit(holder, stack);
                  boolean isFull = isFull(holder);
                  if (mayPickup && hasItem && canFit && !isFull) {
                        drag.allSlots.put(drag.firstSlot, stack.copyWithCount(1));
                        drag.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                  }
            }
            else {
                  ItemStack itemStack = getFirst(holder);
                  if (itemStack != null && !slot.hasItem()) {
                        if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true) && slot.mayPlace(itemStack)) {
                              drag.allSlots.put(slot, ItemStack.EMPTY);
                              drag.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                        }
                  }
            }
      }

      ItemStack getFirst(ComponentHolder holder);

      boolean isFull(ComponentHolder holder);

      boolean canItemFit(ComponentHolder holder, ItemStack stack);
}
