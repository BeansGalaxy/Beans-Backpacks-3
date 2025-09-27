package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.container.BackSlot;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public abstract class TraitMenu<T extends GenericTraits> {
      protected final Minecraft minecraft;
      public final Slot slot;
      public final PatchedComponentHolder holder;
      public final T traits;

      protected final int slotX;
      protected final int slotY;

      protected int leftPos;
      protected int topPos;

      public TraitMenu(Minecraft minecraft, int screenLeft, int screenTop, Slot slot, PatchedComponentHolder holder, T traits) {
            this(minecraft, slot, holder, traits, screenLeft + slot.x, screenTop + slot.y);
      }

      public TraitMenu(Minecraft minecraft, Slot slot, PatchedComponentHolder holder, T traits, int slotX, int slotY) {
            this.minecraft = minecraft;
            this.slot = slot;
            this.holder = holder;
            this.traits = traits;

            this.slotX = slotX;
            this.slotY = slotY;
            this.leftPos = slotX + 14;
            this.topPos = slotY - 2;
      }

      @Nullable
      public static TraitMenu<?> create(Minecraft minecraft, int leftPos, int topPos, @Nullable Slot slot) {
            if (slot == null)
                  return null;

            ItemStack stack = slot.getItem();
            Optional<GenericTraits> optional = Traits.get(stack);

            GenericTraits traits;
            PatchedComponentHolder holder;

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
                  holder = PatchedComponentHolder.of(slot);
            }

            IClientTraits<GenericTraits> client = traits.client();
            return client.createTooltip(minecraft, leftPos, topPos, slot, holder, traits);
      }

      public static final ResourceLocation TEXTURE =
                  ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/exit_trait_menu.png");

      public void render(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            if (isHoveringSlot(mouseX, mouseY)) {
                  int x = slot.x;
                  int y = slot.y;

                  RenderSystem.enableBlend();
                  ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "exit_trait_menu");
                  gui.blitSprite(location, 16, 16, 0, 0, x, y, 310, 16, 16);
                  RenderSystem.disableBlend();
            }

            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(slot.x - slotX + leftPos, slot.y - slotY + topPos, 301);
            menuRender(screen, gui, mouseX - leftPos, mouseY - topPos);
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
            menuClicked(mouseX - leftPos, mouseY - topPos, button, cir);
      }

      // ==============================================================================================================  MENU CLASSES

      protected void menuRender(AbstractContainerScreen<?> screen, GuiGraphics gui, int mouseX, int mouseY) {
            String name = traits.getClass().getSimpleName();
            MutableComponent component = Component.literal(name);
            gui.renderComponentTooltip(minecraft.font, List.of(component), 0, getHeight());
      }

      public void menuClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {

      }

      private boolean isHoveringMenu(int mouseX, int mouseY) {
            mouseX -= leftPos;
            mouseY -= topPos;

            if (mouseX < 0 || mouseY < 0) {
                  return false;
            }

            return getWidth() > mouseX && getHeight() > mouseY;
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

      public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {

      }
}
