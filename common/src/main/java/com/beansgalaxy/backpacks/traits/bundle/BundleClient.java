package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.network.serverbound.SyncSelectedSlot;
import com.beansgalaxy.backpacks.screen.TraitMenu;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.TraitTooltip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BundleClient implements IClientTraits<BundleLikeTraits> {
      static final BundleClient INSTANCE = new BundleClient();

      @Override
      public void renderTooltip(BundleLikeTraits trait, ItemStack itemStack, PatchedComponentHolder holder, GuiGraphics gui, int mouseX, int mouseY, CallbackInfo ci) {
            if (!trait.isEmpty(holder)) {
                  Minecraft minecraft = Minecraft.getInstance();

                  LocalPlayer player = minecraft.player;
                  boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
                  boolean isQuickMove = BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
                  boolean noSpace = trait.fullness(holder).compareTo(Fraction.ONE) == 0;
                  boolean hideEmptySlot = carriedEmpty || isQuickMove || noSpace;

                  Component title;
                  int selectedSlot = trait.getSelectedSlot(holder, player);
                  if (selectedSlot == 0 && !hideEmptySlot) {
                        title = Component.empty();
                  } else {
                        int selectedSlotSafe = selectedSlot == 0 ? 0 : selectedSlot - 1;
                        List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                        ItemStack selected = stacks.get(selectedSlotSafe);
                        title = Constants.getName(selected);
                  }

                  TraitTooltip<?> tooltip = new TraitTooltip<>(trait, itemStack, holder, title);
                  gui.renderTooltip(minecraft.font, List.of(title), Optional.of(tooltip), mouseX, mouseY);
                  ci.cancel();
            }
      }

      @Override
      public int getBarWidth(BundleLikeTraits trait, PatchedComponentHolder holder) {
            Fraction fullness = trait.fullness(holder);
            if (trait.isFull(holder))
                  return (14);
            else {
                  float value = fullness.multiplyBy(Fraction.getFraction(13, 1)).floatValue();
                  return (Mth.floor(value) + 1);
            }
      }

      @Override
      public int getBarColor(BundleLikeTraits trait, PatchedComponentHolder holder) {
            if (trait.isFull(holder))
                  return RED_BAR;
            else
                  return BAR_COLOR;
      }

      @Override
      public TraitMenu<BundleLikeTraits> createTooltip(Minecraft minecraft, int leftPos, int topPos, @Nullable Slot slot, PatchedComponentHolder holder, BundleLikeTraits traits) {
            return new BundleMenu<>(minecraft, leftPos, topPos, slot, holder, traits);
      }

      @Override @Nullable
      public ClientTooltipComponent getTooltipComponent(BundleLikeTraits traits, ItemStack itemStack, PatchedComponentHolder holder, Component title) {
            ArrayList<ItemStack> stacks = new ArrayList<>(holder.getOrDefault(ITraitData.ITEM_STACKS, List.of()));
            return new BundleTooltip(traits, itemStack, stacks, holder, title);
      }

      @Override
      public boolean mouseScrolled(BundleLikeTraits trait, PatchedComponentHolder holder, Level level, Slot hoveredSlot, int containerId, int scrolled) {
            LocalPlayer player = Minecraft.getInstance().player;
            int startSlot = trait.getSelectedSlot(holder, player);

            boolean carriedEmpty = player.containerMenu.getCarried().isEmpty();
            boolean isQuickMove =  BackData.get(player).isMenuKeyDown() || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
            boolean hideEmptySlot = carriedEmpty || isQuickMove || trait.isFull(holder);

            int selectedSlot = startSlot - scrolled;
            if (hideEmptySlot && startSlot == 0 && scrolled == -1)
                  selectedSlot++;

            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            int size = stacks == null ? 0 : stacks.size();
            int i = size == 0 || selectedSlot < 0 ? 0
                        : Math.min(selectedSlot, size);

            if (startSlot == i)
                  return false;

            trait.setSelectedSlot(holder, player, i);
            SyncSelectedSlot.send(containerId, hoveredSlot.index, i);

            return true;
      }

      @Override
      public void appendEquipmentLines(BundleLikeTraits traits, Consumer<Component> pTooltipAdder) {
            int size = traits.size();
            pTooltipAdder.accept(Component.translatable("traits.beansbackpacks.equipment." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }

      @Override
      public void appendTooltipLines(BundleLikeTraits traits, List<Component> lines) {
            int size = traits.size();
            lines.add(Component.translatable("traits.beansbackpacks.inventory." + traits.name() + (size == 1 ? ".solo" : ".size"), size).withStyle(ChatFormatting.GOLD));
      }
}
