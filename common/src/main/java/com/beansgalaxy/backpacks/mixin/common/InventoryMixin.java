package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.components.StackableComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.data.config.options.ShorthandControl;
import com.beansgalaxy.backpacks.network.serverbound.SyncShorthand;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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
            getShorthand().tick(instance);
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

      @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
      private void getShorthandDestroySpeed(BlockState blockState, CallbackInfoReturnable<Float> cir) {
            if (selected >= items.size()) {
                  Shorthand shorthand = getShorthand();
                  int slot = shorthand.getSelected(instance);
                  if (slot == -1)
                        return;

                  float destroySpeed = shorthand.getItem(slot).getDestroySpeed(blockState);
                  cir.setReturnValue(destroySpeed);
            }
      }

      @Inject(method = "getSelected", at = @At("HEAD"), cancellable = true)
      private void getShorthandSelected(CallbackInfoReturnable<ItemStack> cir) {
            if (selected >= items.size()) {
                  Shorthand shorthand = getShorthand();
                  int slot = shorthand.getSelected(instance);
                  if (slot == -1)
                        return;

                  ItemStack stack = shorthand.getItem(slot);
                  if (stack.isEmpty())
                        shorthand.resetSelected(instance);
                  else
                        cir.setReturnValue(stack);
            }
      }

      @Inject(method = "replaceWith", at = @At("TAIL"))
      private void backpackReplaceWith(Inventory that, CallbackInfo ci) {
            getShorthand().replaceWith(Shorthand.get(that));
      }

      @Inject(method = "dropAll", at = @At("TAIL"))
      private void shorthandDropAll(CallbackInfo ci) {
            Shorthand shorthand = getShorthand();
            if (!ServerSave.CONFIG.keep_shorthand_on_death.get()) {
                  shorthand.dropAll(player.getInventory());
            }
      }

      @Inject(method = "contains(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, pStack)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "contains(Lnet/minecraft/tags/TagKey;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(TagKey<Item> pTag, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (!itemstack.isEmpty() && itemstack.is(pTag)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "contains(Ljava/util/function/Predicate;)Z", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(Predicate<ItemStack> pPredicate, CallbackInfoReturnable<Boolean> cir) {
            Iterable<ItemStack> contents = getShorthand().getContent();
            for (ItemStack itemstack : contents) {
                  if (pPredicate.test(itemstack)) {
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }

      @Inject(method = "removeItem(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"), cancellable = true)
      private void shorthandContains(ItemStack pStack, CallbackInfo ci) {
            Iterator<ItemStack> iterator = getShorthand().getContent().iterator();
            while (iterator.hasNext()) {
                  if (iterator.next() == pStack) {
                        iterator.remove();
                        ci.cancel();
                        return;
                  }
            }
      }

      @Inject(method = "clearContent", at = @At("TAIL"))
      private void shorthandClearContent(CallbackInfo ci) {
            getShorthand().clearContent();
      }

      @Inject(method = "dropAll", at = @At(value = "CONSTANT", args = "intValue=0", shift = At.Shift.BEFORE))
      private void cancelDropAllBackSlot(CallbackInfo ci, @Local LocalRef<List<ItemStack>> list) {
            if (list.get() == beans_Backpacks_3$getBody() && ServerSave.CONFIG.keep_back_on_death.get())
                  list.set(List.of());
      }

      @Inject(method = "swapPaint", cancellable = true, at = @At("HEAD"))
      private void backpacks_handleScroll(double pDirection, CallbackInfo ci) {
            Shorthand shorthand = Shorthand.get(player);
            final int size = shorthand.getContainerSize();

            if (size < 1)
                  return;

            ci.cancel();
            int i = (int)Math.signum(pDirection);

            final int maxShorthandIndex = size - 1;
            final int maximum = 9 + size;

            int selection;
            if (this.selected < 9)
                  selection = this.selected - i;
            else {
                  selection = (maxShorthandIndex - shorthand.selection) + 9 - i;
            }

            while (selection < 0)
                  selection += maximum;

            while (selection >= maximum)
                  selection -= maximum;

            if (selection < 9) {
                  shorthand.selection = -1;
                  selected = selection;
            }
            else {
                  selection = maxShorthandIndex - (selection - 9);

                  while (true) {
                        ItemStack stack = shorthand.getItem(selection);
                        if (!stack.isEmpty()) {
                              shorthand.selection = selection;
                              selected = items.size() + selection;
                              break;
                        }

                        selection += i;

                        if (selection < 0) {
                              shorthand.selection = -1;
                              selected = 0;
                              break;
                        }
                        else if (selection >= size) {
                              shorthand.selection = -1;
                              selected = 8;
                              break;
                        }
                  }

                  SyncShorthand.send(shorthand);
            }
      }
}
