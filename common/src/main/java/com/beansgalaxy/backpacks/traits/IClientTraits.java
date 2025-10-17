package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

public interface IClientTraits<T extends GenericTraits> {
      int BAR_COLOR = 16755200;
      int BLUE_BAR = Mth.color(0.4F, 0.4F, 1.0F);
      int RED_BAR = Mth.color(0.9F, 0.2F, 0.3F);

      void appendTooltipLines(T traits, Consumer<Component> lines);

      default boolean isBarVisible(T trait, ComponentHolder holder) {
            return !trait.isEmpty(holder);
      }

      int getBarWidth(T trait, ComponentHolder holder);

      int getBarColor(T trait, ComponentHolder holder);

      default void renderItemDecorations(T trait, ComponentHolder holder, GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
            if (isBarVisible(trait, holder)) {
                  int l = getBarWidth(trait, holder);
                  int i = getBarColor(trait, holder);
                  int i1 = x + 2;
                  int j1 = y + 13;

                  if (stack.isBarVisible()) {
                        j1 += 1;
                  }

                  gui.fill(RenderType.guiOverlay(), i1, j1, i1 + 13, j1 + 2, -16777216);
                  gui.fill(RenderType.guiOverlay(), i1, j1, i1 + l, j1 + 1, i | -16777216);
            }
      }

      default void renderItemInHand(ItemRenderer itemRenderer, T traits, LivingEntity entity, ComponentHolder holder, ItemDisplayContext context, boolean hand, PoseStack stack1, MultiBufferSource buffer, int seed, CallbackInfo ci) {

      }

      @Nullable
      TraitMenu<T> createTooltip(Minecraft minecraft, int leftPos, int topPos, int screenHeight, int screenWidth, Slot slot, ComponentHolder holder, T traits);
}
