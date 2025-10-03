package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class NonTraitClient implements IClientTraits<NonTrait> {
      static final NonTraitClient INSTANCE = new NonTraitClient();

      @Override
      public boolean isBarVisible(NonTrait trait, ComponentHolder holder) {
            return false;
      }

      @Override
      public int getBarColor(NonTrait trait, ComponentHolder holder) {
            return 0;
      }

      @Override @Nullable
      public TraitMenu<NonTrait> createTooltip(Minecraft minecraft, int leftPos, int topPos, @Nullable Slot slot, ComponentHolder holder, NonTrait traits) {
            return null;
      }

      @Override
      public int getBarWidth(NonTrait trait, ComponentHolder holder) {
            return 0;
      }

      @Override
      public void appendTooltipLines(NonTrait traits, Consumer<Component> lines) {
      }
}
