package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class LunchBoxTraits extends BundleLikeTraits implements ISlotSelectorTrait, IDraggingTrait {
      public static final String NAME = "lunch";

      public LunchBoxTraits(ModSound sound, int size) {
            super(sound, size);
      }

      @Override
      public LunchBoxClient client() {
            return LunchBoxClient.INSTANCE;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Nullable
      public static LunchBoxTraits get(ItemStack stack) {
            LunchBoxTraits traits = stack.get(Traits.LUNCH_BOX);
            if (traits != null) {
                  return traits;
            }

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference == null)
                  return null;

            Optional<GenericTraits> optional = reference.getTrait();
            if (optional.isEmpty())
                  return null;

            if (optional.get() instanceof LunchBoxTraits lunch)
                  return lunch;

            return null;
      }

      public static void ifPresent(ItemStack lunchBox, Consumer<LunchBoxTraits> ifPresent) {
            LunchBoxTraits boxTraits = lunchBox.get(Traits.LUNCH_BOX);
            if (boxTraits != null) {
                  ifPresent.accept(boxTraits);
                  return;
            }

            ReferenceTrait referenceTrait = lunchBox.get(Traits.REFERENCE);
            if (referenceTrait == null)
                  return;

            referenceTrait.getTrait().ifPresent(traits -> {
                  if (traits instanceof LunchBoxTraits lunchBoxTraits)
                        ifPresent.accept(lunchBoxTraits);
            });
      }

      public static void firstIsPresent(ItemStack lunchBox, LivingEntity entity, Consumer<ItemStack> ifPresent) {
            ifPresent(lunchBox, traits -> {
                  LunchBoxMutable mutable = traits.mutable(ComponentHolder.of(lunchBox));
                  if (mutable.isEmpty())
                        return;

                  int selectedSlotSafe = entity instanceof Player player
                              ? mutable.getSelectedSlot(player)
                              : 0;

                  ifPresent.accept(mutable.getItemStacks().get(selectedSlotSafe));
            });
      }

      public void finishUsingItem(ComponentHolder holder, ItemStack backpack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
            LunchBoxMutable mutable = mutable(holder);
            int selectedSlot = entity instanceof Player player
                        ? mutable.getSelectedSlot(player)
                        : 0;

            List<ItemStack> itemStacks = mutable.getItemStacks();
            ItemStack stack = itemStacks.get(selectedSlot);
            ItemStack copy = stack.copyWithCount(1);
            stack.shrink(1);
            if (stack.isEmpty())
                  itemStacks.remove(selectedSlot);

            ItemStack consumedStack = copy.finishUsingItem(level, entity);
            if (!consumedStack.isEmpty()) {
                  for (int i = 0; i < itemStacks.size(); i++) {
                        ItemStack nonEdible = itemStacks.get(i);
                        if (nonEdible.isEmpty()) {
                              itemStacks.remove(i);
                              continue;
                        }

                        if (ItemStack.isSameItemSameComponents(nonEdible, consumedStack)) {
                              ItemStack removed = itemStacks.remove(i);
                              consumedStack.grow(removed.getCount());
                              mutable.limitSelectedSlot(i);
                        }
                  }

                  if (!consumedStack.isEmpty()) {
                        itemStacks.addFirst(consumedStack);
                        if (entity instanceof Player player)
                              mutable.setSelectedSlot(player, mutable.getSelectedSlot(player));

                        mutable.growSelectedSlot(0);
                  }
            }

            mutable.push();
            cir.setReturnValue(backpack);
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            LunchBoxMutable mutable = mutable(holder);
            if (mutable.isEmpty())
                  return;

            int selected = mutable.getSelectedSlot(player);
            ItemStack first = mutable.getItemStacks().get(selected);
            FoodProperties $$4 = first.get(DataComponents.FOOD);
            if ($$4 != null) {
                  if (player.canEat($$4.canAlwaysEat())) {
                        player.startUsingItem(hand);
                        ItemStack backpack = player.getItemInHand(hand);
                        cir.setReturnValue(InteractionResultHolder.consume(backpack));
                  }
            }
      }

      @Override
      public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            return inserted.has(DataComponents.FOOD) && super.canItemFit(holder, inserted);
      }

      @Override
      public LunchBoxMutable mutable(ComponentHolder holder) {
            return new LunchBoxMutable(this, holder);
      }

      @Override
      public String toString() {
            return "LunchBoxTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.LUNCH_BOX;
      }
}
