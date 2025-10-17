package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.network.clientbound.SendItemComponentPatch;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.datafixers.util.Function4;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class QuiverTraits extends ChestLikeTraits implements ISlotSelectorTrait, IDraggingTrait {
      public static final String NAME = "quiver";

      public QuiverTraits(ModSound sound, int size) {
            super(sound, size);
      }

      public static QuiverTraits get(ItemStack stack) {
            return Traits.get(ComponentHolder.of(stack), Traits.QUIVER);
      }

      public static void runIfPresent(Player player, Function4<QuiverTraits, Integer, ItemStack, ComponentHolder, Boolean> runnable) {
            int[] i = {-1};

            player.getInventory().contains(stack -> {
                  i[0]++;

                  QuiverTraits trait = get(stack);
                  ComponentHolder holder;
                  if (trait == null) {
                        Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
                        if (optionalEnder.isEmpty()) {
                              return false;
                        }

                        EnderTraits ender = optionalEnder.get();
                        GenericTraits generic = ender.getTrait(player.level());
                        if (generic instanceof QuiverTraits quiverTraits) {
                              trait = quiverTraits;
                              holder = ender;
                        }
                        else return false;
                  }
                  else holder = ComponentHolder.of(stack);

                  return runnable.apply(trait, i[0], stack, holder);
            });
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public QuiverClient client() {
            return QuiverClient.INSTANCE;
      }

      public boolean pickupToQuiver(Player player, int slot, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            Fraction fraction = stacks == null || stacks.isEmpty()
                        ? Fraction.ZERO
                        : Traits.getWeight(stacks);

            int i = Fraction.getFraction(size(), 1).compareTo(fraction);
            if (i <= 0)
                  return false;

            MutableBundleLike<QuiverTraits> mutable = this.mutable(ComponentHolder.of(backpack));
            if (mutable.addItem(stack) != null) {
                  cir.setReturnValue(true);
                  sound().toClient(player, ModSound.Type.INSERT, 1, 1);
                  mutable.push();

                  if (player instanceof ServerPlayer serverPlayer)
                        SendItemComponentPatch.send(serverPlayer, slot, backpack);
            }

            return stack.isEmpty();
      }

      @Override
      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            if (isFull(backpack))
                  return false;
            else
                  return super.overflowFromInventory(equipmentSlot, player, stack, cir);
      }

      @Override
      public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            return canInsertProjectile(inserted.getItem()) && super.canItemFit(holder, inserted);
      }

      public boolean canInsertProjectile(Item item) {
            return item instanceof ArrowItem;
      }

      @Override
public QuiverMutable mutable(ComponentHolder holder) {
            return new QuiverMutable(this, holder);
      }

      @Override
      public int getSelectedSlot(ComponentHolder holder, Player instance) {
            return ISlotSelectorTrait.super.getSelectedSlot(holder, instance);
      }

      @Override
      public String toString() {
            return "QuiverTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.QUIVER;
      }
}
