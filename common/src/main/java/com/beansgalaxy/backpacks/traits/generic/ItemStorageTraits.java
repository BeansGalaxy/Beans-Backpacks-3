package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.DraggingContainer;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class ItemStorageTraits extends GenericTraits implements IDraggingTrait {

      public ItemStorageTraits(ModSound sound) {
            super(sound);
      }

      public static Optional<ItemStorageTraits> get(DataComponentHolder stack) {
            for (TraitComponentKind<? extends ItemStorageTraits> type : TraitComponentKind.STORAGE_TRAITS) {
                  ItemStorageTraits traits = stack.get(type);
                  if (traits != null)
                        return Optional.of(traits);
            }

            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null)
                  return referenceTrait.getTrait().map(traits -> {
                        if (traits instanceof ItemStorageTraits storageTraits)
                              return storageTraits;
                        return null;
                  });

            return Optional.empty();
      }

      public static void runIfPresent(ItemStack stack, Consumer<ItemStorageTraits> runnable) {
            if (!stack.isEmpty()) {
                  Optional<ItemStorageTraits> traits = get(stack);
                  traits.ifPresent(runnable);
            }
      }

      public static void runIfPresent(ItemStack stack, Consumer<ItemStorageTraits> runnable, Runnable orElse) {
            if (!stack.isEmpty()) {
                  Optional<ItemStorageTraits> traits = get(stack);
                  traits.ifPresentOrElse(runnable, orElse);
            }
      }

      public static void runIfEquipped(Player player, BiPredicate<ItemStorageTraits, EquipmentSlot> runnable) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                  ItemStack stack = player.getItemBySlot(slot);
                  if (stack.isEmpty())
                        continue;

                  Optional<ItemStorageTraits> traits = get(stack);
                  if (traits.isEmpty())
                        continue;

                  if (!EquipableComponent.testIfPresent(stack, equip ->
                              equip.traitRemovable() || equip.slots().test(slot)))
                        continue;

                  if (runnable.test(traits.get(), slot))
                        return;
            }
      }

      public static boolean testIfPresent(ItemStack stack, Predicate<ItemStorageTraits> predicate) {
            return !stack.isEmpty() && get(stack).map(predicate::test).orElse(false);
      }

      protected static boolean tryMoveItems(MutableItemStorage to, ItemStack from, Player player) {
            if (to.isFull())
                  return false;

            MutableItemStorage other;
            Optional<ItemStorageTraits> optional = ItemStorageTraits.get(from);
            if (optional.isEmpty()) {
                  EnderTraits enderTraits = from.get(Traits.ENDER);
                  if (enderTraits == null)
                        return false;

                  if (enderTraits.getTrait(player.level()) instanceof ItemStorageTraits traits)
                        other = traits.mutable(enderTraits);
                  else return false;
            }
            else other = optional.get().mutable(PatchedComponentHolder.of(from));

            if (other.isEmpty())
                  return false;

            other.moveItemsTo(to, player, true);
            return true;
      }

      public abstract MutableItemStorage mutable(PatchedComponentHolder holder);

      public abstract void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci);

      public abstract void hotkeyThrow(Slot slot, PatchedComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci);

      public abstract boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir);

      public abstract void clientPickBlock(EquipmentSlot equipmentSlot, boolean instantBuild, Inventory inventory, ItemStack itemStack, Player player, CallbackInfo ci);

      public void serverPickBlock(PatchedComponentHolder holder, int index, ServerPlayer sender) {
            Inventory inventory = sender.getInventory();

            int freeSlot = inventory.getFreeSlot();
            if (freeSlot == -1)
                  return;


            if (freeSlot < 9)
                  inventory.selected = freeSlot;

            ItemStack selectedStack = inventory.getItem(inventory.selected);

            MutableItemStorage mutable = mutable(holder);
            ItemStack take = mutable.removeItem(index);
            inventory.setItem(inventory.selected, take);
            mutable.push();

            List<ItemStack> finalStacks = holder.get(ITraitData.ITEM_STACKS);
            int size = finalStacks == null ? 0 : finalStacks.size();
            limitSelectedSlot(holder, index, size);

            int overflowSlot = -1;
            if (!selectedStack.isEmpty())
            {
                  overflowSlot = inventory.getFreeSlot();
                  inventory.setItem(overflowSlot, selectedStack);
            }

            sender.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, inventory.selected, selectedStack));
            sender.connection.send(new ClientboundSetCarriedItemPacket(inventory.selected));

