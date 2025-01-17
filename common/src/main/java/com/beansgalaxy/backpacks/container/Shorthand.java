package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.network.clientbound.SendWeaponSlot;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public class Shorthand implements Container {
      public static final int SHORTHAND_DEFAU = 2;
      public static final int SHORTHAND_MAX = 4;

      protected final Int2ObjectArrayMap<ItemStack> stacks;
      private final Player owner;

      private int timer = 0;
      public int selection = 0;
      public boolean active = false;

      int heldSelected = 0;
      private int oSize;
      private ItemStack oWeapon;

      public Shorthand(Player player) {
            this.owner = player;
            this.oSize = ServerSave.CONFIG.getShorthandSize(owner);

            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>();
            map.defaultReturnValue(ItemStack.EMPTY);
            this.stacks = map;

      }

      public static Shorthand get(Inventory that) {
            BackData backData = (BackData) that;
            return backData.getShorthand();
      }

      public static Shorthand get(Player player) {
            BackData backData = BackData.get(player);
            return backData.getShorthand();
      }

      public void load(CompoundTag tag, RegistryAccess access) {
            ArrayList<ItemStack> stacks = new ArrayList<>();
            loadByName("weapons", tag, access, stacks);
            loadByName("tools", tag, access, stacks);

            if (tag.contains("shorthand")) {
                  CompoundTag shorthand = tag.getCompound("shorthand");
                  for (String allKey : shorthand.getAllKeys()) {
                        CompoundTag slot = shorthand.getCompound(allKey);
                        int index = Integer.parseInt(allKey);
                        RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                        ItemStack stack1 = ItemStack.OPTIONAL_CODEC.parse(serializationContext, slot).getOrThrow();
                        setItem(index, stack1);
                  }
            }

            if (!stacks.isEmpty()) {
                  Iterator<ItemStack> iterator = stacks.iterator();
                  int i = 0;
                  while (iterator.hasNext()) {
                        if (this.stacks.get(i).isEmpty()) {
                              ItemStack stack = iterator.next();
                              this.stacks.put(i, stack);
                        }
                        i++;
                  }
            }
      }

      @Deprecated(since = "0.8-beta")
      private void loadByName(String name, CompoundTag tag, RegistryAccess access, List<ItemStack> stacks) {
            if (!tag.contains(name))
                  return;

            CompoundTag shorthand = tag.getCompound(name);
            for (String allKey : shorthand.getAllKeys()) {
                  CompoundTag slot = shorthand.getCompound(allKey);
                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(serializationContext, slot).getOrThrow();
                  stacks.add(stack);
            }
      }

      public int getQuickestSlot(BlockState blockState) {
            ItemStack itemInHand = owner.getMainHandItem();
            Inventory inv = owner.getInventory();
            if (!active && ShorthandSlot.isTool(itemInHand))
                  return -1;

            int slot = -1;
            int candidate = -1;
            ItemStack mainHandItem = inv.items.get(active ? heldSelected : inv.selected);
            float topSpeed = mainHandItem.getItem().getDestroySpeed(mainHandItem, blockState);

            boolean saveItemsIfBroken = !ServerSave.CONFIG.tool_belt_break_items.get();
            boolean requiresToolForDrops = blockState.requiresCorrectToolForDrops();
            for (int i = 0; i < getContainerSize(); i++) {
                  ItemStack tool = getItem(i);

                  if (saveItemsIfBroken) {
                        int remainingUses = tool.getMaxDamage() - tool.getDamageValue();
                        if (remainingUses < 2)
                              continue;
                  }

                  float destroySpeed = tool.getItem().getDestroySpeed(tool, blockState);
                  if (destroySpeed > topSpeed) {
                        if (tool.getItem().isCorrectToolForDrops(tool, blockState)) {
                              topSpeed = destroySpeed;
                              slot = i;
                        }
                        else if (!requiresToolForDrops)
                              candidate = i;
                  }
            }

            return slot == -1 ? candidate : slot;
      }

      public void onAttackBlock(BlockState blockState, float blockHardness) {
            Inventory inv = owner.getInventory();

            if (inv.selected - inv.items.size() >= 0 && timer == 0)
                  return;

            if (blockHardness < 0.1f)
                  resetSelected(inv);
            else {
                  int slot = this.getQuickestSlot(blockState);
                  if (slot > -1) {
                        loadTimer();
                        setHeldSelected(inv.selected);
                        int newSelected = slot + inv.items.size();
                        if (inv.selected != newSelected)
                              inv.selected = newSelected;

                        selection = slot;
                        active = true;
                  } else
                        resetSelected(inv);
            }
      }

      void setHeldSelected(int selected) {
            if (selected < 9)
                  heldSelected = selected;
      }

      public int getTimer() {
            return timer;
      }

      public void loadTimer() {
            timer = 20;
      }

      public void clearTimer() {
            timer = 0;
      }

      public void tick(Inventory inventory) {
            if (!owner.level().isClientSide) {
                  int size = size();
                  if (oSize > size) {
                        setChanged();
                        int maxSlot = getMaxSlot();

                        if (maxSlot < size())
                              return;

                        int[] j = {0};
                        for (int i = size(); i < maxSlot; i++) {
                              ItemStack removed = stacks.remove(i);

                              if (getJ(j, removed))
                                    continue;

                              int slot = inventory.getFreeSlot();
                              if (slot == -1)
                                    owner.drop(removed, true);
                              else
                                    inventory.add(slot, removed);
                        }
                  }

            }

            stacks.forEach((i, stack) ->
                        stack.inventoryTick(owner.level(), inventory.player, i, selection == i)
            );

            if (owner instanceof ServerPlayer serverPlayer) {
                  int selected = inventory.selected - inventory.items.size();

                  if (selected > -1) {
                        ItemStack weapon;
                        if (selection == selected)
                              weapon = ItemStack.EMPTY;
                        else if (selection >= 0) {
                              selected = selection;
                              weapon = ItemStack.EMPTY;
                        } else
                              weapon = getItem(selected);

                        if (oWeapon != weapon) {
                              oWeapon = weapon;
                              SendWeaponSlot.send(serverPlayer, selected, weapon);
                        }
                  }
            }

            oSize = getContainerSize();
      }

      private boolean getJ(int[] j, ItemStack removed) {
            for (; j[0] < this.size(); j[0]++) {
                  ItemStack stack = stacks.get(j[0]);
                  if (stack.isEmpty()) {
                        stacks.put(j[0], removed);
                        return true;
                  }
            }
            return false;
      }

      public void tickTimer(Inventory inventory) {
            if (getContainerSize() <= selection || selection < 0) {
                  if (inventory.selected >= inventory.items.size())
                        inventory.selected = heldSelected;
                  clearTimer();
                  return;
            }

            if (timer == 1)
                  resetSelected(inventory);

            if (timer > 0) {
                  if (getItem(selection).isEmpty())
                        resetSelected(inventory);
                  timer--;
            }
      }

      public void resetSelected(Inventory inventory) {
            if (inventory.selected >= inventory.items.size())
                  inventory.selected = heldSelected;
            clearTimer();
            active = false;
      }

      public int getSelected(Inventory inventory) {
            int slot = inventory.selected - inventory.items.size();
            if (getContainerSize() > slot && slot >= 0)
                  return slot;
            else {
                  resetSelected(inventory);
                  return -1;
            }
      }

      public void replaceWith(Shorthand that) {
            clearContent();
            that.stacks.forEach((i, stack) -> {
                  if (!stack.isEmpty())
                        this.stacks.put(i, stack);
            });

            ItemStack backpack = that.owner.getItemBySlot(EquipmentSlot.BODY);
            this.owner.setItemSlot(EquipmentSlot.BODY, backpack);
      }

      public void activateShorthand(boolean active) {
            this.active = active;

            Inventory inventory = owner.getInventory();
            if (active) {
                  setHeldSelected(inventory.selected);

                  int start = selection;
                  do {
                        selection %= getContainerSize();

                        ItemStack stack = getItem(selection);
                        if (!stack.isEmpty())
                              break;

                        selection++;
                  } while (start != selection);

                  inventory.selected = inventory.items.size() + selection;
            }
            else resetSelected(inventory);
      }

      public int getMaxSlot() {
            if (stacks.isEmpty()) {
                  return 0;
            }

            OptionalInt max = stacks.int2ObjectEntrySet().stream().mapToInt(entry ->
                  entry.getValue().isEmpty()
                  ? 0 : entry.getIntKey()
            ).max();
            return max.orElse(-1) + 1;
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

            tag.put("shorthand", container);
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

      public Iterable<ItemStack> getContent() {
            return stacks.values();
      }

      public void putItem(int slot, ItemStack stack) {
            stacks.put(slot, stack);
      }

      public int size() {
            return ServerSave.CONFIG.getShorthandSize(owner);
      }

      @Override
      public void setChanged() {
      }

      @Override
      public int getContainerSize() {
            int maxSlot = getMaxSlot();
            return Math.max(size(), maxSlot);
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

      @Override
      public boolean stillValid(Player player) {
            return !player.isRemoved();
      }

      @Override
      public void clearContent() {
            stacks.clear();
            setChanged();
      }

      public int getHeldSlot() {
            return heldSelected;
      }
}
