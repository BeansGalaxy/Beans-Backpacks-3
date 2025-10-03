package com.beansgalaxy.backpacks.traits.abstract_traits;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.network.serverbound.SyncSelectedSlot;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.alchemy.AlchemyTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public interface ISlotSelectorTrait {
      TraitComponentKind<? extends ISlotSelectorTrait>[] TRAITS = new TraitComponentKind[]{
                  Traits.LUNCH_BOX,
                  Traits.ALCHEMY,
                  Traits.QUIVER
      };

      static ISlotSelectorTrait get(ItemStack stack) {
            for (TraitComponentKind<? extends ISlotSelectorTrait> traits : TRAITS) {
                  ISlotSelectorTrait trait = stack.get(traits);
                  if (trait != null)
                        return trait;
            }

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference == null)
                  return null;

            Optional<GenericTraits> optional = reference.getTrait();
            if (optional.isPresent() && optional.get() instanceof ISlotSelectorTrait trait)
                  return trait;

            return null;
      }

      static BundleLikeTraits getFoodStuffsTrait(ItemStack stack) {
            LunchBoxTraits lunch = stack.get(Traits.LUNCH_BOX);
            if (lunch != null)
                  return lunch;

            AlchemyTraits alchemy = stack.get(Traits.ALCHEMY);
            if (alchemy != null)
                  return alchemy;

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference == null)
                  return null;

            Optional<GenericTraits> optional = reference.getTrait();
            if (optional.isEmpty())
                  return null;

            if (optional.get() instanceof LunchBoxTraits lunchRef)
                  return lunchRef;

            if (optional.get() instanceof AlchemyTraits alchemyRef)
                  return alchemyRef;

            return null;
      }

      @Nullable
      static ItemStack getFoodStuffsSelection(ItemStack lunchBox, Player player) {
            BundleLikeTraits traits = getFoodStuffsTrait(lunchBox);
            if (traits == null)
                  return null;

            MutableBundleLike<?> mutable = traits.mutable(ComponentHolder.of(lunchBox));
            if (mutable.isEmpty())
                  return null;

            int selectedSlotSafe = mutable.getSelectedSlot(player);
            return mutable.getItemStacks().get(selectedSlotSafe);
      }

      default boolean mouseScrolled(Player player, ComponentHolder holder, Level level, Slot hoveredSlot, int containerId, int scrolled) {
            MutableSlotSelector<?> mutable = mutable(holder);
            int startSlot = mutable.getSelectedSlot(player);

            int i = mutable.stepScrollTo(startSlot, scrolled);
            if (i == startSlot)
                  return false;

            mutable.setSelectedSlot(player, i);
            SyncSelectedSlot.send(containerId, hoveredSlot.index, i);

            return true;
      }

      MutableSlotSelector<?> mutable(ComponentHolder holder);

      boolean isFull(ComponentHolder holder);

      private SlotSelection getSlotSelection(ComponentHolder holder) {
            SlotSelection slotSelection = holder.get(ITraitData.SLOT_SELECTION);
            if (slotSelection != null)
                  return slotSelection;

            SlotSelection selection = new SlotSelection();
            holder.set(ITraitData.SLOT_SELECTION, selection);
            return selection;
      }

      default int getSelectedSlot(ComponentHolder holder, Player player) {
            return getSlotSelection(holder).getSelectedSlot(player);
      }

      @Nullable
      default ItemStack getHoverItem(ComponentHolder holder, Player player) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return null;

            int selectedSlot = getSelectedSlot(holder, player);
            return stacks.get(selectedSlot);
      }


}
