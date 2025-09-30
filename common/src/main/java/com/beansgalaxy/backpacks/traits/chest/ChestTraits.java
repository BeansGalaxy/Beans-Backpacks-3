package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.network.serverbound.PickBlock;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.chest.screen.ChestScreen;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableItemStorage;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ChestTraits extends ItemStorageTraits {
      public static final String NAME = "chest";
      public final int rows;
      public final int columns;

      public ChestTraits(@Nullable ResourceLocation location, ModSound sound, int rows, int columns) {
            super(sound);
            this.rows = rows;
            this.columns = columns;
      }

      public static Optional<ChestTraits> get(ComponentHolder backpack) {
            ChestTraits chestTraits = backpack.get(Traits.CHEST);
            if (chestTraits != null)
                  return Optional.of(chestTraits);

            ReferenceTrait referenceTrait = backpack.get(Traits.REFERENCE);
            if (referenceTrait != null) {
                  Optional<GenericTraits> reference = referenceTrait.getTrait();
                  if (reference.isPresent() && reference.get() instanceof ChestTraits traits) {
                        return Optional.of(traits);
                  }
            }

            return Optional.empty();
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public ChestClient client() {
            return ChestClient.INSTANCE;
      }

      @Override
      public ChestEntity entity() {
            return ChestEntity.INSTANCE;
      }

      public int size() {
            return columns * rows;
      }

      @Override
      public Fraction fullness(ComponentHolder holder) {
            ItemContainerContents contents = holder.get(ITraitData.CHEST);
            if (contents == null)
                  return Fraction.ZERO;

            int fullSlots = contents.nonEmptyStream().mapToInt(stack -> stack.isEmpty() ? 0 : 1).sum();
            return Fraction.getFraction(fullSlots, size());
      }

      @Override
      public boolean isFull(ComponentHolder holder) {
            ItemContainerContents contents = holder.get(ITraitData.CHEST);
            if (contents == null)
                  return false;

            NonNullList<ItemStack> pList = NonNullList.of(ItemStack.EMPTY);
            contents.copyInto(pList);
            return pList.size() == size() && pList.stream().allMatch(stack -> stack.getCount() == stack.getMaxStackSize());
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            if (player.level().isClientSide)
                  client().openTinyMenu(this, hand, player);

            ItemStack backpack = player.getItemInHand(hand);
            cir.setReturnValue(InteractionResultHolder.success(backpack));
      }

      @Override
      public void stackedOnMe(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BackData backData = BackData.get(player);
            int tinySlot = backData.getTinySlot();
            if (tinySlot != -1) {
                  if (tinySlot != slot.index) {
                        if (player.level().isClientSide)
                              client().swapTinyMenu(this, slot);

                        cir.setReturnValue(true);
                  }
            }
            else if (backData.isMenuKeyDown()) {
                  if (player.level().isClientSide)
                        client().openTinyMenu(this, slot);

                  cir.setReturnValue(true);
            }
      }

      @Override
      public void stackedOnOther(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public boolean isEmpty(ComponentHolder holder) {
            return !holder.has(ITraitData.CHEST);
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

      @Override
      public ChestMutable mutable(ComponentHolder holder) {
            return new ChestMutable(this, holder);
      }

      @Override
      public void hotkeyUse(Slot slot, EquipmentSlot selectedEquipment, int button, ClickType actionType, Player player, CallbackInfo ci) {
            if (selectedEquipment == null)
                  return;

            ItemStack backpack = player.getItemBySlot(selectedEquipment);
            ComponentHolder holder = ComponentHolder.of(backpack);
            ChestMutable mutable = mutable(holder);
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
                  for (int i = items.size() - 1; i >= 0  && !mutable.isFull(); i--) {
                        ItemStack stack = items.get(i);
                        if (ItemStack.isSameItemSameComponents(carried, stack)) {
                              int toAdd = mutable.getMaxAmountToAdd(stack.copy());
                              int count = Math.min(stack.getMaxStackSize(), toAdd);
                              ItemStack removed = stack.copyWithCount(count);
                              stack.shrink(count);
                              if (mutable.addItem(removed) != null) {
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
                  if (mutable.addItem(removed) != null) {
                        sound().atClient(player, ModSound.Type.INSERT);
                        mutable.push();
                        ci.cancel();
                  }
            }
      }

      @Override
      public void hotkeyThrow(Slot slot, ComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci) {
            if (isEmpty(backpack))
                  return;

            ChestMutable mutable = mutable(backpack);

            ItemStack removed;
            if (menuKeyDown)
                  removed = mutable.removeItem(0);
            else if (EquipableComponent.get(backpack).isPresent())
            {
                  ItemStack itemStack = mutable.getItemStacks().getFirst();
                  removed = itemStack.getCount() == 1 ? mutable.removeItem(0) : itemStack.split(1);
            }
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

                  ChestMutable mutable = mutable(ComponentHolder.of(backpack));
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
            ItemContainerContents contents = backpack.get(ITraitData.CHEST);
            if (contents == null) {
                  return;
            }

            NonNullList<ItemStack> stacks = NonNullList.of(ItemStack.EMPTY);
            contents.copyInto(stacks);

            for (int j = 0; j < stacks.size(); j++) {
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
      }

      @Override
      public void breakTrait(ServerPlayer pPlayer, ItemStack backpack) {
            ItemContainerContents contents = backpack.get(ITraitData.CHEST);
            if (contents == null) {
                  return;
            }

            contents.nonEmptyStream().forEachOrdered(stack -> {
                  boolean success = pPlayer.getInventory().add(-1, stack);
                  if (!success || !stack.isEmpty()) {
                        pPlayer.drop(stack, true, true);
                  }
            });
      }

      @Override
      public @Nullable ItemStack getFirst(ComponentHolder backpack) {
            ItemContainerContents contents = backpack.get(ITraitData.CHEST);
            if (contents == null) {
                  return null;
            }

            return contents.copyOne();
      }

      public void tinySubMenuClick(ComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player) {
            if (index < 0 || index >= size()) {
                  return;
            }

            ChestMutable mutable = mutable(holder);
            if (clickType.isShift()) {
                  ItemStack stack = mutable.removeItem(index);
                  if (player.addItem(stack)) {
                        mutable.push();
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  return;
            }

            tinyMenuClick(holder, index, clickType, carriedAccess, player, mutable);
      }

      @Override
      public void tinyMenuClick(ComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player) {
            if (index < 0 || index >= size()) {
                  return;
            }

            ChestMutable mutable = mutable(holder);
            if (clickType.isShift()) {
                  Inventory inventory = player.getInventory();
                  ItemStack stack = mutable.removeItem(index);
                  for (int i = 0; i < 9; i++) {
                        ItemStack hotbar = inventory.items.get(i);
                        if (ItemStack.isSameItemSameComponents(stack, hotbar)) {
                              int add = Math.min(hotbar.getMaxStackSize() - hotbar.getCount(), stack.getCount());
                              hotbar.grow(add);
                              stack.shrink(add);
                        }

                        if (stack.isEmpty()) {
                              mutable.push();
                              sound().atClient(player, ModSound.Type.REMOVE);
                              return;
                        }
                  }

                  for (int i = 0; i < 9; i++) {
                        ItemStack hotbar = inventory.items.get(i);
                        if (hotbar.isEmpty()) {
                              int add = Math.min(stack.getMaxStackSize(), stack.getCount());
                              inventory.items.set(i, stack.copyWithCount(add));
                              stack.shrink(add);
                        }

                        if (stack.isEmpty()) {
                              mutable.push();
                              sound().atClient(player, ModSound.Type.REMOVE);
                              return;
                        }
                  }
                  return;
            }

            tinyMenuClick(holder, index, clickType, carriedAccess, player, mutable);
      }

      private void tinyMenuClick(ComponentHolder holder, int index, TinyClickType clickType, SlotAccess carriedAccess, Player player, ChestMutable mutable) {
            if (clickType.isHotbar()) {
                  Inventory inventory = player.getInventory();
                  ItemStack hotbarStack = inventory.items.get(clickType.hotbarSlot);
                  if (hotbarStack.isEmpty() || canItemFit(holder, hotbarStack)) {
                        ItemStack stack = mutable.getItem(index);
                        mutable.setItem(index, hotbarStack);
                        inventory.items.set(clickType.hotbarSlot, stack);
                        mutable.push();
                        sound().atClient(player, ModSound.Type.INSERT);
                  }
                  return;
            }

            if (clickType.isAction()) {
                  ItemStack stack = mutable.getItem(index);
                  CallbackInfo ci = new CallbackInfo("chest_tiny_menu", true);
                  ItemStorageTraits.runIfEquipped(player, ((storageTraits, slot) -> {
                        ItemStack backpack = player.getItemBySlot(slot);
                        MutableItemStorage itemStorage = storageTraits.mutable(ComponentHolder.of(backpack));
                        if (canItemFit(holder, stack)) {
                              if (itemStorage.addItem(stack) != null) {
                                    itemStorage.push();
                                    ci.cancel();
                              }
                        }

                        return stack.isEmpty();
                  }));

                  if (ci.isCancelled()) {
                        mutable.push();
                        sound().atClient(player, ModSound.Type.REMOVE);
                  }
            }

            if (clickType.isDrop()) {
                  ItemStack stack = mutable.removeItem(index);
                  player.drop(stack, true);
                  mutable.push();
                  return;
            }

            List<ItemStack> stacks = mutable.getItemStacks();
            ItemStack carried = carriedAccess.get();
            ItemStack stack = stacks.get(index);
            if (stack.isEmpty() && carried.isEmpty())
                  return;

            if (!stack.isEmpty() && !carried.isEmpty()) {
                  if (!canItemFit(holder, carried))
                        return;

                  if (ItemStack.isSameItemSameComponents(stack, carried)) {
                        if (stack.getCount() == stack.getMaxStackSize()) {
                              return;
                        }

                        if (clickType.isRight()) {
                              stack.grow(1);
                              carried.shrink(1);
                        } else {
                              int add = Math.min(stack.getMaxStackSize() - stack.getCount(), carried.getCount());
                              stack.grow(add);
                              carried.shrink(add);
                        }
                  }
                  else {
                        carriedAccess.set(stack);
                        stacks.set(index, carried);
                  }

                  sound().atClient(player, ModSound.Type.INSERT);
            }
            else if (carried.isEmpty()) {
                  if (clickType.isRight()) {
                        int count = Mth.ceil((float) stack.getCount() / 2);
                        ItemStack split = stack.split(count);
                        carriedAccess.set(split);
                  } else {
                        ItemStack removed = mutable.removeItem(index);
                        carriedAccess.set(removed);
                  }

                  sound().atClient(player, ModSound.Type.REMOVE);
            }
            else {
                  if (!canItemFit(holder, carried))
                        return;

                  if (clickType.isRight()) {
                        stacks.set(index, carried.copyWithCount(1));
                        carried.shrink(1);
                  } else {
                        carriedAccess.set(stack);
                        stacks.set(index, carried);
                  }

                  sound().atClient(player, ModSound.Type.INSERT);
            }

            mutable.push();
      }

      @Override public void onPlayerInteract(LivingEntity owner, Player player, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {
            if (player.level().isClientSide) {
                  ViewableBackpack viewable = ViewableBackpack.get(owner);
                  ChestScreen.openScreen(viewable, this, player);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.CHEST;
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChestTraits that)) return false;
            if (!super.equals(o)) return false;
            return rows == that.rows
                        && columns == that.columns
                        && Objects.equals(sound(), that.sound());
      }

      @Override
      public int hashCode() {
            return Objects.hash(sound(), rows, columns);
      }

      @Override
      public String toString() {
            return "ChestTraits{" +
                        "rows=" + rows +
                        ", columns=" + columns +
                        ", sound=" + sound() +
                        '}';
      }
}
