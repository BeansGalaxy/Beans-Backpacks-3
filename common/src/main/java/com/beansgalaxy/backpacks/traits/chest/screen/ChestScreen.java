package com.beansgalaxy.backpacks.traits.chest.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.serverbound.TinyHotbarClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuClick;
import com.beansgalaxy.backpacks.network.serverbound.TinyMenuInteract;
import com.beansgalaxy.backpacks.screen.BackpackScreen;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.List;
import java.util.Optional;

public class ChestScreen extends BackpackScreen {
      public static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.parse(Constants.MOD_ID + ":textures/gui/generic_scalable.png");
      private final ChestTraits traits;

      public static void openScreen(ViewableBackpack backpack, ChestTraits traits, Player player) {
            ChestScreen screen = new ChestScreen(backpack, traits, player);
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.setScreen(screen);
            backpack.onOpen(minecraft.player);
            TinyMenuInteract.send(backpack.getId(), true);
      }

      public ChestScreen(ViewableBackpack backpack, ChestTraits traits, Player player) {
            super(player.inventoryMenu, player.getInventory(), backpack);
            this.traits = traits;
      }

      @Override
      protected void init() {
            super.init();

            int width = traits.columns * 18;
            this.traitX = leftPos - 18;
            int left = traitX - width;
            int h = minecraft.getWindow().getGuiScaledHeight() / 2;
            int top = h - ((traits.rows - 1) / 2) * 9;
            for(int y = 0; y < traits.rows; ++y) {
                  for (int x = 0; x < traits.columns; ++x) {
                        int index = y * traits.columns + x;
                        ChestSlot slot = new ChestSlot(x * 18 + left, y * 18 + top, index);
                        addSlot(slot);
                        addRenderableWidget(slot);
                  }
            }

            traitY = top;
            traitW = width;
            traitH = traits.rows * 18;
      }

      @Override
      protected void tinyHotbarClick(TinyClickType clickType, InventoryMenu menu, LocalPlayer player, int index) {
            traits.tinyHotbarClick(backpack, index, clickType, menu, player);
            TinyHotbarClick.send(backpack, index, clickType);
      }

      @Override
      protected void tinyMenuClick(int index, TinyClickType clickType, SlotAccess carriedAccess, LocalPlayer player) {
            traits.tinyMenuClick(backpack, index, clickType, carriedAccess, player);
            TinyMenuClick.send(backpack, index, clickType);
      }

      @Override
      protected void repopulateSlots(GuiGraphics gui, int pMouseX, int pMouseY, float tick) {

      }

      private class ChestSlot extends TraitSlot {
            public ChestSlot(int pX, int pY, int index) {
                  super(pX, pY, index);
            }

            @Override
            public ItemStack getItem() {
                  ItemContainerContents contents = backpack.get(ITraitData.CHEST);
                  if (contents == null)
                        return ItemStack.EMPTY;

                  NonNullList<ItemStack> pList = NonNullList.withSize(traits.size(), ItemStack.EMPTY);
                  contents.copyInto(pList);
                  return index < pList.size() ? pList.get(index) : ItemStack.EMPTY;
            }

            @Override
            protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float v) {
                  ItemStack stack = getItem();
                  boolean focused = true;
                  boolean hovered = isHovered() && focused;
                  int x = getX() + 9;
                  int y = getY() + 9;

                  gui.blit(CONTAINER_BACKGROUND, x - 9, y - 9, 0, 0, 0, 18, 18, 256, 256);

                  if (!stack.isEmpty()) {
                        Minecraft minecraft = Minecraft.getInstance();
                        CommonClient.renderItem(minecraft, gui, stack, x, y, 200, false);
                        CommonClient.renderItemDecorations(gui, font, stack, x, y, 200);

                        if (hovered && getCarried().isEmpty()) {
                              List<Component> tooltipFromItem = Screen.getTooltipFromItem(minecraft, stack);
                              Optional<TooltipComponent> tooltipImage = stack.getTooltipImage();
                              gui.renderTooltip(font, tooltipFromItem, tooltipImage, mouseX, mouseY);
                        }
                  }

                  if (hovered)
                        gui.fill(x - 8, y - 8, x + 8, y + 8, 100, 0x88FFFFFF);

            }
      }
}
