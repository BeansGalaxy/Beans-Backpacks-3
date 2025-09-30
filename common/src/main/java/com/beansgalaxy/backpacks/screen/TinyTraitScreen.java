package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class TinyTraitScreen extends Screen {
      protected final List<TinyTraitSlot> slots = new ArrayList<>();
      protected int leftPos;
      private int topPos;

      protected TinyTraitScreen(Component pTitle) {
            super(pTitle);
      }

      protected abstract void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index);

      protected abstract void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player);

      protected static TinyClickType getClickType(Minecraft minecraft, int button, Player player) {
            BackData backData = BackData.get(player);
            if (backData.isMenuKeyDown() && backData.getTinySlot() == -1) {
                  return TinyClickType.ACTION;
            }

            boolean eitherShiftDown = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 340)
                        || InputConstants.isKeyDown(minecraft.getWindow().getWindow(), 344);

            if (eitherShiftDown)
                  return TinyClickType.SHIFT;

            if (button == 1)
                  return TinyClickType.RIGHT;

            return TinyClickType.LEFT;
      }

      public void initHotBarSlots() {
            Window window = minecraft.getWindow();
            int scaledHeight = window.getGuiScaledHeight();
            int scaledWidth = window.getGuiScaledWidth();
            for (int i = 0; i < 9; i++) {
                  int x = scaledWidth / 2 + i * 20 - 89;
                  HotBarSlot slot = new HotBarSlot(x, scaledHeight - 20, i);
                  addRenderableWidget(slot);
            }
      }

      @Override
      public void render(GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            ItemStack carried = getCarried();
            CommonClient.renderItem(minecraft, gui, carried, pMouseX, pMouseY, 300, false);
            CommonClient.renderItemDecorations(gui, font, carried, pMouseX, pMouseY, 300);
            super.render(gui, pMouseX, pMouseY, pPartialTick);
      }

      @Override
      public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

      }

      @Override
      public boolean isPauseScreen() {
            return false;
      }

      @Override
      public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
            KeyMapping[] hotbarSlots = minecraft.options.keyHotbarSlots;
            for (int i = 0; i < hotbarSlots.length; i++) {
                  KeyMapping hotbarSlot = hotbarSlots[i];
                  if (hotbarSlot.matches(pKeyCode, pScanCode)) {
                        TinyTraitSlot slot = getHoveredSlot();
                        if (slot != null) {
                              slot.hotbarClick(i);
                        }
                        return true;
                  }
            }
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      public ItemStack getCarried() {
            return minecraft.player.containerMenu.getCarried();
      }

      private @Nullable TinyTraitSlot getHoveredSlot() {
            MouseHandler mouseHandler = minecraft.mouseHandler;
            double x = mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
            double y = mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
            for (TinyTraitSlot slot : slots) {
                  if (slot.isMouseOver(x, y)) {
                        return slot;
                  }
            }
            return null;
      }

      @Override
      public void onClose() {
            this.minecraft.setScreen(null);
      }

      public void addSlot(TinyTraitSlot widget) {
            slots.add(widget);
      }

      public void clearSlots() {
            slots.clear();
      }

      public int topPosOffset() {
            return 0;
      }

      public int getTopPos() {
            return topPos + topPosOffset();
      }

      public void setTopPos(int topPos) {
            this.topPos = topPos;
      }

      public abstract class TinyTraitSlot extends AbstractWidget {
            protected final int index;

            public TinyTraitSlot(int pX, int pY, int index) {
                  super(pX, pY, 18, 18, Component.empty());
                  this.index = index;
            }

            public abstract ItemStack getItem();

            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int button) {
                  if (this.active && this.visible && this.clicked(pMouseX, pMouseY)) {
                        LocalPlayer player = minecraft.player;

                        TinyClickType clickType = getClickType(minecraft, button, player);
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

                        tinyMenuClick(index, clickType, carriedAccess, player);
                        return true;
                  }
                  return false;
            }

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

                  TinyClickType clickType = TinyClickType.getHotbar(hotbarSlot);
                  tinyMenuClick(index, clickType, carriedAccess, player);
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }

            @Override
            public void playDownSound(SoundManager pHandler) {

            }
      }

      public class HotBarSlot extends AbstractWidget {
            private final int index;

            public HotBarSlot(int pX, int pY, int index) {
                  super(pX, pY, 18, 18, Component.empty());
                  this.index = index;
            }

            @Override
            public boolean mouseClicked(double pMouseX, double pMouseY, int button) {
                  if (this.active && this.visible && this.clicked(pMouseX, pMouseY)) {
                        LocalPlayer player = minecraft.player;
                        InventoryMenu menu = player.inventoryMenu;

                        TinyClickType clickType = getClickType(minecraft, button, player);
                        tinyHotbarClick(clickType, menu, player, index);

                        return true;
                  }
                  return false;
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
                  if (isHovered()) {
                        int x = getX() + 9;
                        int y = getY() + 9;
                        gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x88FFFFFF);

                        if (getCarried().isEmpty()) {
                              Inventory inventory = minecraft.player.getInventory();
                              ItemStack stack = inventory.items.get(index);
                              if (!stack.isEmpty()) {
                                    List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                                    Optional<TooltipComponent> tooltipImage = stack.getTooltipImage();
                                    gui.renderTooltip(font, tooltipFromItem, tooltipImage, mouseX, mouseY);
                              }
                        }
                  }
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }
      }
}