//            Services.REGISTRY.triggerSpecial(sender, SpecialCriterion.Special.PICK_BACKPACK);

            if (overflowSlot > -1)
                  sender.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, overflowSlot, inventory.getItem(overflowSlot)));
      }

      public void limitSelectedSlot(PatchedComponentHolder holder, int slot, int size) {
      }

      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            MutableItemStorage mutable = mutable(PatchedComponentHolder.of(backpack));
            ItemStack itemStack = mutable.addItem(stack, player);
            if (itemStack != null) {
                  mutable.push();
                  cir.setReturnValue(true);

                  if (player instanceof ServerPlayer serverPlayer) {
                        List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                        serverPlayer.serverLevel().getChunkSource().broadcast(serverPlayer, packet);
                  }

                  return itemStack.isEmpty();
            }
            return false;
      }

      public boolean canItemFit(PatchedComponentHolder holder, ItemStack inserted) {
            return inserted.getItem().canFitInsideContainerItems() && Traits.get(inserted).isEmpty();
      }

      public abstract void breakTrait(ServerPlayer pPlayer, ItemStack instance);

      @Nullable
      public abstract ItemStack getFirst(PatchedComponentHolder backpack);

      public void tinyHotbarClick(PatchedComponentHolder holder, int slotId, TinyClickType clickType, InventoryMenu menu, Player player) {
            Slot slot = menu.getSlot(slotId);
            ItemStack hotbar = slot.getItem();
            if (ItemStorageTraits.tryMoveItems(mutable(holder), hotbar, player))
                  return;

            if (clickType.isAction()) {
                  ItemStorageTraits.runIfEquipped(player, ((storageTraits, equipment) -> {
                        ItemStack backpack = player.getItemBySlot(equipment);
                        MutableItemStorage itemStorage = storageTraits.mutable(PatchedComponentHolder.of(backpack));
                        if (canItemFit(PatchedComponentHolder.of(backpack), hotbar)) {
                              if (itemStorage.addItem(hotbar, player) != null) {
                                    sound().atClient(player, ModSound.Type.INSERT);
                                    itemStorage.push();
                              }
                        }

                        return hotbar.isEmpty();
                  }));
            }

            if (clickType.isShift()) {
                  MutableItemStorage mutable = mutable(holder);
                  if (mutable.addItem(hotbar, player) != null) {
                        sound().atClient(player, ModSound.Type.INSERT);
                        mutable.push();
                  }
                  return;
            }

            ItemStack carried = menu.getCarried();
            if (hotbar.isEmpty() && carried.isEmpty())
                  return;

            if (!hotbar.isEmpty() && !carried.isEmpty()) {
                  if (ItemStack.isSameItemSameComponents(hotbar, carried)) {
                        int count = clickType.isRight()
                                    ? 1
                                    : carried.getCount();

                        int toAdd = Math.min(hotbar.getMaxStackSize() - hotbar.getCount(), count);
                        hotbar.grow(toAdd);
                        carried.shrink(toAdd);
                  }
                  else {
                        slot.set(carried);
                        menu.setCarried(hotbar);
                  }
            }
            else if (clickType.isRight()) {
                  if (hotbar.isEmpty()) {
                        ItemStack copy = carried.copyWithCount(1);
                        carried.shrink(1);
                        slot.set(copy);
                  }
                  else {
                        int count = Mth.ceil((float) hotbar.getCount() / 2);
                        ItemStack split = hotbar.split(count);
                        menu.setCarried(split);
                  }
            }
            else {
                  slot.set(carried);
                  menu.setCarried(hotbar);
            }
      }

      public abstract void tinyMenuClick(PatchedComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player);

      public void clickSlot(DraggingContainer drag, Player player, PatchedComponentHolder holder) {
            Slot slot = drag.firstSlot;

            if (drag.isPickup) {
                  ItemStack stack = slot.getItem();
                  boolean mayPickup = slot.mayPickup(player);
                  boolean hasItem = slot.hasItem();
                  boolean canFit = canItemFit(holder, stack);
                  boolean isFull = isFull(holder);
                  if (mayPickup && hasItem && canFit && !isFull) {
                        drag.allSlots.put(drag.firstSlot, stack.copyWithCount(1));
                        drag.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                  }
            }
            else {
                  ItemStack itemStack = getFirst(holder);
                  if (itemStack != null && !slot.hasItem()) {
                        if (AbstractContainerMenu.canItemQuickReplace(slot, itemStack, true) && slot.mayPlace(itemStack)) {
                              drag.allSlots.put(slot, ItemStack.EMPTY);
                              drag.slotClicked(slot, slot.index, 1, ClickType.PICKUP);
                        }
                  }
            }
      }
}
