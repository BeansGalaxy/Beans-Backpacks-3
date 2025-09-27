package com.beansgalaxy.backpacks.traits.chest.screen;

import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.network.serverbound.TinyChestClick;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class MenuChestScreen extends TinyChestScreen {
      private final AbstractContainerScreen<?> previousScreen;
      private final Slot slot;
      private boolean hideChestMenu = false;

      private MenuChestScreen(AbstractContainerScreen<?> previousScreen, ChestTraits traits, Slot slot) {
            super(traits);
            this.previousScreen = previousScreen;
            this.slot = slot;
      }

      @Override
      public int topPosOffset() {
            return -traits.rows * 3;
      }

      public static void openWithSlot(Minecraft minecraft, AbstractContainerScreen<?> previousScreen, ChestTraits traits, Slot slot) {
            MenuChestScreen menuChestScreen = new MenuChestScreen(previousScreen, traits, slot);
            minecraft.setScreen(menuChestScreen);
      }

      public static void openWithHand(Minecraft minecraft, Player player, ChestTraits traits, Slot slot) {
            InventoryScreen inventoryScreen = new InventoryScreen(player);
            minecraft.setScreen(inventoryScreen);
            MenuChestScreen menuChestScreen = new MenuChestScreen(inventoryScreen, traits, slot);
            menuChestScreen.hideChestMenu = true;
            minecraft.setScreen(menuChestScreen);
      }

      public void swap(ChestTraits chestTraits, Slot slot) {
            onClose();
            MenuChestScreen menuChestScreen = new MenuChestScreen(previousScreen, chestTraits, slot);
            KeyPress.isPressed menusKey = KeyPress.isPressed(minecraft, KeyPress.getMenusKeyBind());
            menuChestScreen.hideChestMenu = !menusKey.pressed();
            minecraft.setScreen(menuChestScreen);
      }

      public int slotIndex() {
            return slot.index;
      }

      @Override
      public ItemStack getStack() {
            return slot.getItem();
      }

      @Override
      protected void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player) {
            traits.tinySubMenuClick(getHolder(), index, clickType, carriedAccess, player);
            TinyChestClick.send(previousScreen.getMenu().containerId, slot, index, clickType);
      }

      @Override
      public ComponentHolder getHolder() {
            ItemStack stack = getStack();
            Optional<EnderTraits> optional = EnderTraits.get(stack);
            if (optional.isPresent())
                  return optional.get();

            return ComponentHolder.of(stack);
      }

      @Override
      protected void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index) {

      }

      @Override
      public void render(GuiGraphics gui, int pMouseX, int pMouseY, float pPartialTick) {
            ItemStack stack = getStack();
            if (stack.isEmpty() || ChestTraits.get(getHolder()).or(() ->
                        EnderTraits.get(stack).flatMap(enderTraits -> {
                              GenericTraits trait = enderTraits.getTrait(minecraft.level);
                              if (trait instanceof ChestTraits chestTraits)
                                    return Optional.of(chestTraits);
                              return Optional.empty();
                        })).isEmpty())
            {
                  previousScreen.render(gui, pMouseX, pMouseY, pPartialTick);
                  onClose();
                  return;
            }

            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(0, 0, -1000);
            if (hideChestMenu) {
                  super.render(gui, pMouseX, pMouseY, pPartialTick);
                  pose.popPose();

                  previousScreen.render(gui, pMouseX, pMouseY, pPartialTick);
            }
            else {
                  previousScreen.render(gui, pMouseX, pMouseY, pPartialTick);
                  pose.popPose();

                  super.render(gui, pMouseX, pMouseY, pPartialTick);

            }
      }

      @Override
      protected void init() {
            super.init();
            Window window = minecraft.getWindow();
            previousScreen.init(minecraft, window.getGuiScaledWidth(), window.getGuiScaledHeight());
      }

      @Override
      public boolean isFocused() {
            return !hideChestMenu;
      }

      @Override
      public void tick() {
            KeyPress.isPressed menusKey = KeyPress.isPressed(minecraft, KeyPress.getMenusKeyBind());
            if (menusKey.pressed() == this.hideChestMenu) {
                  hideChestMenu = !menusKey.pressed();
            }

            previousScreen.tick();
            super.tick();
      }

      private boolean clickOutOfBounds(double pMouseX, double pMouseY) {
            int height = traits.rows * 18;
            if (pMouseY < getTopPos() || pMouseY > getTopPos() + height) {
                  return true;
            }

            int width = traits.columns * 18;
            return pMouseX < leftPos || pMouseX > leftPos + width;
      }

      @Override
      public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (hideChestMenu || clickOutOfBounds(pMouseX, pMouseY)) {
                  return previousScreen.mouseClicked(pMouseX, pMouseY, pButton);
            }
            return super.mouseClicked(pMouseX, pMouseY, pButton);
      }

      @Override
      public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
            if (hideChestMenu || clickOutOfBounds(pMouseX, pMouseY)) {
                  return previousScreen.mouseReleased(pMouseX, pMouseY, pButton);
            }
            return super.mouseReleased(pMouseX, pMouseY, pButton);
      }

      @Override
      public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
            if (hideChestMenu || clickOutOfBounds(pMouseX, pMouseY)) {
                  return previousScreen.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
            }
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
      }

      @Override
      public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
            if (hideChestMenu || clickOutOfBounds(pMouseX, pMouseY)) {
                  return previousScreen.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
            }
            return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
      }

      @Override
      public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
            if (pKeyCode == 256 && KeyPress.isPressed(minecraft, KeyPress.getMenusKeyBind()).pressed()) {
                  this.onClose();
                  return true;
            }
            if (hideChestMenu) {
                  return previousScreen.keyPressed(pKeyCode, pScanCode, pModifiers);
            }
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }

      @Override
      public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
            if (hideChestMenu) {
                  return previousScreen.keyReleased(pKeyCode, pScanCode, pModifiers);
            }
            return super.keyReleased(pKeyCode, pScanCode, pModifiers);
      }

      @Override
      public void onClose() {
            this.minecraft.setScreen(previousScreen);
      }
}
