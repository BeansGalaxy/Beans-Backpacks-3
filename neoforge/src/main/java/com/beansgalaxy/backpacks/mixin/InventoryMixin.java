package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements BackData {
      @Shadow @Final public Player player;
      @Unique @Final public NonNullList<ItemStack> beans_Backpacks_3$body = NonNullList.withSize(1, ItemStack.EMPTY);
      @Shadow private List<NonNullList<ItemStack>> compartments;

      @Inject(method = "<init>", at = @At("TAIL"))
      public void backpackInit(Player player, CallbackInfo ci) {
            ImmutableList.Builder<NonNullList<ItemStack>> builder = ImmutableList.builder();
            compartments = builder.addAll(this.compartments).add(beans_Backpacks_3$body).build();
      }

      @Unique private boolean beans_Backpacks_3$actionKeyIsDown = false;
      @Unique private boolean beans_Backpacks_3$menuKeyIsDown = false;
      @Unique private int beans_Backpacks_3$tinySlot = -1;

      @Override
      public boolean isActionKeyDown() {
            return beans_Backpacks_3$actionKeyIsDown;
      }

      @Override
      public void setActionKey(boolean actionKeyIsDown) {
            this.beans_Backpacks_3$actionKeyIsDown = actionKeyIsDown;
      }

      @Override
      public boolean isMenuKeyDown() {
            return beans_Backpacks_3$menuKeyIsDown;
      }

      @Override
      public void setMenuKey(boolean menuKeyIsDown) {
            this.beans_Backpacks_3$menuKeyIsDown = menuKeyIsDown;
      }

      @Override
      public int getTinySlot() {
            return beans_Backpacks_3$tinySlot;
      }

      @Unique private Shorthand shorthand;

      @Override @Unique
      public Shorthand getShorthand() {
            if (shorthand == null)
                  shorthand = new Shorthand(player);
            return shorthand;
      }

      @Override
      public void setTinySlot(int tinySlot) {
            beans_Backpacks_3$tinySlot = tinySlot;
      }

      public NonNullList<ItemStack> beans_Backpacks_3$getBody() {
            return beans_Backpacks_3$body;
      }
}
