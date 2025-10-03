package com.beansgalaxy.backpacks.traits.backpack;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.bundle.BundleMenu;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import org.apache.commons.lang3.math.Fraction;

import java.util.function.Consumer;

public class BackpackClient implements IClientTraits<BackpackTraits> {
      public static final BackpackClient INSTANCE = new BackpackClient();

      @Override
      public int getBarWidth(BackpackTraits trait, ComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isFull(holder))
                  return (14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  return (Mth.floor(value) + 1);
            }
      }

      @Override
      public int getBarColor(BackpackTraits trait, ComponentHolder holder) {
            if (trait.isFull(holder))
                  return RED_BAR;
            else
                  return BAR_COLOR;
      }

      @Override
      public TraitMenu<BackpackTraits> createTooltip(Minecraft minecraft, int leftPos, int topPos, Slot slot, ComponentHolder holder, BackpackTraits traits) {
            if (slot instanceof EquipmentSlotAccess access && traits.slots().test(access.getSlot())) {
                  return new BundleMenu<>(minecraft, leftPos, topPos, slot, holder, traits);
            }

            return null;
      }

      @Override
      public void appendTooltipLines(BackpackTraits traits, Consumer<Component> lines) {
            int size = traits.size();
            lines.accept(Component.translatable("traits.beansbackpacks.tooltip." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
