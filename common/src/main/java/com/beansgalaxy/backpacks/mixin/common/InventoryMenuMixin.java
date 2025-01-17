package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.container.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingInput, CraftingRecipe> {
      public InventoryMenuMixin(MenuType<?> $$0, int $$1) {
            super($$0, $$1);
      }

      @Inject(method = "<init>", at = @At("RETURN"))
      private void createBackSlot(Inventory inv, boolean active, Player owner, CallbackInfo ci) {
            addSlot(new BackSlot(inv));

            BackData backData = BackData.get(owner);

            UtilityContainer utility = backData.getUtility();
            addSlot(new UtilitySlot(utility, 0));
            addSlot(new UtilitySlot(utility, 1));

            Shorthand shorthand = backData.getShorthand();

            for (int i = 0; i < 9; i++)
                  this.addSlot(new ShorthandSlot(shorthand, i));
      }
}
