package com.beansgalaxy.backpacks.client.predicates;

import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record IsUtility() implements ConditionalItemModelProperty {
      public static final MapCodec<IsUtility> MAP_CODEC = MapCodec.unit(IsUtility::new);
      
      @Override
      public MapCodec<? extends ConditionalItemModelProperty> type() {
            return MAP_CODEC;
      }
      
      @Override public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int i, ItemDisplayContext context) {
            if (entity == null)
                  return false;
            
            ItemStack backpack = entity.getItemBySlot(EquipmentSlot.BODY);
            if (backpack.isEmpty()) {
                  return false;
            }
            
            Optional<UtilityComponent.Mutable> optional = UtilityComponent.get(backpack);
            if (optional.isEmpty())
                  return false;
            
            UtilityComponent.Mutable mutable = optional.get();
            return mutable.slots.containsValue(stack);
      }
}
