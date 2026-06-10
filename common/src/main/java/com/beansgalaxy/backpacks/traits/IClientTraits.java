package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IClientTraits<T extends GenericTraits> {
      int BAR_COLOR = 16755200;
      int BLUE_BAR = Tint.fastColor(.4, .4, .0);
      int RED_BAR = Tint.fastColor(.9, .2, .3);

      void appendTooltipLines(T traits, Consumer<Component> lines);

      default boolean isBarVisible(T trait, ComponentHolder holder) {
            return !trait.isEmpty(holder);
      }

      int getBarWidth(T trait, ComponentHolder holder);

      int getBarColor(T trait, ComponentHolder holder);

      default void renderItemDecorations(T trait, ComponentHolder holder, GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
            if (isBarVisible(trait, holder)) {
                  int i = x + 2;
                  int j = y + 13;
                  
                  if (stack.isBarVisible())
                        j += 1;
                  
                  gui.fill(RenderPipelines.GUI, i, j, i + 13, j + 2, -16777216);
                  gui.fill(RenderPipelines.GUI, i, j, i + getBarWidth(trait, holder), j + 1, Tint.fastColor(255, stack.getBarColor()));
            }
      }
      
      @Nullable
      TraitMenu<T> createTooltip(Minecraft minecraft, int leftPos, int topPos, int screenHeight, int screenWidth, Slot slot, ComponentHolder holder, T traits);
}
