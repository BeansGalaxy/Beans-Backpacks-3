package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class ItemStorageTraits extends GenericTraits {

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
            else other = optional.get().mutable(ComponentHolder.of(from));

            if (other.isEmpty())
                  return false;

            other.moveItemsTo(to, player, true);
            return true;
      }

      public abstract MutableItemStorage mutable(ComponentHolder holder);

      public abstract void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci);

      public abstract void hotkeyThrow(Slot slot, ComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci);

      public abstract boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir);

      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            MutableItemStorage mutable = mutable(ComponentHolder.of(backpack));
            ItemStack itemStack = mutable.addItem(stack);
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

      public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            return inserted.getItem().canFitInsideContainerItems() && Traits.get(inserted).isEmpty();
      }

      public abstract void breakTrait(ServerPlayer pPlayer, ItemStack instance);

      @Nullable
      public abstract ItemStack getFirst(ComponentHolder backpack);

      public void tinyHotbarClick(ComponentHolder holder, int slotId, TinyClickType clickType, InventoryMenu menu, Player player) {
            Slot slot = menu.getSlot(slotId);
            ItemStack hotbar = slot.getItem();
            if (ItemStorageTraits.tryMoveItems(mutable(holder), hotbar, player))
                  return;

            if (clickType.isAction()) {
                  BackpackTraits.runIfEquipped(player, ((storageTraits, equipment) -> {
                        ItemStack backpack = player.getItemBySlot(equipment);
                        MutableItemStorage itemStorage = storageTraits.mutable(ComponentHolder.of(backpack));
                        if (canItemFit(ComponentHolder.of(backpack), hotbar)) {
                              if (itemStorage.addItem(hotbar) != null) {
                                    sound().atClient(player, ModSound.Type.INSERT);
                                    itemStorage.push();
                              }
                        }

                        return hotbar.isEmpty();
                  }));
            }

            if (clickType.isShift()) {
                  MutableItemStorage mutable = mutable(holder);
                  if (mutable.addItem(hotbar) != null) {
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

      public abstract void tinyMenuClick(ComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player);

}
