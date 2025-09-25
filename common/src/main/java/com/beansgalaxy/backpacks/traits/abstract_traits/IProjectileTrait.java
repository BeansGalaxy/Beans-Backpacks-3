package com.beansgalaxy.backpacks.traits.abstract_traits;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Function4;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface IProjectileTrait {
      TraitComponentKind<? extends IProjectileTrait>[] TRAITS = new TraitComponentKind[]{
                  Traits.QUIVER
      };

      static IProjectileTrait get(ItemStack stack) {
            for (TraitComponentKind<? extends IProjectileTrait> kind : TRAITS) {
                  IProjectileTrait trait = stack.get(kind);
                  if (trait != null)
                        return trait;
            }

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference == null)
                  return null;

            Optional<GenericTraits> optional = reference.getTrait();
            if (optional.isEmpty())
                  return null;

            return optional.get() instanceof IProjectileTrait proTrait
                   ? proTrait
                   : null;
      }

      static void runIfEquipped(Player player, Function4<IProjectileTrait, @Nullable EquipmentSlot, ItemStack, PatchedComponentHolder, Boolean> runnable) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                  ItemStack stack = player.getItemBySlot(slot);
                  if (stack.isEmpty())
                        continue;

                  IProjectileTrait trait = get(stack);
                  PatchedComponentHolder holder;
                  if (trait == null) {
                        Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
                        if (optionalEnder.isEmpty()) {
                              continue;
                        }

                        EnderTraits ender = optionalEnder.get();
                        GenericTraits generic = ender.getTrait(player.level());
                        if (generic instanceof IProjectileTrait quiverTraits) {
                              trait = quiverTraits;
                              holder = ender;
                        }
                        else continue;
                  }
                  else {
                        holder = PatchedComponentHolder.of(stack);
                  }

                  if (runnable.apply(trait, slot, stack, holder))
                        return;
            }
      }

      MutableBundleLike<?> mutable(PatchedComponentHolder holder);

      int getSelectedSlotSafe(PatchedComponentHolder holder, Player instance);
}
