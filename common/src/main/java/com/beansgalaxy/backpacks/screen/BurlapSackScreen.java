package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BurlapSackScreen extends AbstractContainerScreen<BurlapSackMenu> {
      public static final ResourceLocation TEXTURE =
                  ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/burlap_sack_menu.png");

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
      protected void renderSlot(GuiGraphics pGuiGraphics, Slot pSlot) {
            int i = pSlot.x;
            int j = pSlot.y;
            ItemStack itemstack = pSlot.getItem();
            boolean flag = false;
            boolean flag1 = pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && !this.isSplittingStack;
            ItemStack itemstack1 = this.menu.getCarried();
            String s = null;
            int j1;
            if (pSlot == this.clickedSlot && !this.draggingItem.isEmpty() && this.isSplittingStack && !itemstack.isEmpty()) {
                  itemstack = itemstack.copyWithCount(itemstack.getCount() / 2);
            } else if (this.isQuickCrafting && this.quickCraftSlots.contains(pSlot) && !itemstack1.isEmpty()) {
                  if (this.quickCraftSlots.size() == 1) {
                        return;
                  }
                  
                  if (AbstractContainerMenu.canItemQuickReplace(pSlot, itemstack1, true) && this.menu.canDragTo(pSlot)) {
                        flag = true;
                        j1 = Math.min(itemstack1.getMaxStackSize(), pSlot.getMaxStackSize(itemstack1));
                        int l = pSlot.getItem().isEmpty() ? 0 : pSlot.getItem().getCount();
                        int i1 = AbstractContainerMenu.getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, itemstack1) + l;
                        if (i1 > j1) {
                              i1 = j1;
                              String var10000 = ChatFormatting.YELLOW.toString();
                              s = var10000 + j1;
                        }
                        
                        itemstack = itemstack1.copyWithCount(i1);
                  } else {
                        this.quickCraftSlots.remove(pSlot);
                        this.recalculateQuickCraftRemaining();
                  }
            }
            
            pGuiGraphics.pose().pushPose();
            pGuiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
            if (itemstack.isEmpty() && pSlot.isActive()) {
                  Pair<ResourceLocation, ResourceLocation> pair = pSlot.getNoItemIcon();
                  if (pair != null) {
                        TextureAtlasSprite textureatlassprite = this.minecraft.getTextureAtlas(pair.getFirst()).apply(pair.getSecond());
                        pGuiGraphics.blit(i, j, 0, 16, 16, textureatlassprite);
                        flag1 = true;
                  }
            }
            
            if (!flag1) {
                  if (flag) {
                        pGuiGraphics.fill(i, j, i + 16, j + 16, -2130706433);
                  }
                  
                  j1 = pSlot.x + pSlot.y * this.imageWidth;
                  if (pSlot.isFake()) {
                        pGuiGraphics.renderFakeItem(itemstack, i, j, j1);
                  } else {
                        pGuiGraphics.renderItem(itemstack, i, j, j1);
                  }
                  
                  CommonClient.renderItemDecorations(pGuiGraphics, this.font, itemstack, i + 8, j + 8, 190);
            }
            
            pGuiGraphics.pose().popPose();
      }

      @Override
      protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
            int i = this.leftPos;
            int j = this.topPos;
            pGuiGraphics.blit(TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);

            if (hoveredSlot instanceof BurlapSackMenu.BurlapSackSlot slot) {
                  if (!slot.isHighlightable()) {
                        int lastOpenSlot = slot.index - slot.getContainerSlot() + menu.entity.getSize();
                        Slot lastSlot = menu.slots.get(lastOpenSlot);
                        renderSlotHighlight(pGuiGraphics, lastSlot.x + leftPos, lastSlot.y + topPos, 0);
                  }
            }
      }
}
