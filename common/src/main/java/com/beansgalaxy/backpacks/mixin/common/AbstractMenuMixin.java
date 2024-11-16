package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractMenuMixin {
      @Shadow public abstract Slot getSlot(int $$0);

      @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
      private void hotKeyPress(int index, int button, ClickType actionType, Player player, CallbackInfo ci) {
            if (index < 0)
                  return;

            boolean menuKeyDown = BackData.get(player).isMenuKeyDown();

            Slot slot = getSlot(index);
            ItemStack stack = slot.getItem();
            if (ClickType.THROW.equals(actionType)) {
                  ItemStorageTraits.runIfPresent(stack, trait -> {
                        trait.hotkeyThrow(slot, PatchedComponentHolder.of(stack), button, player, menuKeyDown, ci);
                  }, () -> EnderTraits.get(stack).ifPresent(enderTraits -> enderTraits.getTrait().ifPresent(traits -> {
                        if (traits instanceof ItemStorageTraits storageTraits) {
                              storageTraits.hotkeyThrow(slot, enderTraits, button, player, menuKeyDown, ci);
                        }
                  })));
                  return;
            }

            if (menuKeyDown) {
                  if (slot instanceof EquipmentSlotAccess) {
                        ItemStorageTraits.runIfPresent(stack, trait -> {
                              trait.hotkeyUse(slot, null, button, actionType, player, ci);
                        });
                  } else {
                        ItemStorageTraits.runIfEquipped(player, (trait, equipmentSlot) -> {
                              trait.hotkeyUse(slot, equipmentSlot, button, actionType, player, ci);
                              return ci.isCancelled();
                        });
                  }
            }
      }
}
