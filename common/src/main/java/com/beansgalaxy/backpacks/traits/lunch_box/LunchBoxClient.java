package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.traits.bundle.BundleClient;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.math.Fraction;

public class LunchBoxClient extends BundleClient {
      static final LunchBoxClient INSTANCE = new LunchBoxClient();

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
      
}
