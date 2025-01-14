package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.network.clientbound.SendWeaponSlot;
import com.google.common.collect.Iterables;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;

public class Shorthand {
      public static final int WEAPON_DEFAU = 1;
      public static final int TOOL_DEFAU = 2;
      public static final int WEAPON_MAX = 4;
      public static final int TOOL_MAX = 5;

      public final ShortContainer.Tools tools = new ShortContainer.Tools(this);
      public final ShortContainer.Weapon weapons = new ShortContainer.Weapon(this);

      private final Player owner;
      private int timer = 0;
      int heldSelected = 0;
      int selectedWeapon = 0;
      private int oToolSize;
      private int oWeaponSize;
      private ItemStack oWeapon;

      public Shorthand(Player player) {
            this.owner = player;
      }

      public static Shorthand get(Inventory that) {
            BackData backData = (BackData) that;
            return backData.getShorthand();
      }

      public static Shorthand get(Player player) {
            BackData backData = BackData.get(player);
            return backData.getShorthand();
      }

      public int size() {
            return tools.getContainerSize() + weapons.getContainerSize();
      }

      public int getSelectedWeapon() {
            return selectedWeapon;
      }

      public void selectWeapon(Inventory inventory, boolean advance) {
            if (weapons.isEmpty()) {
                  resetSelected(inventory);
                  return;
            }

            int weaponsSize = weapons.getContainerSize();
            int itemsSize = inventory.items.size();

            int toolsSize = tools.getContainerSize();
            int slot = inventory.selected - itemsSize;
            int weaponSlot = slot - toolsSize;
            int selectedSlot = getSelectedSlot(advance, weaponsSize, weaponSlot);

            int i = selectedSlot;
            while (weapons.getItem(i).isEmpty()) {
                  if (advance) {
                        i++;
                        if (i >= weaponsSize)
                              i = 0;
                  }
                  else {
                        i--;
                        if (i < 0)
                              i = weaponsSize - 1;
                  }

                  if (i == selectedSlot) {
                        resetSelected(inventory);
                        return;
                  }
            }

            int selected = itemsSize + toolsSize + i;
            if (selected == inventory.selected) {
                  resetSelected(inventory);
                  return;
            }

            selectedWeapon = i;
            setHeldSelected(inventory.selected);
            inventory.selected = selected;
      }

      private int getSelectedSlot(boolean advance, int weaponsSize, int weaponSlot) {
            if (weaponsSize < 3)
                  return advance ? 0 : 1;

            if (weaponSlot < 0)
                  return selectedWeapon;

            if (advance)
                  return selectedWeapon + 1 < weaponsSize ? selectedWeapon + 1 : 0;

            int i = selectedWeapon > 0 ? selectedWeapon : weaponsSize;
            return i - 1;
      }

      public ItemStack getItem(int slot) {
            int weaponSlot = slot - tools.getContainerSize();
            return weaponSlot < 0 ? tools.getItem(slot) : weapons.getItem(weaponSlot);
      }

      public int getToolsSize() {
            return ServerSave.CONFIG.getToolBeltSize(owner);
      }

      public int getWeaponsSize() {
            return ServerSave.CONFIG.getShorthandSize(owner);
      }

      public void dropOverflowItems(ShortContainer container) {
            container.setChanged();
            int maxSlot = container.getMaxSlot();
            int size = container.getSize();

            if (maxSlot < size)
                  return;

            for (int i = size; i < maxSlot; i++) {
                  ItemStack removed = container.stacks.remove(i);
                  owner.drop(removed, true);
            }
      }

      public int getQuickestSlot(BlockState blockState) {
            ItemStack itemInHand = owner.getMainHandItem();
            Inventory inv = owner.getInventory();
            boolean shorthandSelected = inv.selected >= inv.items.size();
            if (!shorthandSelected && ShorthandSlot.isTool(itemInHand)) {
                  return -1;
            }

            int slot = -1;
            int canadate = -1;
            ItemStack mainHandItem = inv.items.get(shorthandSelected ? heldSelected : inv.selected);
            float topSpeed = mainHandItem.getItem().getDestroySpeed(mainHandItem, blockState);

            boolean saveItemsIfBroken = !ServerSave.CONFIG.tool_belt_break_items.get();
            boolean requiresToolForDrops = blockState.requiresCorrectToolForDrops();
            int toolSize = tools.getContainerSize();
            for (int i = 0; i < toolSize; i++) {
                  ItemStack tool = tools.getItem(i);

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
                              canadate = i;
                  }
            }

