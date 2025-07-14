package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {

      @Shadow @Final private Minecraft minecraft;

      @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
                  at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"))
      private void backpacks_renderItemDecorations(Font pFont, ItemStack pStack, int pX, int pY, String pText, CallbackInfo ci) {
            Optional<GenericTraits> traitsOptional = Traits.get(pStack);
            if (traitsOptional.isPresent()) {
                  GenericTraits traits = traitsOptional.get();
                  traits.client().renderItemDecorations(traits, PatchedComponentHolder.of(pStack), (GuiGraphics)(Object) this, pFont, pStack, pX, pY);
                  return;
            }

            Optional<EnderTraits> optionalEnder = EnderTraits.get(pStack);
            if (optionalEnder.isPresent()) {
                  EnderTraits enderTraits = optionalEnder.get();
                  GenericTraits traits = enderTraits.getTrait(minecraft.level);
                  traits.client().renderItemDecorations(traits, enderTraits, (GuiGraphics)(Object) this, pFont, pStack, pX, pY);
                  return;
            }
      }
}
