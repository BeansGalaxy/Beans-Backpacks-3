package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.network.serverbound.PickBlock;
import com.beansgalaxy.backpacks.network.serverbound.TraitMenuClick;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.bundle.BundleScreen;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
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

import java.util.*;

public abstract class BundleLikeTraits extends ItemStorageTraits {
      private final int size;

      public BundleLikeTraits(ModSound sound, int size) {
            super(sound);
            this.size = size;
      }

      public static Optional<BundleLikeTraits> get(ComponentHolder stack) {
            for (TraitComponentKind<? extends BundleLikeTraits> type : TraitComponentKind.BUNDLE_TRAITS) {
                  BundleLikeTraits traits = stack.get(type);
                  if (traits != null)
                        return Optional.of(traits);
            }

            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null)
                  return referenceTrait.getTrait().map(traits -> {
                        if (traits instanceof BundleLikeTraits storageTraits)
                              return storageTraits;
                        return null;
                  });

            return Optional.empty();
      }

      public int size() {
            return size;
      }

      @Override
      public Fraction fullness(ComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null) {
                  return Fraction.ZERO;
            }

            return fullness(stacks);
      }

      public Fraction fullness(List<ItemStack> stacks) {
            return Traits.getWeight(stacks, size());
      }

      @Override
      public boolean isEmpty(ComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            return stacks == null || stacks.isEmpty();
      }

      @Override
      public int getAnalogOutput(ComponentHolder holder) {
            Fraction fullness = fullness(holder);
            if (fullness.compareTo(Fraction.ZERO) == 0)
                  return 0;

            Fraction maximum = Fraction.getFraction(Math.min(size(), 15), 1);
            Fraction fraction = fullness.multiplyBy(maximum);
            return fraction.intValue();
      }

      @Override @Nullable
      public ItemStack getFirst(ComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            return stacks == null ? null : stacks.getFirst();
      }

      @Override
      public void stackedOnMe(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
      }

      @Override
      public void stackedOnOther(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (!ClickAction.SECONDARY.equals(click))
                  return;

            MutableBundleLike<?> mutable = mutable(backpack);
            ModSound sound = sound();
            if (other.isEmpty()) {
                  ItemStack stack = mutable.removeItem(other, player);
                  if (stack.isEmpty() || !slot.mayPlace(stack))
                        return;

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

      @Override
      public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            return !inserted.isEmpty() && super.canItemFit(holder, inserted);
      }

      public abstract MutableBundleLike<?> mutable(ComponentHolder holder);

      @Override
      public void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci) {
            if (selectedEquipment == null) {
                  ComponentHolder holder = ComponentHolder.of(slot.getItem());
                  MutableBundleLike<?> mutable = mutable(holder);
                  if (mutable.isEmpty()) {
                        ci.cancel();
                        return;
                  }

                  Inventory inventory = player.getInventory();
                  int selectedSlot = mutable.getSelectedSlot(player);
                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        ItemStack carried = player.containerMenu.getCarried();
                        List<ItemStack> stacks = mutable.getItemStacks();
                        ItemStack selectedStack = stacks.get(selectedSlot);
                        if (!ItemStack.isSameItemSameComponents(carried, selectedStack) || !canItemFit(holder, carried)) {
                              ci.cancel();
                              return;
                        }

                        for (int i = stacks.size() - 1; i >= 0  && !mutable.isFull(); i--) {
                              ItemStack stack = stacks.get(i);
                              boolean same = ItemStack.isSameItemSameComponents(carried, stack);
                              if (same) {
                                    int stackableSlot = inventory.getSlotWithRemainingSpace(stack);
                                    if (stackableSlot == -1) {
                                          stackableSlot = inventory.getFreeSlot();
                                    }
                                    if (stackableSlot == -1)
                                          continue;

                                    ItemStack removed = mutable.removeItem(i);
                                    if (inventory.add(-1, removed)) {
                                          ci.cancel();
                                    }
                              }
                        }

                        boolean cancelled = ci.isCancelled();
                        if (cancelled) {
                              sound().atClient(player, ModSound.Type.REMOVE);
                              mutable.push();
                        }
                        return;
                  }

                  ItemStack stack = mutable.removeItem(selectedSlot);
                  int stackableSlot = inventory.getSlotWithRemainingSpace(stack);
                  if (stackableSlot == -1) {
                        stackableSlot = inventory.getFreeSlot();
                  }
                  if (stackableSlot != -1 && inventory.add(-1, stack)) {
                        sound().atClient(player, ModSound.Type.REMOVE);
                        mutable.push();
                        ci.cancel();
                  }
            } else {
                  ItemStack backpack = player.getItemBySlot(selectedEquipment);
                  ComponentHolder holder = ComponentHolder.of(backpack);
                  MutableBundleLike<?> mutable = mutable(holder);
                  if (mutable.isFull())
                        return;

                  if (ClickType.PICKUP_ALL.equals(actionType)) {
                        List<ItemStack> stacks = mutable.getItemStacks();
                        ItemStack carried = player.containerMenu.getCarried();
                        if (stacks.isEmpty()
                        || !ItemStack.isSameItemSameComponents(carried, stacks.getFirst())
                        || !canItemFit(holder, carried)
                        ) {
                              ci.cancel();
                              return;
                        }

                        Inventory inventory = player.getInventory();
                        NonNullList<ItemStack> items = inventory.items;
                        for (int i = items.size() - 1; i >= 0 && !mutable.isFull(); i--) {
                              ItemStack stack = items.get(i);
                              if (ItemStack.isSameItemSameComponents(carried, stack)) {
                                    int toAdd = mutable.getMaxAmountToAdd(stack.copy());
                                    int count = Math.min(stack.getMaxStackSize(), toAdd);
                                    ItemStack removed = stack.copyWithCount(count);
                                    stack.shrink(count);
                                    if (mutable.addItem(removed, 0) != null) {
                                          ci.cancel();
                                    }
                              }
                        }

                        boolean cancelled = ci.isCancelled();
                        if (cancelled) {
                              sound().atClient(player, ModSound.Type.INSERT);
                              mutable.push();
                        }
                        return;
                  }

                  if (canItemFit(holder, slot.getItem())) {
                        ItemStack slotItem = slot.getItem().copy();
                        int toAdd = mutable.getMaxAmountToAdd(slotItem);
                        ItemStack removed = slot.remove(toAdd);
                        if (mutable.addItem(removed, 0) != null) {
                              sound().atClient(player, ModSound.Type.INSERT);
                              mutable.push();
                              ci.cancel();
                        }
                  }
            }
      }

      @Override
      public void hotkeyThrow(Slot slot, ComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci) {
            if (isEmpty(backpack))
                  return;

            MutableBundleLike<?> mutable = mutable(backpack);
            int selectedSlot = mutable.getSelectedSlot(player);

            ItemStack removed;
            if (menuKeyDown)
                  removed = mutable.removeItem(selectedSlot);
            else return;

            player.drop(removed, true);
            sound().atClient(player, ModSound.Type.REMOVE);
            mutable.push();
            ci.cancel();
      }

      @Override
      public boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (!isFull(backpack)) {
                  inventory.items.forEach(stacks -> {
                        if (ItemStack.isSameItemSameComponents(stacks, stack)) {
                              int present = stacks.getCount();
                              int inserted = stack.getCount();
                              int count = present + inserted;
                              int remainder = Math.max(0, count - stack.getMaxStackSize());
                              count -= remainder;

                              stacks.setCount(count);
                              stack.setCount(remainder);
                        }
                  });

                  if (stack.isEmpty()) {
                        cir.setReturnValue(true);
                        return true;
                  }

                  MutableBundleLike<?> mutable = mutable(ComponentHolder.of(backpack));
                  Iterator<ItemStack> iterator = mutable.getItemStacks().iterator();
                  while (iterator.hasNext() && !stack.isEmpty()) {
                        ItemStack itemStack = iterator.next();
                        if (ItemStack.isSameItemSameComponents(itemStack, stack)) {
                              ItemStack returnStack = mutable.addItem(stack);
                              if (returnStack != null) {
                                    cir.setReturnValue(true);
                              }
                        }
                  }

                  if (cir.isCancelled() && cir.getReturnValue()) {
                        sound().toClient(player, ModSound.Type.INSERT, 1, 1);
                        mutable.push();

                        if (player instanceof ServerPlayer serverPlayer) {
                              List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                              ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                              serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                        }
                  }

                  return stack.isEmpty();
            }
            return false;
      }

      @Override
      public void clientPickBlock(EquipmentSlot equipmentSlot, boolean instantBuild, Inventory inventory, ItemStack itemStack, Player player, CallbackInfo ci) {
            if (instantBuild || inventory.getFreeSlot() == -1)
                  return;

            int slot = inventory.findSlotMatchingItem(itemStack);
            if (slot > -1 || player == null)
                  return;

            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return;

            int size = stacks.size();
            for (int j = 0; j < size; j++) {
                  ItemStack backpackStack = stacks.get(j);
                  if (ItemStack.isSameItem(itemStack, backpackStack)) {
                        slot = j;
                  }
            }

            if (slot < 0)
                  return;

            PickBlock.send(slot, equipmentSlot);
            sound().atClient(player, ModSound.Type.REMOVE);
            ci.cancel();

            SlotSelection selection = backpack.get(ITraitData.SLOT_SELECTION);
            if (selection != null) {
                  selection.limit(slot, size);
            }
      }

      @Override
      public void breakTrait(ServerPlayer pPlayer, ItemStack instance) {
            List<ItemStack> stacks = instance.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return;

            stacks.forEach(stack -> {
                  boolean success = pPlayer.getInventory().add(-1, stack);
                  if (!success || !stack.isEmpty()) {
                        pPlayer.drop(stack, true, true);
                  }
            });
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BundleLikeTraits that)) return false;
            if (!super.equals(o)) return false;
            return size == that.size && Objects.equals(sound(), that.sound());
      }

      @Override
      public int hashCode() {
            return Objects.hash(sound(), size);
      }

      @Override
      public void tinyMenuClick(ComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player) {
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
                        if (i == size)
                              i = 0;

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
                        if (i == size)
                              i = 0;

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
                  if (index == -1)
                        return;

                  List<ItemStack> stacks = mutable.stacks.get();
                  if (index >= stacks.size())
                        return;

                  ItemStack stack = stacks.get(index);
                  ItemStorageTraits.runIfEquipped(player, ((storageTraits, slot) -> {
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
                  } else if (mutable.addItem(carried) != null) {
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
                  } else if (mutable.addItem(carried, size) != null) {
                        mutable.push();
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  return;
            }

            ItemStack stack = stacks.get(index);
            if (stack.isEmpty() && carried.isEmpty())
                  return;

            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        int toAdd = mutable.toAdd(carried);
                        if (toAdd == 0)
                              return;

                        if (clickType.isRight()) {
                              stack.grow(1);
                              carried.shrink(1);
                        } else {
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
            } else if (mutable.addItem(carried, index + 1) != null) {
                  sound().atClient(player, ModSound.Type.INSERT);
            }

            mutable.push();
      }

      @Override
      public void tinyHotbarClick(ComponentHolder holder, int slotId, TinyClickType clickType, InventoryMenu menu, Player player) {
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

      @Override
      public void onPlayerInteract(LivingEntity owner, Player player, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {
            if (player.level().isClientSide) {
                  ViewableBackpack viewable = ViewableBackpack.get(owner);
                  BundleScreen.openScreen(player, viewable, this);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
      }

      @Override
      public void menuClick(ComponentHolder holder, int index, TraitMenuClick.Kind clickType, SlotAccess carriedAccess, Player player) {
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
                        if (i == size)
                              i = 0;

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
                        if (i == size)
                              i = 0;

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
                  if (index == -1)
                        return;

                  List<ItemStack> stacks = mutable.stacks.get();
                  if (index >= stacks.size())
                        return;

                  ItemStack stack = stacks.get(index);
                  ItemStorageTraits.runIfEquipped(player, ((storageTraits, slot) -> {
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

            int size = stacks.size();
            if (index >= size) {
                  if (clickType.isRight()) {
                        if (mutable.addItem(carried.copyWithCount(1), size) != null) {
                              carried.shrink(1);
                              mutable.push();
                              sound().atClient(player, ModSound.Type.INSERT);
                        }
                  } else if (mutable.addItem(carried, size) != null) {
                        mutable.push();
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  return;
            }

            ItemStack stack = stacks.get(index);
            if (stack.isEmpty() && carried.isEmpty())
                  return;

            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        int toAdd = mutable.toAdd(carried);
                        if (toAdd == 0)
                              return;

                        if (clickType.isRight()) {
                              stack.grow(1);
                              carried.shrink(1);
                        } else {
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
            } else if (mutable.addItem(carried, index + 1) != null) {
                  sound().atClient(player, ModSound.Type.INSERT);
            }

            mutable.push();
      }
}
