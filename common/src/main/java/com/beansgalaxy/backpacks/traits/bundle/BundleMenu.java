package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.network.serverbound.TraitMenuClick;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class BundleMenu<T extends BundleLikeTraits> extends TraitMenu<T> {
      int rows, columns, size;

      public BundleMenu(Minecraft minecraft, int screenLeft, int screenTop, int screenHeight, int screenWidth, Slot slot, ComponentHolder holder, T traits) {
            super(minecraft, screenLeft, screenTop, screenHeight, screenWidth, slot, holder, traits);
            updateSize();
            
            double offset = slot.y * 0.1 - 2;
            int drift = (int) ((rows - 1) * offset);
            topPos -= drift + 2;
      }
      
      protected boolean hasSpace() {
            return traits.fullness(holder).compareTo(Fraction.ONE) != 0;
      }

      private void updateSize() {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);

            this.size = stacks == null ? 0 : stacks.size();
            int sudoSize = size + (hasSpace() ? 1 : 0);

            int forRow = 1;
            int columns = Math.min(sudoSize, 4);
            int rows = 1;
            for (int i = columns; i <= sudoSize; i++) {
                  if (i > columns * rows) {
                        if (forRow == 2) {
                              columns++;
                              forRow = 0;
                        }
                        else {
                              rows++;
                              forRow++;
                        }
                  }
            }

            this.columns = columns;
            this.rows = rows;
      }

      @Override
      protected int getWidth() {
            return columns * 16 + 16;
      }

      @Override
      protected int getHeight() {
            return rows * 16 + 8;
      }
      
      private final class DragInstance {
            final double startX;
            final double startY;
            final int button;
            boolean hasMoved = false;
            double currentX;
            double currentY;
            
            private DragInstance(double startX, double startY, int button) {
                  this.startX = startX;
                  this.startY = startY;
                  this.button = button;
                  currentX = startX;
                  currentY = startY;
            }
            
            void move(double mouseX, double mouseY) {
                  double oX = mouseX - currentX;
                  double oY = mouseY - currentY;
                  
                  currentX += oX;
                  currentY += oY;
                  leftPos += oX;
                  topPos += oY;
            }
      }
      @Nullable private DragInstance dragInstance = null;
      
      @Override
      public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
            super.mouseDragged(mouseX, mouseY, button, dragX, dragY, cir);
            
            if (dragInstance != null) {
                  if (!dragInstance.hasMoved) {
                        dragInstance.hasMoved = true;
                  }
                  else {
                        dragInstance.move(mouseX, mouseY);
                  }
            }
            else if (hoveredSlot == null || hoveredSlot.isEmpty) {
                  LocalPlayer player = minecraft.player;
                  AbstractContainerMenu containerMenu = player.containerMenu;
                  if (containerMenu.getCarried().isEmpty()) {
                        dragInstance = new DragInstance(mouseX, mouseY, button);
                        cir.setReturnValue(true);
                  }
            }
      }
      
      @Override
      public void menuClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
            dragInstance = null;
            
            if (hoveredSlot != null) {
                  LocalPlayer player = minecraft.player;
                  AbstractContainerMenu containerMenu = player.containerMenu;
                  TraitMenuClick.Kind clickType = getClickType(button);

                  TraitMenuClick.send(containerMenu.containerId, slot, hoveredSlot.index, clickType);
                  SlotAccess access = SlotAccess.of(containerMenu::getCarried, containerMenu::setCarried);
                  traits.menuClick(holder, hoveredSlot.index, clickType, access, player);
                  cir.setReturnValue(true);
            }

            updateSize();
      }

      private TraitMenuClick.Kind getClickType(int button) {
            boolean eitherShiftDown = InputConstants.isKeyDown(minecraft.getWindow(), 340)
                        || InputConstants.isKeyDown(minecraft.getWindow(), 344);

            if (eitherShiftDown)
                  return TraitMenuClick.Kind.SHIFT;

            if (button == 1)
                  return TraitMenuClick.Kind.RIGHT;

            return TraitMenuClick.Kind.LEFT;
      }
      
      @Override
      public void dropHoveredItem(CallbackInfoReturnable<Boolean> cir) {
            if (hoveredSlot == null)
                  return;
            
            LocalPlayer player = minecraft.player;
            AbstractContainerMenu containerMenu = player.containerMenu;
            TraitMenuClick.Kind clickType = TraitMenuClick.Kind.DROP;
            
            TraitMenuClick.send(containerMenu.containerId, slot, hoveredSlot.index, clickType);
            SlotAccess access = SlotAccess.of(containerMenu::getCarried, containerMenu::setCarried);
            traits.menuClick(holder, hoveredSlot.index, clickType, access, player);
            updateSize();
      }

      @Override
      protected void menuRender(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            if (isFocused())
                  renderTooltipBackground(gui, 8, 4, getWidth() - 16, getHeight() - 8, holder.get(DataComponents.TOOLTIP_STYLE));
            else
                  TooltipRenderUtil.renderTooltipBackground(gui, 8, 4, getWidth() - 16, getHeight() - 8, holder.get(DataComponents.TOOLTIP_STYLE));
            
            renderItems(gui, mouseX, mouseY);
      }
      
      public static void renderTooltipBackground(GuiGraphics guiGraphics, int x, int y, int width, int height, @Nullable ResourceLocation sprite) {
            int i = x - 3 - 9;
            int j = y - 3 - 9;
            int k = width + 3 + 3 + 18;
            int l = height + 3 + 3 + 18;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getBackgroundSprite(sprite), i, j, k, l);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getFrameSprite(sprite), i, j, k, l);
      }
      
      private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/background");
      private static final ResourceLocation FRAME_SPRITE = ResourceLocation.withDefaultNamespace("tooltip/trait");
      
      private static ResourceLocation getBackgroundSprite(@Nullable ResourceLocation name) {
            return name == null ? BACKGROUND_SPRITE : name.withPath((p_371425_) -> {
                  return "tooltip/" + p_371425_ + "_background";
            });
      }
      
      private static ResourceLocation getFrameSprite(@Nullable ResourceLocation name) {
            return name == null ? FRAME_SPRITE : name.withPath((p_371467_) -> {
                  return "tooltip/" + p_371467_ + "_frame";
            });
      }
      
      @Nullable
      private tSlot hoveredSlot = null;

      private void renderItems(GuiGraphics gui, int mouseX, int mouseY) {
            updateSize();

            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            hoveredSlot = null;
            tSlot lastSlot = null;

            for (int y = 0; y < rows; y++) {
                  for (int x = 0; x < columns; x++) {
                        int i = y * columns + x;

                        int x1 = x * 16 + 8;
                        int y1 = y * 16 + 4;
                        int x2 = x1 + 16;
                        int y2 = y1 + 16;
                        
                        boolean isHovered = isFocused && mouseX >= x1 && mouseY >= y1 && mouseX < x2 && mouseY < y2;
                        
                        if (i == size) {
                              lastSlot = new tSlot(i, x1, x2, y1, y2, true);
                        }
                        
                        if (i >= size) {
                              if (isHovered)
                                    hoveredSlot = lastSlot;
                              continue;
                        }
                        
                        if (isHovered)
                              hoveredSlot = new tSlot(i, x1, x2, y1, y2, false);
                        
                        if (stacks == null)
                              break;
                        
                        Font font = minecraft.font;
                        ItemStack stack = stacks.get(i);
                        CommonClient.renderItem(minecraft, gui, stack, x1 + 8, y1 + 8);
                        CommonClient.renderItemDecorations(gui, font, stack, x1 + 8, y1 + 8);
                        
                        if (isHovered) {
                              List<Component> lines = Screen.getTooltipFromItem(this.minecraft, stack);
                              Optional<TooltipComponent> image = stack.getTooltipImage();
                              
                              List<ClientTooltipComponent> collected = lines.stream()
                                    .map(Component::getVisualOrderText)
                                    .map(ClientTooltipComponent::create)
                                    .toList();
                              
                              image.map(ClientTooltipComponent::create).ifPresent(component -> {
                                    if (collected.isEmpty()) 
                                          collected.addFirst(component);
                                    else 
                                          collected.add(1, component);
                              });
                              
                              int height = collected.stream().mapToInt(component -> component.getHeight(font)).sum();
                              gui.renderTooltip(font, collected, -4, -height + 10, DefaultTooltipPositioner.INSTANCE, holder.get(DataComponents.TOOLTIP_STYLE));
                        }
                  }
            }

            if (hoveredSlot != null) {
                  int x = hoveredSlot.x1;
                  int y = hoveredSlot.y1;
                  renderHighlight(gui, x, y);
            }
      }
      
      public static void renderHighlight(GuiGraphics gui, int x, int y) {
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, x - 4, y - 4, 24, 24);
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, x - 4, y - 4, 24, 24);
      }
      
      private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
      private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");

      record tSlot(int index, int x1, int x2, int y1, int y2, boolean isEmpty) {

      }
}
