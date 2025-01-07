package com.beansgalaxy.backpacks.traits.experience;

import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class XpClient implements IClientTraits<XpTraits> {
      static final XpClient INSTANCE = new XpClient();

      @Override
      public void renderTooltip(XpTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, Component.empty());
                  gui.renderTooltip(Minecraft.getInstance().font, List.of(Component.empty()), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public boolean isBarVisible(XpTraits trait, PatchedComponentHolder holder) {
            return false;
      }

      @Override
      public int getBarColor(XpTraits trait, PatchedComponentHolder holder) {
            return 0;
      }

      @Override
      public int getBarWidth(XpTraits trait, PatchedComponentHolder holder) {
            return 0;
      }

      @Override
      public @Nullable ClientTooltipComponent getTooltipComponent(XpTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            return new XpTooltip(traits, itemStack, holder, title);
      }

      @Override
      public void appendEquipmentLines(XpTraits traits, Consumer<Component> pTooltipAdder) {
            int size = traits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(XpTraits traits, List<Component> lines) {
            int size = traits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
