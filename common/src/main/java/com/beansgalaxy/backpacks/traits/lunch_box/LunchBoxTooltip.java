package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LunchBoxTooltip extends BundleTooltip {
      private final boolean hasNonEdibles;
      private final int edibleSize;
      private final boolean selectNonEdible;

      public LunchBoxTooltip(BundleLikeTraits traits, ItemStack itemStack, List<ItemStack> stacks, List<ItemStack> nonEdibles, ComponentHolder holder, Component title) {
            super(traits, itemStack, concatList(stacks, nonEdibles), holder, title);
            this.edibleSize = stacks.size();
            this.hasNonEdibles = nonEdibles != null;
            this.selectNonEdible = hasNonEdibles && carriedEmpty;
      }

      private static ArrayList<ItemStack> concatList(List<ItemStack> stacks, List<ItemStack> nonEdibles) {
            ArrayList<ItemStack> arrayList = Lists.newArrayList(stacks);
            if (nonEdibles != null)
                  arrayList.addAll(nonEdibles);

            return arrayList;
      }

      @Override
      protected void drawSlot(Font font, GuiGraphics gui, Iterator<ItemStack> stacks, int x, int y, int index) {
            if (stacks.hasNext()) {
                  ItemStack stack = stacks.next();
                  renderItem(minecraft, gui, stack, x, y, 300, false);
                  renderItemDecorations(gui, font, stack, x, y, 300);
            }

            if (index == selectedSlot) {
                  int fillX = x - 9;
                  int fillY = y - 9;
                  gui.fill(fillX + 1, fillY + 1, fillX + 17, fillY + 17, selectNonEdible ? 200 : 500, selectNonEdible ? 0x40606060 : 0x60FFFFFF);
            }

            if (hasNonEdibles && index == edibleSize) {
                  int fillX = x - 9;
                  int fillY = y - 9;
                  gui.fill(fillX + 1, fillY + 1, fillX + 17, fillY + 17, !selectNonEdible ? 200 : 500, !selectNonEdible ? 0x40606060 : 0x60FFFFFF);
            }
      }
}
