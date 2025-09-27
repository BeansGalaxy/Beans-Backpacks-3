package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

public class LunchBoxClient extends BundleClient {
      static final LunchBoxClient INSTANCE = new LunchBoxClient();

      @Override
      public void renderTooltip(BundleLikeTraits trait, ItemStack itemStack, ComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  Minecraft minecraft = Minecraft.getInstance();

                  LocalPlayer player = minecraft.player;
                  boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                  boolean isQuickMove = BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
                  boolean noSpace = trait.fullness(holder).compareTo(Fraction.ONE) == 0;
                  boolean hideEmptySlot = carriedEmpty || isQuickMove || noSpace;

                  Component title;
                  int selectedSlot = trait.getSelectedSlot(holder, player);
                  if (selectedSlot == 0 && !hideEmptySlot) {
                        title = Component.empty();
                  } else {
                        ItemStack selected;
                        List<ItemStack> nonEdibles = holder.get(ITraitData.NON_EDIBLES);
                        if (nonEdibles != null && carriedEmpty) {
                              selected = nonEdibles.getFirst();
                        }
                        else {
                              int selectedSlotSafe = selectedSlot == 0 ? 0 : selectedSlot - 1;
                              List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                              selected = stacks.get(selectedSlotSafe);
                        }

                        title = Constants.getName(selected);
                  }

                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, title);
                  gui.renderTooltip(minecraft.font, List.of(title), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override @Nullable
      public ClientTooltipComponent getTooltipComponent(BundleLikeTraits traits, ItemStack itemStack, ComponentHolder holder, Component title) {
            List<ItemStack> stacks = holder.getOrDefault(ITraitData.ITEM_STACKS, List.of());
            List<ItemStack> nonEdibles = holder.get(ITraitData.NON_EDIBLES);
            return new LunchBoxTooltip(traits, itemStack, stacks, nonEdibles, holder, title);
      }

      @Override
      public int getBarWidth(BundleLikeTraits trait, ComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isEmpty(holder))
                  return (0);
            else if (fullness.equals(Fraction.ONE))
                  return (13);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(12, 1)).floatValue();
                  return (Mth.floor(value) + 1);
            }
      }

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
                        i = slotSelection.getSelectedSlotSafe(minecraft.player);
                  } else i = 0;

                  ItemStack food = stacks.get(i);

                  ci.cancel();
                  itemRenderer.renderStatic(entity, food, context, hand, pose, buffer, entity.level(), seed, OverlayTexture.NO_OVERLAY, entity.getId() + context.ordinal());
            }
      }
}
