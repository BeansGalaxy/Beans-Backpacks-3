package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Item.class)
public class ItemMixin {

      @Inject(method = "use", at = @At("HEAD"), cancellable = true)
      private void backpackUseOn(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            ItemStack backpack = player.getItemInHand(hand);
            Traits.runIfPresent(backpack, traits -> {
                  traits.use(level, player, hand, ComponentHolder.of(backpack), cir);
            });
      }

      @Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
      private void stackOnBackpack(ItemStack backpack, ItemStack thatStack, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            Traits.runIfPresent(backpack, traits -> {
                  traits.stackedOnMe(ComponentHolder.of(backpack), thatStack, slot, click, player, access, cir);
            });
      }

      @Inject(method = "overrideStackedOnOther", at = @At("HEAD"), cancellable = true)
      private void backpackOnStack(ItemStack backpack, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            Traits.runIfPresent(backpack, traits -> {
                  traits.stackedOnOther(ComponentHolder.of(backpack), slot.getItem(), slot, click, player, cir);
            });
      }

      // =============================================================================================================== LUNCH BOX TRAITS

      @Inject(method = "finishUsingItem", at = @At("HEAD"), cancellable = true)
      private void finishUsingLunchBox(ItemStack backpack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
            ComponentHolder holder = ComponentHolder.of(backpack);
            LunchBoxTraits.ifPresent(backpack, traits -> {
                  traits.finishUsingItem(holder, backpack, level, entity, cir);
            });
      }

      @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
      private void backpackFitInsideContainer(ItemStack backpack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
            LunchBoxTraits.selectionIsPresent(backpack, entity, food -> {
                  cir.setReturnValue(food.getUseDuration(entity));
            });
      }

}
