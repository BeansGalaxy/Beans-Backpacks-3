package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuInteract;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.recipebook.CraftingRecipeBookComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class BackpackScreen extends AbstractRecipeBookScreen<InventoryMenu> {
      protected final List<TraitSlot> slots = new ArrayList<>();
      protected int traitX = 0, traitY = 0, traitW = 0, traitH = 0;
      protected final ViewableBackpack backpack;

      public BackpackScreen(InventoryMenu pMenu, Inventory pPlayerInventory, ViewableBackpack backpack) {
            super(pMenu, new CraftingRecipeBookComponent(pPlayerInventory.player.inventoryMenu), pPlayerInventory, Component.translatable("container.crafting"));
            this.backpack = backpack;
            this.titleLabelX = 97;
      }

      @Override
      protected void init() {
            super.init();
            Window window = minecraft.getWindow();
            int scaledHeight = window.getGuiScaledHeight();
            int scaledWidth = window.getGuiScaledWidth();
            leftPos = scaledWidth / 2 - 12;
            topPos = (scaledHeight) / 2 - 83;
      }

      @Override
      public void onClose() {
            minecraft.setScreen(null);
            TinyMenuInteract.send(backpack.getId(), false);
      }

      protected abstract void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player);

      protected abstract void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index);

      protected static TinyClickType getClickType(Minecraft minecraft, int button, Player player) {
            BackData backData = BackData.get(player);
            if (backData.isMenuKeyDown() && backData.getTinySlot() == -1) {
                  return TinyClickType.ACTION;
            }

            boolean eitherShiftDown = InputConstants.isKeyDown(minecraft.getWindow(), 340)
                        || InputConstants.isKeyDown(minecraft.getWindow(), 344);

            if (eitherShiftDown)
                  return TinyClickType.SHIFT;

            if (button == 1)
                  return TinyClickType.RIGHT;

            return TinyClickType.LEFT;
      }


      @Override
      protected void slotClicked(@NotNull Slot pSlot, int pSlotId, int pMouseButton, @NotNull ClickType pType) {
            LocalPlayer player = this.minecraft.player;
            if (pSlot == null) { // IN THE VANILLA CODE, DEV INCLUDED A NULL CHECK FOR A PARAM LABELED NOT NULL ¯\_(ツ)_/¯
                  this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, pSlotId, pMouseButton, pType, player);
                  return;
            }
            
            if (pType == ClickType.QUICK_MOVE) {
                  TinyClickType tinyType = pSlot instanceof ResultSlot
                        ? TinyClickType.CRAFT
                        : TinyClickType.SHIFT;
                  
                  tinyHotbarClick(tinyType, menu, player, pSlot.index);
            }
            else {
                  this.minecraft.gameMode.handleInventoryMouseClick(this.menu.containerId, pSlot.index, pMouseButton, pType, player);
            }
      }

      @Override @NotNull
      public List<? extends GuiEventListener> children() {
            return Stream.concat(super.children().stream(), slots.stream()).toList();
      }
      
      @Override
      public boolean keyPressed(KeyEvent event) {
            TraitSlot slot = getHoveredSlot();
            if (slot != null) {
                  Options options = minecraft.options;
                  if (options.keyDrop.matches(event)) {
                        slot.dropItem();
                        return true;
                  }

                  KeyMapping[] hotbarSlots = options.keyHotbarSlots;
                  for (int i = 0; i < hotbarSlots.length; i++) {
                        KeyMapping hotbarSlot = hotbarSlots[i];
                        if (hotbarSlot.matches(event)) {
                              slot.hotbarClick(i);
                              return true;
                        }
                  }
            }
            return super.keyPressed(event);
      }

      private @Nullable TraitSlot getHoveredSlot() {
            MouseHandler mouseHandler = minecraft.mouseHandler;
            double x = mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
            double y = mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
            for (TraitSlot slot : slots) {
                  if (slot.isMouseOver(x, y)) {
                        return slot;
                  }
            }
            return null;
      }

      @Override
      public void render(@NotNull GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            if (backpack.shouldClose())
                  onClose();

            repopulateSlots(gui, pMouseX, pMouseY, pPartialTick);
            super.render(gui, pMouseX, pMouseY, pPartialTick);

            List<Component> lines = Screen.getTooltipFromItem(minecraft, backpack.toStack());
            int width = 0;
            for (Component line : lines) {
                  int i = font.width(line);
                  if (i > width)
                        width = i;
            }
            
            List<ClientTooltipComponent> list = lines.stream()
                  .map(Component::getVisualOrderText)
                  .map(ClientTooltipComponent::create)
                  .collect(Util.toMutableList());

            width += 18 + 5 + 33;
            gui.renderTooltip(font, list, leftPos - width + 19, traitY - 12 * lines.size() + 9,
                  DefaultTooltipPositioner.INSTANCE, backpack.get(DataComponents.TOOLTIP_STYLE)
            );
            
            this.renderTooltip(gui, pMouseX, pMouseY);
      }

      protected abstract void repopulateSlots(GuiGraphics gui, int x, int y, float tick);

      public void addSlot(TraitSlot widget) {
            slots.add(widget);
      }

      public void clearSlots() {
            slots.clear();
      }

      public ItemStack getCarried() {
            return menu.getCarried();
      }

      @Override
      protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
            pGuiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
      }
      
      @Override
      protected boolean hasClickedOutside(double x, double y, int left, int top) {
            if (x > traitX - traitW && y > traitY && x < traitX && y < traitY + traitH)
                  return false;
            
            return super.hasClickedOutside(x, y, left, top);
      }

      public abstract class TraitSlot extends AbstractWidget {
            protected final int index;

            public TraitSlot(int pX, int pY, int index) {
                  super(pX, pY, 18, 18, Component.empty());
                  this.index = index;
            }

            public abstract ItemStack getItem();
            
            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
                  if (this.active && this.visible && this.isMouseOver(event.x(), event.y())) {
                        LocalPlayer player = minecraft.player;

                        TinyClickType clickType = getClickType(minecraft, event.button(), player);
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

            @Override
            protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

            }

            @Override
            public void playDownSound(SoundManager pHandler) {

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

            public void dropItem() {
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

                  tinyMenuClick(index, TinyClickType.DROP, carriedAccess, player);
            }
      }

      @Override
      protected void renderBg(GuiGraphics gui, float v, int pMouseX, int pMouseY) {
            int left = this.leftPos;
            int top = this.topPos;
            gui.blit(RenderPipelines.GUI_TEXTURED, INVENTORY_LOCATION, left, top, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
            InventoryScreen.renderEntityInInventoryFollowsMouse(gui, left + 26, top + 8, left + 75, top + 78, 30, 0.0625F, pMouseX, pMouseY, this.minecraft.player);
            CommonClient.renderSlots(gui, leftPos, topPos, imageWidth, imageHeight, minecraft.player);
      }
      
      @Override
      protected ScreenPosition getRecipeBookButtonPosition() {
            return new ScreenPosition(this.leftPos + 104, this.height / 2 - 22);
      }
}
