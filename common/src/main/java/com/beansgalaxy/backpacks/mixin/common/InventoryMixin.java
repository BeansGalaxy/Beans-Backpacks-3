package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.StackableComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements BackData {
      @Shadow @Final public Player player;
      @Shadow @Final public NonNullList<ItemStack> items;
      @Shadow public int selected;

      @Shadow public abstract ItemStack getItem(int pIndex);

      @Inject(method = "tick", at = @At("TAIL"))
      public void tickCarriedBackpack(CallbackInfo ci)
      {
            ItemStack carried = player.containerMenu.getCarried();
            Level level = player.level();
            Traits.get(carried).ifPresent(traits ->
                        carried.inventoryTick(level, player, -1, false)
            );

            getUtility().tick(instance);

            for (Slot slot : player.containerMenu.slots) {
                  ItemStack stack = slot.getItem();
                  EnderTraits.get(stack).ifPresent(enderTraits -> {
                        if (!enderTraits.isLoaded())
                              enderTraits.reload(level);

                        if (player instanceof ServerPlayer serverPlayer) {
                              enderTraits.addListener(serverPlayer);
                        }
                  });
            }
      }

      @Unique @Final public Inventory instance = (Inventory) (Object) this;

      @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "HEAD"), cancellable = true)
      public void addToBackpackBeforeInventory(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!stack.isEmpty()) {
                  Optional<EquipableComponent> optional = EquipableComponent.get(stack);
                  if (optional.isPresent()) {
                        EquipableComponent equipable = optional.get();

                        if (Traits.testIfPresent(stack, traits -> !traits.isEmpty(PatchedComponentHolder.of(stack)))) {
                              for (EquipmentSlot value : equipable.values()) {
                                    ItemStack itemBySlot = player.getItemBySlot(value);
                                    if (!itemBySlot.isEmpty())
                                          continue;

                                    player.setItemSlot(value, stack.copy());
                                    stack.setCount(0);
                                    cir.setReturnValue(true);
                                    return;
                              }

                              if (!equipable.traitRemovable() && !player.isCreative()) {
                                    cir.setReturnValue(false);
                                    return;
                              }
                        }
                  }

                  ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) -> {
                        ItemStack backpack = player.getItemBySlot(equipmentSlot);
                        return traits.pickupToBackpack(player, equipmentSlot, instance, backpack, stack, cir);
                  });

                  if (ServerSave.CONFIG.do_nbt_stacking.get())
                        backpacks_tryStackingComponent(stack, cir);
            }
      }

      @Unique
      private void backpacks_tryStackingComponent(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack selectedStack = this.getItem(this.selected);
            if (this.backpacks_hasSpaceForStackable(selectedStack, stack)) {
                  if (StackableComponent.stackItems(instance, selected, selectedStack, stack)) {
                        cir.setReturnValue(true);
                  }
            }

            ItemStack offHandStack = this.getItem(40);
            if (this.backpacks_hasSpaceForStackable(offHandStack, stack)) {
                  if (StackableComponent.stackItems(instance, 40, offHandStack, stack)) {
                        cir.setReturnValue(true);
                  }
            }

            for(int i = 0; i < this.items.size(); ++i) {
                  ItemStack destination = this.items.get(i);
                  if (this.backpacks_hasSpaceForStackable(destination, stack)) {
                        if (StackableComponent.stackItems(instance, i, destination, stack)) {
                              cir.setReturnValue(true);
                        }
                  }
            }
      }

      @Unique
      private boolean backpacks_hasSpaceForStackable(ItemStack pDestination, ItemStack pOrigin) {
            if (pOrigin.isEmpty())
                  return true;

            return pOrigin.getCount() < pOrigin.getMaxStackSize()
                        && !pDestination.isEmpty()
                        && ItemStack.isSameItem(pDestination, pOrigin)
                        && pDestination.isStackable()
                        && pDestination.getCount() < pDestination.getMaxStackSize();
      }

      @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
      public void addToBackpackAfterInventory(int $$0, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!cir.getReturnValue()) {
                  ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) ->
                              traits.overflowFromInventory(equipmentSlot, player, stack, cir)
                  );
            }
      }

      @Inject(method = "dropAll", at = @At(value = "CONSTANT", args = "intValue=0", shift = At.Shift.BEFORE))
      private void cancelDropAllBackSlot(CallbackInfo ci, @Local LocalRef<List<ItemStack>> list) {
            if (list.get() == beans_Backpacks_3$getBody() && ServerSave.CONFIG.keep_back_on_death.get())
                  list.set(List.of());
      }

}
