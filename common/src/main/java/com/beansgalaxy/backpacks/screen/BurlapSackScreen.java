package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.bundle.BundleMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BurlapSackScreen extends AbstractContainerScreen<BurlapSackMenu> {
      public static final ResourceLocation TEXTURE =
                  Constants.defaultLocation("textures/gui/burlap_sack_menu.png");

      public BurlapSackScreen(BurlapSackMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
            super(pMenu, pPlayerInventory, pTitle);
            this.imageHeight = 186;
            this.inventoryLabelY = 92;
      }
      
      @Override
      public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
      }
      
      @Override
      protected void renderSlot(GuiGraphics gui, Slot slot) {
            int i = slot.x;
            int j = slot.y;
            ItemStack itemstack = slot.getItem();
            boolean flag = false;
            boolean flag1 = slot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
            ItemStack itemstack1 = this.menu.getCarried();
            String s = null;
            int j1;
            if (slot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
                  itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
            } else if (this.isQuickCrafting && this.quickCraftSlots.contains(slot) && !itemstack1.isEmpty()) {
                  if (this.quickCraftSlots.size() == 1) {
                        return;
                  }
                  
                  if (AbstractContainerMenu.canItemQuickReplace(slot, itemstack1, true) && this.menu.canDragTo(slot)) {
                        flag = true;
                        j1 = Math.min(itemstack1.getMaxStackSize(), slot.getMaxStackSize(itemstack1));
                        int l = slot.getItem().isEmpty() ? 0 : slot.getItem().getCount();
                        int i1 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack1) + l;
                        if (i1 > j1) {
                              i1 = j1;
                              String var10000 = ChatFormatting.YELLOW.toString();
                              s = var10000 + j1;
                        }
                        
                        itemstack = itemstack1.copyWithCount(i1);
                  } else {
                        this.quickCraftSlots.remove(slot);
                        this.recalculateQuickCraftRemaining();
                  }
            }
            
            gui.nextStratum();
            if (itemstack.isEmpty() && slot.isActive()) {
                  ResourceLocation resourcelocation = slot.getNoItemIcon();
                  if (resourcelocation != null) {
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, resourcelocation, i, j, 16, 16);
                        flag1 = true;
                  }
            }
            
            if (!flag1) {
                  if (flag) {
                        gui.fill(i, j, i + 16, j + 16, -2130706433);
                  }
                  
                  j1 = slot.x + slot.y * this.imageWidth;
                  if (slot.isFake()) {
                        gui.renderFakeItem(itemstack, i, j, j1);
                  } else {
                        gui.renderItem(itemstack, i, j, j1);
                  }
                  
                  CommonClient.renderItemDecorations(gui, this.font, itemstack, i + 8, j + 8);
            }
      }

      @Override
      protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
            int i = this.leftPos;
            int j = this.topPos;
            pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

            if (hoveredSlot instanceof BurlapSackMenu.BurlapSackSlot slot) {
                  if (!slot.isHighlightable()) {
                        int lastOpenSlot = slot.index - slot.getContainerSlot() + menu.entity.getSize();
                        Slot lastSlot = menu.slots.get(lastOpenSlot);
                        BundleMenu.renderHighlight(pGuiGraphics, lastSlot.x + leftPos, lastSlot.y + topPos);
                  }
            }
      }
}
