package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.chest.ChestClient;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.math.Fraction;

public class AlchemyClient extends ChestClient {
      static final AlchemyClient INSTANCE = new AlchemyClient();
      
      @Override
      public int getBarWidth(ChestLikeTraits trait, ComponentHolder holder) {
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
      public int getBarColor(ChestLikeTraits trait, ComponentHolder holder) {
            return BAR_COLOR;
      }
      
}
