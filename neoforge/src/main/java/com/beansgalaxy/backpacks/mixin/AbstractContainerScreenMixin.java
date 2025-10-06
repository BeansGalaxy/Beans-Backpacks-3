package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.items.ModItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen {

      @Shadow protected abstract boolean isHovering(Slot slot, double mouseX, double mouseY);

      protected AbstractContainerScreenMixin(Component pTitle) {
            super(pTitle);
      }

      @WrapOperation(method = "renderSlotContents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItem(Lnet/minecraft/world/item/ItemStack;III)V"))
      private void renderHoveredSlotItem(GuiGraphics instance, ItemStack stack, int x, int y, int seed, Operation<Void> original, @Local(ordinal = 0, argsOnly = true) Slot slot) {
            int mouseX = (int) (
                        this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth()
            );
            int mouseY = (int) (
                        this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight()
            );

            if (isHovering(slot, mouseX, mouseY)) {
                  if (stack.is(ModItems.LUNCH_BOX.get())) {
                        CommonClient.renderHoveredItem(minecraft, instance, stack, x, y, seed, original, "lunch_box_open");
                        return;
                  }
                  if (stack.is(ModItems.NETHERITE_LUNCH_BOX.get())) {
                        CommonClient.renderHoveredItem(minecraft, instance, stack, x, y, seed, original, "netherite_lunch_box_open");
                        return;
                  }
                  if (stack.is(ModItems.ALCHEMIST_BAG.get())) {
                        CommonClient.renderHoveredItem(minecraft, instance, stack, x, y, seed, original, "alchemy_bag_open");
                        return;
                  }
            }

            original.call(instance, stack, x, y, seed);
      }

}