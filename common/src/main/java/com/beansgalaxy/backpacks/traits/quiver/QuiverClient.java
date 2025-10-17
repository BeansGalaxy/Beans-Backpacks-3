package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.chest.ChestClient;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class QuiverClient extends ChestClient {
      static final QuiverClient INSTANCE = new QuiverClient();

      @Override
      public boolean isBarVisible(ChestLikeTraits trait, ComponentHolder holder) {
            return false;
      }

      @Override
      public void renderItemDecorations(ChestLikeTraits trait, ComponentHolder holder, GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks != null && !stacks.isEmpty()) {
                  Minecraft minecraft = Minecraft.getInstance();
                  SlotSelection slotSelection = holder.get(ITraitData.SLOT_SELECTION);

                  int i;
                  if (slotSelection != null) {
                        i = slotSelection.get(minecraft.player);
                  }
                  else i = 0;

                  ItemStack arrow = stacks.get(i);
                  CommonClient.renderItem(minecraft, gui, arrow, x + 8, y + 8, 150, false);
                  CommonClient.renderItemDecorations(gui, font, arrow, x + 8, y + 8, 150);
            }
            super.renderItemDecorations(trait, holder, gui, font, stack, x, y);
      }
}