            if (slot == -1) {
                  for (int i = 0; i < weapons.getContainerSize(); i++) {
                        ItemStack tool = weapons.getItem(i);

                        if (saveItemsIfBroken) {
                              int remainingUses = tool.getMaxDamage() - tool.getDamageValue();
                              if (remainingUses < 2)
                                    continue;
                        }

                        float destroySpeed = tool.getItem().getDestroySpeed(tool, blockState);
                        if (destroySpeed > topSpeed) {
                              if (tool.getItem().isCorrectToolForDrops(tool, blockState)) {
                                    topSpeed = destroySpeed;
                                    slot = toolSize + i;
                              }
                        }
                  }
            }

            return slot == -1 ? canadate : slot;
      }

      public void onAttackBlock(BlockState blockState, float blockHardness) {
            Inventory inv = owner.getInventory();
            int weaponSlot = inv.selected - inv.items.size() - tools.getContainerSize();
            if (weaponSlot >= 0 && timer == 0)
                  return;

            if (blockHardness < 0.1f)
                  resetSelected(inv);
            else {
                  int slot = this.getQuickestSlot(blockState);
                  if (slot > -1) {
                        setTimer(25);
                        setHeldSelected(inv.selected);
                        int newSelected = slot + inv.items.size();
                        if (inv.selected != newSelected)
                              inv.selected = newSelected;
                  } else
                        resetSelected(inv);
            }
      }

      void setHeldSelected(int selected) {
            if (selected < 9)
                  heldSelected = selected;
      }

      private void setTimer(int time) {
            this.timer = time;
      }

      public int getTimer() {
            return timer;
      }

      public Iterable<ItemStack> getContent() {
            return Iterables.concat(tools.stacks.values(), weapons.stacks.values());
      }

      public void tick(Inventory inventory) {
            int toolsSize = getToolsSize();
            if (oToolSize > toolsSize)
                  dropOverflowItems(tools);

            int weaponsSize = getWeaponsSize();
            if (oWeaponSize > weaponsSize)
                  dropOverflowItems(weapons);

            int slot = inventory.selected - inventory.items.size();
            tools.stacks.forEach((i, stack) ->
                        stack.inventoryTick(owner.level(), inventory.player, i, slot == i)
            );

            int selected = slot - tools.getContainerSize();
            weapons.stacks.forEach((i, stack) ->
                        stack.inventoryTick(owner.level(), inventory.player, i, selected == i)
            );

            if (owner instanceof ServerPlayer serverPlayer) {
                  int selectedWeapon = getSelectedWeapon();
                  ItemStack weapon;
                  if (selected == selectedWeapon)
                        weapon = ItemStack.EMPTY;
                  else if (selected >= 0) {
                        selectedWeapon = selected;
                        this.selectedWeapon = selectedWeapon;
                        weapon = ItemStack.EMPTY;
                  } else
                        weapon = weapons.getItem(selectedWeapon);

                  if (oWeapon != weapon) {
                        oWeapon = weapon;
                        SendWeaponSlot.send(serverPlayer, selectedWeapon, weapon);
                  }
            }

            oToolSize = tools.getContainerSize();
            oWeaponSize = weapons.getContainerSize();

            tickToolBeltTimer(inventory, slot);
      }

      private void tickToolBeltTimer(Inventory inventory, int slot) {
            if (size() <= slot || slot < 0) {
                  if (inventory.selected >= inventory.items.size())
                        inventory.selected = heldSelected;
                  timer = 0;
                  return;
            }

            if (timer == 1)
                  resetSelected(inventory);

            if (timer > 0) {
                  if (getItem(slot).isEmpty())
                        resetSelected(inventory);
                  timer--;
            }
      }

      public void resetSelected(Inventory inventory) {
            if (inventory.selected >= inventory.items.size())
                  inventory.selected = heldSelected;
            timer = 0;
      }

      public int getSelected(Inventory instance) {
            int slot = instance.selected - instance.items.size();
            if (size() > slot && slot >= 0)
                  return slot;
            else {
                  resetSelected(instance);
                  return -1;
            }
      }

      public void replaceWith(Shorthand that) {
            this.weapons.replaceWith(that.weapons);
            this.tools.replaceWith(that.tools);

            ItemStack backpack = that.owner.getItemBySlot(EquipmentSlot.BODY);
            this.owner.setItemSlot(EquipmentSlot.BODY, backpack);
      }

      public void updateSelectedWeapon(int selectedSlot, ItemStack stack) {
            selectedWeapon = selectedSlot;
            weapons.setItem(selectedSlot, stack);
      }

      public void clearContent() {
            Iterator<ItemStack> iterator = getContent().iterator();
            while (iterator.hasNext()) {
                  iterator.next();
                  iterator.remove();
            }
      }

      public boolean setSlot(Inventory inventory, int slot) {
            setHeldSelected(inventory.selected);
            inventory.selected = inventory.items.size() + tools.getContainerSize() + slot;
            selectedWeapon = slot;
            return false;
      }
}
