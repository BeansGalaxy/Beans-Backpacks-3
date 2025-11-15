package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.bundle.BundleMenu;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public abstract class TraitMenu<T extends GenericTraits> {
      protected final Minecraft minecraft;
      public final Slot slot;
      public final ComponentHolder holder;
      public final T traits;

      protected final int slotX;
      protected final int slotY;
      public final long timeOpened;
      private final int screenWidth;
      private final int screenHeight;
      
      protected double leftPos;
      protected double topPos;

      public TraitMenu(Minecraft minecraft, int screenLeft, int screenTop, int screenHeight, int screenWidth, Slot slot, ComponentHolder holder, T traits) {
            this(minecraft, slot, holder, traits, screenLeft + slot.x, screenTop + slot.y, screenHeight, screenWidth);
      }

      public TraitMenu(Minecraft minecraft, Slot slot, ComponentHolder holder, T traits, int slotX, int slotY, int screenHeight, int screenWidth) {
            this.minecraft = minecraft;
            this.slot = slot;
            this.holder = holder;
            this.traits = traits;

            this.slotX = slotX;
            this.slotY = slotY;
            this.leftPos = slotX + 14;
            this.topPos = slotY - 2;
            
            this.screenHeight = screenHeight;
            this.screenWidth = screenWidth;

            this.timeOpened = Util.getMillis();
      }
      
      int topPos() {
            return (int) topPos;
      }
      
      int leftPos() {
            return (int) leftPos;
      }

      @Nullable
      public static TraitMenu<?> create(Minecraft minecraft, int leftPos, int topPos, int imageHeight, int imageWidth, Slot slot) {
            if (slot == null)
                  return null;
            
            ItemStack stack = slot.getItem();
            Optional<GenericTraits> optional = Traits.get(stack);
            
            GenericTraits traits;
            ComponentHolder holder;
            
            if (optional.isEmpty()) {
                  Optional<EnderTraits> oEnderTraits = EnderTraits.get(stack);
                  if (oEnderTraits.isEmpty())
                        return null;
                  
                  EnderTraits enderTraits = oEnderTraits.get();
                  traits = enderTraits.getTrait(Minecraft.getInstance().level);
                  holder = enderTraits;
            }
            else {
                  traits = optional.get();
                  holder = ComponentHolder.of(slot);
            }
            
            IClientTraits<GenericTraits> client = traits.client();
            
            return client.createTooltip(minecraft, leftPos, topPos, imageHeight, imageWidth, slot, holder, traits);
      }

      public void render(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            PoseStack pose = gui.pose();
            pose.translate(0, 0, 300);
            int y = slot.y;
            int x = slot.x;
            
            if (isFocused) {
                  ItemStack stack = slot.getItem();
                  BundleMenu.renderTooltipBackground(gui, x + 1, y + 1, 14, 14, 0);
                  CommonClient.renderItem(minecraft, gui, stack, x + 8, y + 8, 50, false);
                  CommonClient.renderItemDecorations(gui, minecraft.font, stack, x + 8, y + 8, 50);
                  pose.translate(0, 0, 300);
            }
            
            pose.pushPose();
            pose.translate(slot.x - slotX + leftPos(), slot.y - slotY + topPos(), 0);
            menuRender(screen, gui, mouseX - leftPos(), mouseY - topPos());
            pose.popPose();
      }

      public boolean isHovering(int mouseX, int mouseY) {
            return isHoveringSlot(mouseX, mouseY) || isHoveringMenu(mouseX, mouseY);
      }

      public boolean isHoveringSlot(int mouseX, int mouseY) {
            if (mouseX < slotX - 1)
                  return false;
            
            return mouseY >= slotY - 1 && mouseY <= slotY + 16 && mouseX <= slotX + 18; // IS HOVERING SLOT
      }

      public void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
            menuClicked(mouseX - leftPos(), mouseY - topPos(), button, cir);
      }

      // ==============================================================================================================  MENU CLASSES
      
      protected boolean isFocused = false;
      
      public void setFocus(boolean isFocused) {
            this.isFocused = isFocused;
      }
      
      protected void menuRender(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            String name = traits.getClass().getSimpleName();
            MutableComponent component = Component.literal(name);
            gui.renderComponentTooltip(minecraft.font, List.of(component), 0, getHeight());
      }

      public void menuClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {

      }

      private boolean isHoveringMenu(int mouseX, int mouseY) {
            mouseX -= leftPos();
            mouseY -= topPos();

            if (mouseX < 0 || mouseY < 0) {
                  return false;
            }

            return getWidth() - 4 > mouseX && getHeight() > mouseY;
      }

      protected int getWidth() {
            String name = traits.getClass().getSimpleName();
            MutableComponent component = Component.literal(name);
            int width = minecraft.font.width(component);
            return width + 16;
      }

      protected int getHeight() {
            return 16;
      }
      
      public void dropHoveredItem(CallbackInfoReturnable<Boolean> cir) {
      }
      
      public boolean isFocused() {
            return isFocused;
      }
      
      public void mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
      
      }
}
