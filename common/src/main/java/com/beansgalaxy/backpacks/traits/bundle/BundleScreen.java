package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.network.serverbound.TinyHotbarClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuInteract;
import com.beansgalaxy.backpacks.screen.BackpackScreen;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BundleScreen extends BackpackScreen {
      protected final BundleLikeTraits traits;
      protected BundleScreen.BundleTraitSlot lastSlot = null;

      public static void openScreen(Player player, ViewableBackpack backpack, BundleLikeTraits traits) {
            Minecraft minecraft = Minecraft.getInstance();
            BundleScreen screen = new BundleScreen(player, backpack, traits);
            minecraft.setScreen(screen);
            backpack.onOpen(player);
            TinyMenuInteract.send(backpack.getId(), true);
      }

      protected BundleScreen(Player player, ViewableBackpack backpack, BundleLikeTraits traits) {
            super(player.inventoryMenu, player.getInventory(), backpack);
            this.traits = traits;
      }

      public int traitTop() {
            return topPos + 84;
      }

      @Override
      protected void repopulateSlots(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            ItemStack carried = getCarried();
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            boolean hasSpace;
            int size;

            if (stacks == null) {
                  hasSpace = true;
                  size = 1;
            }
            else {
                  hasSpace = traits.fullness(stacks).compareTo(Fraction.ONE) != 0;
                  size = stacks.size() + (hasSpace ? 1 : 0) + (carried.isEmpty() ? 0 : 1);
            }

            int guiScaledWidth = minecraft.getWindow().getGuiScaledWidth();
            int maxWidth = guiScaledWidth / 2 - 7 * 18;

            boolean forCol = false;
            int columns = Math.min(size, 4);
            int rows = 1;
            for (int i = columns; i <= size; i++) {
                  if (i > columns * rows) {
                        if (forCol && maxWidth > 0) {
                              columns++;
                              maxWidth -= 18;
                        } else
                              rows++;
                        forCol = !forCol;
                  }
            }

            clearSlots();
            int width = columns * 18;
            int left = leftPos - 18 - width;
            int top = traitTop() - 11 - ((rows - 1) / 2) * 10;
            int i = hasSpace ? -1 : 0;

            Component title = Component.empty();
            for(int y = 0; y < rows; ++y) {
                  for(int x = 0; x < columns; ++x) {
                        BundleTraitSlot slot = new BundleTraitSlot(left + x * 18, top + y * 18, i);
                        if (slot.isMouseOver(pMouseX, pMouseY)) {
                              ItemStack stack = slot.getItem();
                              if (!stack.isEmpty()) {
                                    List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                                    title = tooltipFromItem.getFirst();
                              }
                        }
                        addSlot(slot);
                        i++;
                        if (i == size - (carried.isEmpty() ? 0 : 1))
                              lastSlot = slot;
                  }
            }

            int fontWidth = font.width(title);
            int fontHeight = 9;
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(0, 0, 100);
            if (fontWidth > width) {
                  List<FormattedCharSequence> split = font.split(title, width);
                  int height = split.size() * 9;
                  int splitRow = height;
                  for (FormattedCharSequence sequence : split) {
                        gui.drawString(minecraft.font, sequence, left + 1, top - splitRow, 0xFFFFFFFF);
                        splitRow = splitRow - 9;
                  }
                  fontHeight = height;
            }
            else gui.drawString(minecraft.font, title, left + 1, top - 9, 0xFFFFFFFF);

            pose.popPose();

            this.traitX = left + width;
            this.traitY = top - fontHeight;
            this.traitW = width;
            this.traitH = rows * 18 + fontHeight;

            TooltipRenderUtil.renderTooltipBackground(gui, left, top - 1 - fontHeight, width, rows * 18 + 2 + fontHeight, 1);

            for (TraitSlot slot : slots) {
                  slot.render(gui, pMouseX, pMouseY, pPartialTick);
            }
      }

      @Override
      protected final void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player) {
            traits.tinyMenuClick(backpack, index, clickType, carriedAccess, player);
            TinyMenuClick.send(backpack, index, clickType);
      }

      @Override
      protected final void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index) {
            traits.tinyHotbarClick(backpack, index, clickType, menu, player);
            TinyHotbarClick.send(backpack, index, clickType);
      }

      private int lastSlotIndex() {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            return stacks == null ? 0 : stacks.size();
      }

      public class BundleTraitSlot extends TraitSlot {

            public BundleTraitSlot(int pX, int pY, int index) {
                  super(pX, pY, index);
            }

            public ItemStack getItem() {
                  List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || index == -1)
                        return ItemStack.EMPTY;

                  return index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int i, int i1, float v) {
                  ItemStack stack = getItem();
                  boolean hovered = isHovered();
                  int x = getX() + 9;
                  int y = getY() + 9;

                  if (!stack.isEmpty()) {
                        Minecraft minecraft = Minecraft.getInstance();
                        BundleTooltip.renderItem(minecraft, gui, stack, x, y, 50, false);
                        BundleTooltip.renderItemDecorations(gui, font, stack, x, y, 50);

                        if (hovered)
                              gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x80FFFFFF);
                  } else if (hovered) {
                        if (this == slots.getFirst())
                              gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x80FFFFFF);
                        else if (lastSlot != null) {
                              int x1 = lastSlot.getX() + 9;
                              int y1 = lastSlot.getY() + 9;
                              gui.fill(x1 - 8, y1 - 8, x1 + 8, y1 + 8, 100, 0x80FFFFFF);
                        }
                  }
            }

            @Override
            public void hotbarClick(int hotbarSlot) {
                  LocalPlayer player = minecraft.player;
                  AbstractContainerMenu menu = player.containerMenu;
                  SlotAccess carriedAccess = new SlotAccess() {
                        public ItemStack get() {
                              return menu.getCarried();
                        }

                        public boolean set(ItemStack p_150452_) {
                              menu.setCarried(p_150452_);
                              return true;
                        }
                  };

                  if (index == -1) {
                        tinyHotbarClick(TinyClickType.SHIFT, player.inventoryMenu, player, hotbarSlot);
                        return;
                  }

                  int lastSlot = lastSlotIndex();
                  if (index >= lastSlot)
                        tinyHotbarClick(TinyClickType.I_SHIFT, player.inventoryMenu, player, hotbarSlot);
                  else {
                        TinyClickType clickType = TinyClickType.getHotbar(hotbarSlot);
                        tinyMenuClick(index, clickType, carriedAccess, player);
                  }
            }
      }
}
