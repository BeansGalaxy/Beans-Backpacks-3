package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.Constants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class BurlapSackScreen extends AbstractContainerScreen<BurlapSackMenu> {
      public static final ResourceLocation TEXTURE =
                  ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/burlap_sack_menu.png");

      public BurlapSackScreen(BurlapSackMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
            super(pMenu, pPlayerInventory, pTitle);
            this.imageHeight = 186;
            this.inventoryLabelY = 92;
      }

      @Override
      protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
            RenderSystem.setShader(GameRenderer::getPositionShader);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.setShaderTexture(0, TEXTURE);

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

            if (hoveredSlot instanceof BurlapSackMenu.BurlapSackSlot slot) {
                  if (!slot.isHighlightable()) {
                        int lastOpenSlot = slot.index - slot.getContainerSlot() + menu.entity.getSize();
                        Slot lastSlot = menu.slots.get(lastOpenSlot);
                        renderSlotHighlight(pGuiGraphics, lastSlot.x + leftPos, lastSlot.y + topPos, 0);
                  }
            }
      }
}
