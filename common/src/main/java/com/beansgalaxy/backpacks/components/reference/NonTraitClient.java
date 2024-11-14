package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Consumer;

public class NonTraitClient implements IClientTraits<NonTrait> {
      static final NonTraitClient INSTANCE = new NonTraitClient();

      @Override
      public void renderTooltip(NonTrait trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

      }

      @Override
      public void isBarVisible(NonTrait trait, PatchedComponentHolder holder, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void getBarWidth(NonTrait trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {

      }

      @Override
      public void getBarColor(NonTrait trait, PatchedComponentHolder holder, CallbackInfoReturnable<Integer> cir) {

      }


      @Override
      public void appendEquipmentLines(NonTrait traits, Consumer<Component> pTooltipAdder) {
      }

      @Override
      public @Nullable ClientTooltipComponent getTooltipComponent(NonTrait traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            return null;
      }

      @Override
      public void appendTooltipLines(NonTrait traits, List<Component> lines) {
      }
}
