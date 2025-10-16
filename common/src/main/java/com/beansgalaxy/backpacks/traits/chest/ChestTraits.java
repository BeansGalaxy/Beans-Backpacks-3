package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableChestLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;

public class ChestTraits extends ChestLikeTraits {
      public static final String NAME = "chest";
      
      public ChestTraits(ModSound sound, int size) {
            super(sound, size);
      }
      
      @Override public String name() {
            return NAME;
      }
      
      @Override public ChestClient client() {
            return ChestClient.INSTANCE;
      }
      
      @Override public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.CHEST;
      }
      
      @Override public MutableChestLike<?> mutable(ComponentHolder holder) {
            return new MutableChestLike<ChestLikeTraits>(this, holder);
      }
}
