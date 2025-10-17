package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.math.Fraction;

import java.util.function.Consumer;

public class BundleClient implements IClientTraits<BundleLikeTraits> {
      static final BundleClient INSTANCE = new BundleClient();

      @Override
      public int getBarWidth(BundleLikeTraits trait, ComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isFull(holder))
                  return (14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  return (Mth.floor(value) + 1);
            }
      }

      @Override
      public int getBarColor(BundleLikeTraits trait, ComponentHolder holder) {
            if (trait.isFull(holder))
                  return RED_BAR;
            else
                  return BAR_COLOR;
      }

      @Override
      public TraitMenu<BundleLikeTraits> createTooltip(Minecraft minecraft, int leftPos, int topPos, int screenHeight, int screenWidth, Slot slot, ComponentHolder holder, BundleLikeTraits traits) {
            return new BundleMenu<>(minecraft, leftPos, topPos, screenHeight, screenWidth, slot, holder, traits);
      }

      @Override
      public void appendTooltipLines(BundleLikeTraits traits, Consumer<Component> lines) {
            int size = traits.size();
            lines.accept(Component.translatable("traits.beansbackpacks.tooltip." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
