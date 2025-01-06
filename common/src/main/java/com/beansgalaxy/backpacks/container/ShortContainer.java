package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.network.clientbound.SendSelectedSlot;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Iterator;
import java.util.OptionalInt;

public abstract class ShortContainer implements Container {
      protected final Int2ObjectArrayMap<ItemStack> stacks;
      private final String name;
      private int size = 0;

      public String getName() {
            return name;
      }

      public ShortContainer(String name) {
            this.name = name;
            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>();
            map.defaultReturnValue(ItemStack.EMPTY);
            this.stacks = map;
      }

      public int getSize() {
            return size;
      }

      public abstract int updateSize();

      public int getMaxSlot() {
            if (stacks.isEmpty()) {
                  return 0;
            }

            OptionalInt max = stacks.int2ObjectEntrySet().stream().mapToInt(entry ->
                        entry.getValue().isEmpty()
                                    ? 0 : entry.getIntKey()
            ).max();
            return max.orElse(0) + 1;
      }

      @Override
      public int getContainerSize() {
            int maxSlot = getMaxSlot();
            return Math.max(getSize(), maxSlot);
      }

      @Override
      public boolean isEmpty() {
            return stacks.int2ObjectEntrySet().stream().allMatch(stack -> stack.getValue().isEmpty());
      }

      @Override
      public ItemStack getItem(int slot) {
            return stacks.get(slot);
      }

      @Override
      public ItemStack removeItem(int slot, int amount) {
            ItemStack stack = getItem(slot);
            ItemStack split = stack.getCount() > amount
                        ? stack.split(amount)
                        : removeItemNoUpdate(slot);

            setChanged();
            return split;
      }

      @Override
      public ItemStack removeItemNoUpdate(int slot) {
            setChanged();
            return stacks.remove(slot);
      }

      @Override
      public void setItem(int slot, ItemStack stack) {
            if (stack.isEmpty())
                  stacks.remove(slot);
            else stacks.put(slot, stack);
            setChanged();
      }

      public void putItem(int slot, ItemStack stack) {
            stacks.put(slot, stack);
      }

      @Override
      public void setChanged() {
            size = updateSize();
      }

      @Override
      public boolean stillValid(Player player) {
            return !player.isRemoved();
      }

      @Override
      public void clearContent() {
            stacks.clear();
            setChanged();
      }

      public void save(CompoundTag tag, RegistryAccess access) {
            CompoundTag container = new CompoundTag();
            stacks.forEach((slot, tool) -> {
                  if (tool.isEmpty())
                        return;

                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  ItemStack.CODEC.encodeStart(serializationContext, tool).ifSuccess(stackTag ->
                              container.put(String.valueOf(slot), stackTag));
            });

            tag.put(name, container);
      }

      public void load(CompoundTag tag, RegistryAccess access) {
            if (!tag.contains(name))
                  return;

            CompoundTag shorthand = tag.getCompound(name);
            for (String allKey : shorthand.getAllKeys()) {
                  CompoundTag slot = shorthand.getCompound(allKey);
                  int index = Integer.parseInt(allKey);
                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(serializationContext, slot).getOrThrow();
                  setItem(index, stack);
            }

            size = updateSize();
      }

      public void dropAll(Inventory inventory) {
            ObjectIterator<Int2ObjectMap.Entry<ItemStack>> iterator = stacks.int2ObjectEntrySet().iterator();
            while (iterator.hasNext()) {
                  ItemStack itemstack = iterator.next().getValue();
                  if (!itemstack.isEmpty())
                        inventory.player.drop(itemstack, true, false);
                  iterator.remove();
            }
      }

      public Iterator<ItemStack> getContent() {
            return stacks.values().iterator();
      }

      public void replaceWith(ShortContainer that) {
            clearContent();
            that.stacks.forEach((i, stack) -> {
                  if (!stack.isEmpty())
                        this.stacks.put(i, stack);
            });
      }

      public static class Weapon extends ShortContainer {
            protected final Int2ObjectArrayMap<ItemStack> lastStacks;
            private final Shorthand shorthand;

            public Weapon(Shorthand shorthand) {
                  super("weapons");
                  this.shorthand = shorthand;

                  Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>();
                  map.defaultReturnValue(ItemStack.EMPTY);
                  this.lastStacks = map;
            }

            @Override public int updateSize() {
                  return shorthand.getWeaponsSize();
            }

            @Override public void setItem(int slot, ItemStack stack) {
                  if (!stack.isEmpty())
                        lastStacks.put(slot, stack);

                  super.setItem(slot, stack);
            }

            public static boolean putBackLastStack(Player player, ItemStack stack) {
                  Shorthand shorthand = Shorthand.get(player);
                  Weapon weapons = shorthand.weapons;
                  Inventory inventory = player.getInventory();
                  int selected = inventory.selected;
                  for (Int2ObjectMap.Entry<ItemStack> entry : weapons.lastStacks.int2ObjectEntrySet()) {
                        int i = entry.getIntKey();
                        if (!weapons.stacks.get(i).isEmpty())
                              continue;

                        ItemStack lastStack = entry.getValue();
                        if (ItemStack.isSameItemSameComponents(lastStack, stack)) {
                              ItemStack copy = stack.copy();
                              stack.setCount(0);

                              weapons.lastStacks.put(i, copy);
                              weapons.stacks.put(i, copy);

                              if (shorthand.selectedWeapon == i) {
                                    shorthand.setHeldSelected(selected);
                                    if (player instanceof ServerPlayer serverPlayer)
                                          SendSelectedSlot.send(serverPlayer, i);
                              }

                              return true;
                        }
                  }

                  return false;
            }
      }

      public static class Tools extends ShortContainer {
            private final Shorthand shorthand;

            public Tools(Shorthand shorthand) {
                  super("tools");
                  this.shorthand = shorthand;
            }

            @Override public int updateSize() {
                  return shorthand.getToolsSize();
            }
      }
}
