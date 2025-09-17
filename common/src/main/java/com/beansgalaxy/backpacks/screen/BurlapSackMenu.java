package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import com.beansgalaxy.backpacks.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public class BurlapSackMenu extends AbstractContainerMenu {
      public final AbstractBurlapSackEntity entity;
      private final Level level;

      public BurlapSackMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
            this(containerId, inventory, inventory.player.level().getBlockEntity(buf.readBlockPos()));
      }

      public BurlapSackMenu(int containerId, Inventory inventory, BlockEntity entity) {
             super(Services.PLATFORM.getBurlapSackMenuType(), containerId);
             this.entity = ((AbstractBurlapSackEntity) entity);
             this.level = inventory.player.level();

             addPlayerHotbar(inventory);
             addPlayerInventory(inventory);

             addBurlapSackSlots();
      }

      public BurlapSackMenu(int containerId, Inventory inventory, BlockPos pos) {
            this(containerId, inventory, inventory.player.level().getBlockEntity(pos));
      }

      @Override
      public ItemStack quickMoveStack(Player player, int index) {
            Slot slot = slots.get(index);
            ItemStack clicked = slot.getItem();

            if (index > 35) {
                  int stopCount = Math.min(clicked.getCount(), clicked.getMaxStackSize());

                  stopCount = tryStack(8, 0, clicked, stopCount);
                  if (stopCount == 0)
                        return ItemStack.EMPTY;

                  stopCount = tryStack(35, 8, clicked, stopCount);
                  if (stopCount == 0)
                        return ItemStack.EMPTY;

                  stopCount = findEmpty(8, 0, clicked, stopCount);
                  if (stopCount == 0)
                        return ItemStack.EMPTY;

                  stopCount = findEmpty(35, 8, clicked, stopCount);
                  if (stopCount == 0)
                        return ItemStack.EMPTY;

            }
            else {
                  int space = entity.getRemainingSpace(clicked);
                  if (space == 0)
                        return ItemStack.EMPTY;

                  for (ItemStack stack : entity.getItemStacks()) {
                        if (ItemStack.isSameItemSameComponents(stack, clicked)) {
                              int count = Math.min(clicked.getCount(), space);
                              stack.grow(count);
                              clicked.shrink(count);
                              return ItemStack.EMPTY;
                        }
                  }
                  entity.addItem(clicked);
            }
            return ItemStack.EMPTY;
      }

      private int tryStack(int start, int low, ItemStack clicked, int stopCount) {
            for (int i = start; i > low; i--) {
                  Slot iSlot = slots.get(i);
                  ItemStack stack = iSlot.getItem();
                  if (stack.isEmpty())
                        continue;

                  int count = stack.getCount();
                  int maxStackSize = iSlot.getMaxStackSize(stack);
                  if (count < maxStackSize && ItemStack.isSameItemSameComponents(stack, clicked)) {
                        int min = Math.min(maxStackSize - count, stopCount);

                        stack.grow(min);
                        clicked.shrink(min);
                        stopCount -= min;

                        iSlot.setChanged();

                        if (clicked.isEmpty()) {
                              entity.getItemStacks().remove(clicked);
                              return 0;
                        }
                  }
            }

            return stopCount;
      }

      private int findEmpty(int start, int low, ItemStack clicked, int endCount) {
            for (int i = start; i > low; i--) {
                  Slot iSlot = slots.get(i);
                  if (!iSlot.getItem().isEmpty())
                        continue;

                  int maxStackSize = iSlot.getMaxStackSize(clicked);
                  int toMove = Math.min(endCount, maxStackSize);
                  iSlot.setByPlayer(clicked.copyWithCount(toMove));
                  clicked.shrink(toMove);
                  endCount -= toMove;

                  if (clicked.isEmpty()) {
                        entity.getItemStacks().remove(clicked);
                        return 0;
                  }
            }

            return endCount;
      }

      @Override
      public boolean stillValid(Player pPlayer) {
            return stillValid(ContainerLevelAccess.create(level, entity.getBlockPos()), pPlayer, ModBlocks.BURLAP_SACK.get());
      }

      private void addPlayerHotbar(Inventory inventory) {
            for (int i = 0; i < 9; i++) {
                  this.addSlot(new Slot(inventory, i, 8 + i * 18, 162));
            }
      }

      private void addPlayerInventory(Inventory inventory) {
            for (int i = 0; i < 3; i++) {
                  for (int j = 0; j < 9; j++) {
                        this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
                  }
            }
      }

      private void addBurlapSackSlots() {
            for (int y = 0; y < 4; y++) {
                  for (int x = 0; x < 9; x++) {
                        addSlot(new BurlapSackSlot(x + y * 9, 8 + x * 18, 72 + y * -18));
                  }
            }
      }

      class BurlapSackSlot extends Slot {
            public BurlapSackSlot(int pSlot, int pX, int pY) {
                  super(entity, pSlot, pX, pY);
            }

            @Override
            public void setByPlayer(ItemStack pStack) {
                  entity.addItem(getContainerSlot(), pStack);
                  setChanged();
            }

            @Override
            public void setByPlayer(ItemStack pNewStack, ItemStack pOldStack) {
                  setByPlayer(pNewStack);
            }

            @Override
            public boolean isHighlightable() {
                  return entity.getContainerSize() > getContainerSlot();
            }

            public ItemStack safeInsert(ItemStack stack, int increment) {
                  if (!stack.isEmpty() && this.mayPlace(stack))
                        entity.addItem(getContainerSlot(), stack, increment);

                  return stack;
            }

            @Override
            public boolean mayPlace(ItemStack pStack) {
                  return entity.getRemainingSpace(pStack) > pStack.getCount();
            }
      }

      @Override
      public void clicked(int pSlotId, int pButton, ClickType pClickType, Player pPlayer) {
            if (ClickType.SWAP.equals(pClickType))
                  return;

            super.clicked(pSlotId, pButton, pClickType, pPlayer);
      }
}

