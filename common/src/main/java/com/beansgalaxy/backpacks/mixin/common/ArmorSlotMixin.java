package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public class ArmorSlotMixin extends Slot implements EquipmentSlotAccess {
      @Shadow @Final private EquipmentSlot slot;

      public ArmorSlotMixin(Container $$0, int $$1, int $$2, int $$3) {
            super($$0, $$1, $$2, $$3);
      }

      @Override
      public EquipmentSlot getSlot() {
            return slot;
      }

      @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
      private void disableBackpackPickup(Player player, CallbackInfoReturnable<Boolean> cir) {
            ItemStack stack = getItem();
            BackpackTraits traits = BackpackTraits.get(stack);
            if (traits == null)
                  return;

            if (!traits.isEmpty(stack))
                  cir.setReturnValue(false);
      }

      @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
      private void enableBackpackEquip(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
            BackpackTraits traits = BackpackTraits.get(pStack);
            if (traits == null)
                  return;

            if (traits.slots().test(slot))
                  cir.setReturnValue(true);
      }
}
