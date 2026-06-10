package com.beansgalaxy.backpacks.components.ender;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.Cancellable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class EnderItem extends Item {

      public EnderItem(Properties properties) {
            super(properties.stacksTo(1));
      }

      private static void runIfPresent(ItemStack ender, LivingEntity entity, Cancellable cir, BiConsumer<EnderTraits, GenericTraits> consumer) {
            getEnderTrait(ender).ifPresent(enderTraits -> {
                  Level level = entity.level();
                  GenericTraits traits = enderTraits.getTrait(level);
                  consumer.accept(enderTraits, traits);
            });
      }

      public static Optional<EnderTraits> getEnderTrait(ItemStack ender) {
            return Optional.ofNullable(ender.get(Traits.ENDER));
      }

      @Override
      public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
            return InteractionResult.PASS;
      }

      @Override
      public boolean overrideOtherStackedOnMe(ItemStack ender, ItemStack $$1, Slot $$2, ClickAction $$3, Player player, SlotAccess $$5) {
            EnderCallback<Boolean> cir = EnderCallback.of(false);
            runIfPresent(ender, player, cir, (enderTraits, genericTraits) ->
                        genericTraits.stackedOnMe(enderTraits, $$1, $$2, $$3, player, $$5, cir)
            );
            return cir.getReturnValue();
      }

      @Override
      public boolean overrideStackedOnOther(ItemStack ender, Slot slot, ClickAction $$2, Player player) {
            EnderCallback<Boolean> cir = EnderCallback.of(false);
            runIfPresent(ender, player, cir, (enderTraits, genericTraits) ->
                        genericTraits.stackedOnOther(enderTraits, slot.getItem(), slot, $$2, player, cir)
            );
            return cir.getReturnValue();
      }

      // ==================================================================================================================== CLIENT SYNC ONLY

      @Override
      public ItemStack finishUsingItem(ItemStack ender, Level pLevel, LivingEntity entity) {
            EnderCallback<ItemStack> cir = EnderCallback.of(ender);
            runIfPresent(ender, entity, cir, (enderTraits, genericTraits) -> {
                  if (genericTraits instanceof LunchBoxTraits lunchBoxTraits) {
                        lunchBoxTraits.finishUsingItem(enderTraits, ender, pLevel, entity, cir);
                  }
            });

            return cir.getReturnValue();
      }

      @Override
      public int getUseDuration(ItemStack ender, LivingEntity entity) {
            EnderCallback<Integer> cir = EnderCallback.of(0);

            getEnderTrait(ender).ifPresent(enderTraits -> {
                  GenericTraits trait = enderTraits.getTrait(entity.level());
                  if (trait instanceof LunchBoxTraits lunchBoxTraits) {
                        List<ItemStack> stacks = enderTraits.get(ITraitData.ITEM_STACKS);
                        if (stacks == null || stacks.isEmpty())
                              return;

                        int selectedSlotSafe;
                        if (entity instanceof Player player) {
                              SlotSelection selection = enderTraits.get(ITraitData.SLOT_SELECTION);
                              selectedSlotSafe = selection != null
                                                 ? selection.get(player)
                                                 : 0;
                        }
                        else selectedSlotSafe = 0;

                        ItemStack first = stacks.get(selectedSlotSafe);
                        cir.setReturnValue(first.getUseDuration(entity));
                  }
            });

            if (cir.isCancelled())
                  return cir.getReturnValue();

            return super.getUseDuration(ender, entity);
      }

      @Override
      public ItemUseAnimation getUseAnimation(ItemStack ender) {
            EnderCallback<ItemUseAnimation> cir = EnderCallback.of(ItemUseAnimation.NONE);
            getEnderTrait(ender).ifPresent(enderTraits -> {
                  enderTraits.getTrait().ifPresent(trait -> {
                        if (trait instanceof LunchBoxTraits) {
                              List<ItemStack> stacks = enderTraits.get(ITraitData.ITEM_STACKS);
                              if (stacks == null || stacks.isEmpty())
                                    return;

                              cir.setReturnValue(ItemUseAnimation.EAT);
                        }
                  });
            });

            if (cir.isCancelled())
                  return cir.getReturnValue();

            return super.getUseAnimation(ender);
      }
      
      public static class EnderCallback<R> extends CallbackInfoReturnable<R> {
            private EnderCallback(R returnValue) {
                  super("ender", true, returnValue);
            }

            static <R> EnderCallback<R> of(R returnValue) {
                  return new EnderCallback<>(returnValue);
            }
      }

}
