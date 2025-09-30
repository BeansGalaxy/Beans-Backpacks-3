package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public class AlchemyClient extends BundleClient {
      static final AlchemyClient INSTANCE = new AlchemyClient();

      @Override
      public int getBarColor(BundleLikeTraits trait, ComponentHolder holder) {
            return BAR_COLOR;
      }

      @Override
      public void renderItemInHand(ItemRenderer itemRenderer, BundleLikeTraits traits, LivingEntity entity, ComponentHolder holder, ItemDisplayContext context, boolean hand, PoseStack pose, MultiBufferSource buffer, int seed, CallbackInfo ci) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks != null && !stacks.isEmpty()) {
                  Minecraft minecraft = Minecraft.getInstance();
                  SlotSelection slotSelection = holder.get(ITraitData.SLOT_SELECTION);

                  int i;
                  if (slotSelection != null) {
                        i = slotSelection.getSelectedSlot(minecraft.player);
                  } else i = 0;

                  ItemStack food = stacks.get(i);

                  ci.cancel();
                  itemRenderer.renderStatic(entity, food, context, hand, pose, buffer, entity.level(), seed, OverlayTexture.NO_OVERLAY, entity.getId() + context.ordinal());
            }
      }
}
