package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.bundle.BundleTooltip;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class QuiverClient extends BundleClient {
      static final QuiverClient INSTANCE = new QuiverClient();

      @Override
      public int getBarColor(BundleLikeTraits trait, PatchedComponentHolder holder) {
            return BAR_COLOR;
      }

      @Override
      public void renderItemDecorations(BundleLikeTraits trait, PatchedComponentHolder holder, GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks != null && !stacks.isEmpty()) {
                  Minecraft minecraft = Minecraft.getInstance();
                  SlotSelection slotSelection = holder.get(ITraitData.SLOT_SELECTION);

                  int i;
                  if (slotSelection != null) {
                        i = slotSelection.getSelectedSlotSafe(minecraft.player);
                  }
                  else i = 0;

                  ItemStack arrow = stacks.get(i);
                  BundleTooltip.renderItem(minecraft, gui, arrow, x + 8, y + 8, 200, false);
            }
            super.renderItemDecorations(trait, holder, gui, font, stack, x, y);
      }
}
