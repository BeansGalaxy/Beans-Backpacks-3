package com.beansgalaxy.backpacks.mixin.client.model_properties;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.predicates.IsUtility;
import com.beansgalaxy.backpacks.client.predicates.Searching;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ConditionalItemModelProperties.class)
public class ConditionalItemMixin {
      @Shadow @Final private static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ConditionalItemModelProperty>> ID_MAPPER;
      
      @Inject(method="bootstrap", at=@At("TAIL"))
      private static void appendItemPredicates(CallbackInfo ci) {
            ID_MAPPER.put(Constants.defaultLocation("is_utility"), IsUtility.MAP_CODEC);
            ID_MAPPER.put(Constants.defaultLocation("searching"), Searching.MAP_CODEC);
      }
}
