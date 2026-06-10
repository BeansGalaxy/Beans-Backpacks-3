package com.beansgalaxy.backpacks.mixin.client.model_properties;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.client.predicates.BackpackColor;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemTintSources.class)
public class ItemTintSourcesMixin {
      @Shadow @Final private static ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemTintSource>> ID_MAPPER;
      
      @Inject(method="bootstrap", at=@At("TAIL"))
      private static void bootstrap(CallbackInfo ci) {
            ID_MAPPER.put(Constants.defaultLocation("dye"), BackpackColor.MAP_CODEC);
      }
}
