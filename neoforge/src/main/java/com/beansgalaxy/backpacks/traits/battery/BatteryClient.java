package com.beansgalaxy.backpacks.traits.battery;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.common.BatteryTooltip;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BatteryClient implements IClientTraits<BatteryTraits> {
      static final BatteryClient INSTANCE = new BatteryClient();

      @Override
      public boolean isBarVisible(BatteryTraits trait, PatchedComponentHolder holder) {
            return holder.has(ITraitData.SOLO_STACK) || holder.has(ITraitData.LONG);
      }

      @Override
      public void renderTooltip(BatteryTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  Long amount = holder.getOrDefault(ITraitData.LONG, 0L);
                  MutableComponent energy = Component.literal(energyToReadable(amount) + "/" + energyToReadable(trait.size()) + " E");
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, energy);
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(energy), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      private static String energyToReadable(long energy) {
            String s = String.valueOf(energy);
            int length = s.length();
            if (length < 3)
                  return s;

            char[] chars = s.toCharArray();
            if (length > 12)
                  return chars[0] + "." + chars[1] + "*10^" + length;

            StringBuilder builder = new StringBuilder();
            int i = 0;
            while (i < length % 3) {
                  builder.append(chars[i]);
                  i++;
            }

            builder.append('.').append(chars[i]);

            if (length > 6) {
                  builder.append(chars[i + 1]);
                  if (length > 9) {
                        builder.append('b');
                  }
                  else {
                        builder.append('m');
                  }
            } else {
                  builder.append('k');
            }

            return builder.toString();
      }

      @Override
      public int getBarWidth(BatteryTraits trait, PatchedComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (fullness.equals(Fraction.ONE))
                  return 14;
            else if (fullness.getNumerator() == 0) {
                  return 0;
            } else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  return Mth.floor(value) + 1;
            }
      }

      @Override
      public int getBarColor(BatteryTraits trait, PatchedComponentHolder holder) {
            if (!holder.has(ITraitData.SOLO_STACK))
                  return BAR_COLOR;
            else
                  return RED_BAR;//Mth.color(0.9F, 1F, 0.3F);
      }

      @Override
      public @Nullable ClientTooltipComponent getTooltipComponent(BatteryTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            return new BatteryTooltip(itemStack, holder, title);
      }

      @Override
      public void appendEquipmentLines(BatteryTraits traits, Consumer<Component> pTooltipAdder) {
            long size = traits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(BatteryTraits traits, List<Component> lines) {
            long size = traits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
