package com.beansgalaxy.backpacks.client.predicates;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record Fullness() implements RangeSelectItemModelProperty {
      
      public static final MapCodec<Fullness> MAP_CODEC = MapCodec.unit(Fullness::new);
      
      @Override
      public MapCodec<? extends RangeSelectItemModelProperty> type() {
            return MAP_CODEC;
      }
      
      @Override
      public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int i) {
            Optional<GenericTraits> optional = Traits.get(stack);
            GenericTraits traits;
            ComponentHolder holder;
            if (optional.isPresent()) {
                  traits = optional.get();
                  holder = ComponentHolder.of(stack);
            }
            else {
                  EnderTraits enderTraits = stack.get(Traits.ENDER);
                  if (enderTraits == null)
                        return 0f;
                  
                  traits = enderTraits.getTrait(level);
                  holder = enderTraits;
            }
            
            if (traits.isFull(holder))
                  return 1f;
            
            Fraction fullness = traits.fullness(holder);
            if (traits.isEmpty(holder) || fullness.equals(Fraction.ZERO))
                  return 0f;
            
            float v = fullness.floatValue();
            return v * 0.89f + 0.1f;
      }
}
