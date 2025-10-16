package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.network.serverbound.TraitMenuClick;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.bundle.BundleScreen;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class ChestLikeTraits extends BundleLikeTraits {
      public ChestLikeTraits(ModSound sound, int size) {
            super(sound, size);
      }
      
      @Override public Fraction fullness(ComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return Fraction.ZERO;
            
            return Fraction.getFraction(stacks.size(), size());
      }
      
      @Override
      public boolean isFull(ComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null || stacks.isEmpty() || stacks.size() < size())
                  return false;
            
            for (ItemStack stack : stacks) {
                  int maxStackSize = stack.getMaxStackSize();
                  int count = stack.getCount();
                  if (maxStackSize != count)
                        return false;
            }
            
            return true;
      }
      
      @Override public void stackedOnOther(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (!ClickAction.SECONDARY.equals(click)) return;
            
            MutableBundleLike<?> mutable = mutable(backpack);
            ModSound sound = sound();
            if (other.isEmpty()) {
                  ItemStack stack = mutable.removeItem(other, player);
                  if (stack.isEmpty() || !slot.mayPlace(stack)) return;
                  
                  slot.set(stack);
                  sound.atClient(player, ModSound.Type.REMOVE);
            }
            else if (slot.mayPickup(player)) {
                  if (mutable.addItem(other, 0) != null)
                        sound.atClient(player, ModSound.Type.INSERT);
            }
            else return;
            
            mutable.push(cir);
      }
      
      @Override public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            return !inserted.isEmpty() && super.canItemFit(holder, inserted);
      }
      
      public abstract MutableChestLike<?> mutable(ComponentHolder holder);
      
      @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChestLikeTraits that)) return false;
            if (!super.equals(o)) return false;
            return size() == that.size() && Objects.equals(sound(), that.sound());
      }
      
      @Override public int hashCode() {
            return Objects.hash(sound(), size());
      }
      
      
      @Override public void tinyMenuClick(ComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player) {
            MutableBundleLike<?> mutable = mutable(holder);
            if (clickType.isHotbar()) {
                  Inventory inventory = player.getInventory();
                  ItemStack hotbarStack = inventory.items.get(clickType.hotbarSlot);
                  ItemStack stack = mutable.removeItem(index);
                  if (!hotbarStack.isEmpty()) {
                        int add = mutable.toAdd(hotbarStack);
                        if (add < hotbarStack.getCount()) {
                              return;
                        }
                        
                        mutable.addItem(hotbarStack, index);
                  }
                  
                  sound().at(player, ModSound.Type.REMOVE);
                  inventory.items.set(clickType.hotbarSlot, stack);
                  mutable.push();
                  return;
            }
            
            if (clickType.isShift()) {
                  Inventory inventory = player.getInventory();
                  ItemStack stack = mutable.removeItem(index);
                  int size = inventory.items.size();
                  
                  int i = 9;
                  do {
                        if (i == size) i = 0;
                        
                        ItemStack hotbar = inventory.items.get(i);
                        if (ItemStack.isSameItemSameComponents(stack, hotbar)) {
                              int add = Math.min(hotbar.getMaxStackSize() - hotbar.getCount(), stack.getCount());
                              hotbar.grow(add);
                              stack.shrink(add);
                        }
                        
                        if (stack.isEmpty()) {
                              mutable.push();
                              sound().at(player, ModSound.Type.INSERT);
                              return;
                        }
                        
                        i++;
                  } while (i != 9);
                  
                  do {
                        if (i == size) i = 0;
                        
                        ItemStack hotbar = inventory.items.get(i);
                        if (hotbar.isEmpty()) {
                              int add = Math.min(stack.getMaxStackSize(), stack.getCount());
                              inventory.items.set(i, stack.copyWithCount(add));
                              stack.shrink(add);
                        }
                        
                        if (stack.isEmpty()) {
                              mutable.push();
                              sound().at(player, ModSound.Type.INSERT);
                              return;
                        }
                        
                        i++;
                  } while (i != 9);
            }
            
            if (clickType.isAction()) {
                  if (index == -1) return;
                  
                  List<ItemStack> stacks = mutable.stacks.get();
                  if (index >= stacks.size()) return;
                  
                  ItemStack stack = stacks.get(index);
                  BackpackTraits.runIfEquipped(player, ((storageTraits, slot) -> {
                        ItemStack backpack = player.getItemBySlot(slot);
                        MutableItemStorage itemStorage = storageTraits.mutable(ComponentHolder.of(backpack));
                        if (canItemFit(holder, stack)) {
                              if (itemStorage.addItem(stack) != null) {
                                    mutable.push();
                                    sound().atClient(player, ModSound.Type.INSERT);
                                    itemStorage.push();
                              }
                        }
                        
                        return stack.isEmpty();
                  }));
            }
            
            if (clickType.isDrop()) {
                  ItemStack stack = mutable.removeItem(index);
                  player.drop(stack, true);
                  mutable.push();
                  return;
            }
            
            List<ItemStack> stacks = mutable.getItemStacks();
            ItemStack carried = carriedAccess.get();
            
            if (index == -1) {
                  if (clickType.isRight()) {
                        if (mutable.addItem(carried.copyWithCount(1)) != null) {
                              carried.shrink(1);
                              mutable.push();
                              sound().at(player, ModSound.Type.INSERT);
                        }
                  }
                  else if (mutable.addItem(carried) != null) {
                        mutable.push();
                        sound().at(player, ModSound.Type.INSERT);
                  }
                  return;
            }
            
            int size = stacks.size();
            if (index >= size) {
                  if (clickType.isRight()) {
                        if (mutable.addItem(carried.copyWithCount(1), size) != null) {
                              carried.shrink(1);
                              mutable.push();
                              sound().atClient(player, ModSound.Type.INSERT);
                        }
                  }
                  else if (mutable.addItem(carried, size) != null) {
                        mutable.push();
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  return;
            }
            
            ItemStack stack = stacks.get(index);
            if (stack.isEmpty() && carried.isEmpty()) return;
            
            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        int toAdd = mutable.toAdd(carried);
                        if (toAdd == 0) return;
                        
                        if (clickType.isRight()) {
                              stack.grow(1);
                              carried.shrink(1);
                        }
                        else {
                              int add = Math.min(stack.getMaxStackSize() - stack.getCount(), toAdd);
                              stack.grow(add);
                              carried.shrink(add);
                        }
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  else if (mutable.addItem(carried, index) != null) {
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
            }
            else if (clickType.isRight()) {
                  int count = Mth.ceil((float) stack.getCount() / 2);
                  ItemStack split = stack.split(count);
                  carriedAccess.set(split);
                  sound().atClient(player, ModSound.Type.REMOVE);
            }
            else if (carried.isEmpty()) {
                  ItemStack removed = mutable.removeItem(index);
                  carriedAccess.set(removed);
                  sound().atClient(player, ModSound.Type.REMOVE);
            }
            else if (mutable.addItem(carried, index + 1) != null) {
                  sound().atClient(player, ModSound.Type.INSERT);
            }
            
            mutable.push();
      }
      
      @Override public void tinyHotbarClick(ComponentHolder holder, int slotId, TinyClickType clickType, InventoryMenu menu, Player player) {
            if (TinyClickType.I_SHIFT.equals(clickType)) {
                  Slot slot = menu.getSlot(slotId);
                  ItemStack hotbar = slot.getItem();
                  MutableBundleLike<?> mutable = mutable(holder);
                  if (mutable.addItem(hotbar, mutable.getItemStacks().size()) != null) {
                        sound().atClient(player, ModSound.Type.INSERT);
                        mutable.push();
                  }
                  return;
            }
            
            super.tinyHotbarClick(holder, slotId, clickType, menu, player);
      }
      
      @Override public void onPlayerInteract(LivingEntity owner, Player player, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {
//            if (player.level().isClientSide) {
//                  ViewableBackpack viewable = ViewableBackpack.get(owner);
//                  BundleScreen.openScreen(player, viewable, this);
//            }
            cir.setReturnValue(InteractionResult.SUCCESS);
      }
      
      @Override public void menuClick(ComponentHolder holder, int index, TraitMenuClick.Kind clickType, SlotAccess carriedAccess, Player player) {
            MutableBundleLike<?> mutable = mutable(holder);
//            if (clickType.isHotbar()) {
//                  Inventory inventory = player.getInventory();
//                  ItemStack hotbarStack = inventory.items.get(clickType.hotbarSlot);
//                  ItemStack stack = mutable.removeItem(index);
//                  if (!hotbarStack.isEmpty()) {
//                        int add = mutable.toAdd(hotbarStack);
//                        if (add < hotbarStack.getCount()) {
//                              return;
//                        }
//
//                        mutable.addItem(hotbarStack, index, player);
//                  }
//
//                  sound().at(player, ModSound.Type.REMOVE);
//                  inventory.items.set(clickType.hotbarSlot, stack);
//                  mutable.push();
//                  return;
//            }
            
            if (clickType.isShift()) {
                  Inventory inventory = player.getInventory();
                  ItemStack stack = mutable.removeItem(index);
                  int size = inventory.items.size();
                  
                  int i = 9;
                  do {
                        if (i == size) i = 0;
                        
                        ItemStack hotbar = inventory.items.get(i);
                        if (ItemStack.isSameItemSameComponents(stack, hotbar)) {
                              int add = Math.min(hotbar.getMaxStackSize() - hotbar.getCount(), stack.getCount());
                              hotbar.grow(add);
                              stack.shrink(add);
                        }
                        
                        if (stack.isEmpty()) {
                              mutable.push();
                              sound().at(player, ModSound.Type.INSERT);
                              return;
                        }
                        
                        i++;
                  } while (i != 9);
                  
                  do {
                        if (i == size) i = 0;
                        
                        ItemStack hotbar = inventory.items.get(i);
                        if (hotbar.isEmpty()) {
                              int add = Math.min(stack.getMaxStackSize(), stack.getCount());
                              inventory.items.set(i, stack.copyWithCount(add));
                              stack.shrink(add);
                        }
                        
                        if (stack.isEmpty()) {
                              mutable.push();
                              sound().at(player, ModSound.Type.INSERT);
                              return;
                        }
                        
                        i++;
                  } while (i != 9);
            }
            
            if (clickType.isAction()) {
                  if (index == -1) return;
                  
                  List<ItemStack> stacks = mutable.stacks.get();
                  if (index >= stacks.size()) return;
                  
                  ItemStack stack = stacks.get(index);
                  BackpackTraits.runIfEquipped(player, ((storageTraits, slot) -> {
                        ItemStack backpack = player.getItemBySlot(slot);
                        MutableItemStorage itemStorage = storageTraits.mutable(ComponentHolder.of(backpack));
                        if (canItemFit(holder, stack)) {
                              if (itemStorage.addItem(stack) != null) {
                                    mutable.push();
                                    sound().atClient(player, ModSound.Type.INSERT);
                                    itemStorage.push();
                              }
                        }
                        
                        return stack.isEmpty();
                  }));
            }
            
            if (clickType.isDrop()) {
                  BackData backData = BackData.get(player);
                  ItemStack stack;
                  if (backData.isMenuKeyDown()) {
                        stack = mutable.removeItem(index);
                  }
                  else {
                        ItemStack item = mutable.getItemStacks().get(index);
                        stack = item.copyWithCount(1);
                        item.shrink(1);
                  }
                  
                  player.drop(stack, true);
                  mutable.push();
                  return;
            }
            
            List<ItemStack> stacks = mutable.getItemStacks();
            ItemStack carried = carriedAccess.get();
            
            int size = stacks.size();
            if (index >= size) {
                  if (clickType.isRight()) {
                        if (mutable.addItem(carried.copyWithCount(1), size) != null) {
                              carried.shrink(1);
                              mutable.push();
                              sound().atClient(player, ModSound.Type.INSERT);
                        }
                  }
                  else if (mutable.addItem(carried, size) != null) {
                        mutable.push();
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  return;
            }
            
            ItemStack stack = stacks.get(index);
            if (stack.isEmpty() && carried.isEmpty()) return;
            
            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        int toAdd = mutable.toAdd(carried);
                        if (toAdd == 0) return;
                        
                        if (clickType.isRight()) {
                              stack.grow(1);
                              carried.shrink(1);
                        }
                        else {
                              stack.grow(toAdd);
                              carried.shrink(toAdd);
                        }
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  else if (clickType.isRight()) {
                        if (mutable.addItem(carried.copyWithCount(1), index) != null) {
                              carried.shrink(1);
                              sound().atClient(player, ModSound.Type.INSERT);
                        }
                  }
                  else if (mutable.addItem(carried, index) != null) {
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
            }
            else if (clickType.isRight()) {
                  int count = Mth.ceil((float) Math.min(stack.getMaxStackSize(), stack.getCount()) / 2);
                  ItemStack split = stack.split(count);
                  carriedAccess.set(split);
                  sound().atClient(player, ModSound.Type.REMOVE);
            }
            else if (carried.isEmpty()) {
                  ItemStack removed = mutable.removeItem(index);
                  carriedAccess.set(removed);
                  sound().atClient(player, ModSound.Type.REMOVE);
            }
            else if (mutable.addItem(carried, index + 1) != null) {
                  sound().atClient(player, ModSound.Type.INSERT);
            }
            
            mutable.push();
      }
}
