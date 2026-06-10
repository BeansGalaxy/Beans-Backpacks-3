package com.beansgalaxy.backpacks.client.predicates;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.util.Tint;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

public record BackpackColor(int defaultColor, boolean highlight) implements ItemTintSource {
      public static final MapCodec<BackpackColor> MAP_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(BackpackColor::defaultColor),
            Codec.BOOL.optionalFieldOf("highlight", false).forGetter(BackpackColor::highlight)
      ).apply(in, BackpackColor::new));
      
      public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return highlight
                  ? componentHighlight(stack, defaultColor)
                  : componentTint(stack, defaultColor);
      }
      
      public MapCodec<BackpackColor> type() {
            return MAP_CODEC;
      }
      
      public int defaultColor() {
            return this.defaultColor;
      }
      
      
      private static int componentTint(ItemStack itemStack, int rgbBase) {
            DyedItemColor itemColor = itemStack.get(DataComponents.DYED_COLOR);
            if (itemColor != null) {
                  int rgbTint = itemColor.rgb();
                  Tint tint = new Tint(rgbTint);
                  Tint.HSL tintHsl = tint.HSL();
                  tintHsl.modLum(l -> (Math.sqrt(l) + l) / 2);
                  return tintHsl.rgb();
            }
            return Tint.fastColor(255, rgbBase);
      }
      
      private static int componentHighlight(ItemStack itemStack, int rgbBase) {
            DyedItemColor itemColor = itemStack.get(DataComponents.DYED_COLOR);
            
            int rgb = itemColor == null ? rgbBase : itemColor.rgb();
            
            Tint tint = new Tint(rgb);
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.rotate(10);
            hsl.setLum((Math.pow(brightness, 4) + lum + 2.3 + (tint.getBlue() / 160.0)) / 5);
            double sat = hsl.getSat();
            hsl.setSat((1 - brightness + sat) / 2);
            return hsl.rgb();
      }
      
}
