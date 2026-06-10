package com.beansgalaxy.backpacks.mixin.client.model_properties;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.predicates.Fullness;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangeSelectItemModelProperties.class)
public class  RangeSelectMixin {
      
      @Shadow @Final private static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends RangeSelectItemModelProperty>> ID_MAPPER;
      
      @Inject(method="bootstrap", at=@At("TAIL"))
      private static void appendItemPredicates(CallbackInfo ci) {
            ID_MAPPER.put(Constants.defaultLocation("fullness"), Fullness.MAP_CODEC);
      }
}
