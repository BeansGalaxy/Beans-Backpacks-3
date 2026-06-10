package com.beansgalaxy.backpacks.client.predicates;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record Searching() implements ConditionalItemModelProperty {
      public static final MapCodec<Searching> MAP_CODEC = MapCodec.unit(Searching::new);
      
      @Override
      public MapCodec<? extends ConditionalItemModelProperty> type() {
            return MAP_CODEC;
      }
      
      @Override public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i, ItemDisplayContext context) {
            EnderTraits enderTraits = stack.get(Traits.ENDER);
            return enderTraits == null || !enderTraits.isLoaded();
      }
}
