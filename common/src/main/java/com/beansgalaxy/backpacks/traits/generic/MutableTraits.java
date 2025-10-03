package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.util.ModSound;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface MutableTraits {

      void push();

      default void push(CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(true);
            push();
      }

      ModSound sound();

      Fraction fullness();

      default boolean isEmpty() {
            int i = fullness().compareTo(Fraction.ZERO);
            return 0 >= i;
      }

      default boolean isFull() {
            int i = fullness().compareTo(Fraction.ONE);
            return i >= 0;
      }
}
