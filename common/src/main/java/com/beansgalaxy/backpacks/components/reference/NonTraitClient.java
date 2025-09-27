package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

public class NonTraitClient implements IClientTraits<NonTrait> {
      static final NonTraitClient INSTANCE = new NonTraitClient();

      @Override
      public void renderTooltip(NonTrait trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {

      }

      @Override
      public boolean isBarVisible(NonTrait trait, PatchedComponentHolder holder) {
            return false;
      }

      @Override
      public int getBarColor(NonTrait trait, PatchedComponentHolder holder) {
            return 0;
      }

      @Override @Nullable
      public TraitMenu<NonTrait> createTooltip(Minecraft minecraft, int leftPos, int topPos, @Nullable Slot slot, PatchedComponentHolder holder, NonTrait traits) {
            return null;
      }

      @Override
      public int getBarWidth(NonTrait trait, PatchedComponentHolder holder) {
            return 0;
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
