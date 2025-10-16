package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.math.Fraction;

import java.util.function.Consumer;

public class ChestClient implements IClientTraits<ChestLikeTraits> {
      public static final ChestClient INSTANCE = new ChestClient();
      
      @Override
      public int getBarWidth(ChestLikeTraits trait, ComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isFull(holder))
                  return (14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(12, 1)).floatValue();
                  return (Mth.floor(value) + 1);
            }
      }
      
      @Override
      public int getBarColor(ChestLikeTraits trait, ComponentHolder holder) {
            if (trait.isFull(holder))
                  return RED_BAR;
            else
                  return BAR_COLOR;
      }
      
      @Override
      public TraitMenu<ChestLikeTraits> createTooltip(Minecraft minecraft, int leftPos, int topPos, Slot slot, ComponentHolder holder, ChestLikeTraits traits) {
            return new ChestMenu<>(minecraft, leftPos, topPos, slot, holder, traits);
      }
      
      @Override
      public void appendTooltipLines(ChestLikeTraits traits, Consumer<Component> lines) {
            int size = traits.size();
            lines.accept(Component.translatable("traits.beansbackpacks.tooltip." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
