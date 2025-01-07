package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BackSlot extends Slot implements EquipmentSlotAccess {
      public static final int SLOT = 41;

      private final Player owner;

      public BackSlot(Inventory inv) {
            super(inv, SLOT, getX(), getY());
            owner = inv.player;
      }

      public static int getX() {
            return CommonClass.CLIENT_CONFIG.back_slot_pos.get(0);
      }

      public static int getY() {
            return CommonClass.CLIENT_CONFIG.back_slot_pos.get(1);
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
            boolean standardCheck = stack.isEmpty();
            if (standardCheck)
                  return true;

            return EquipableComponent.get(stack).map(equipable ->
                        equipable.slots().test(EquipmentSlot.BODY)
            ).orElse(false);
      }

      @Override
      public boolean mayPickup(Player player) {
            ItemStack stack = getItem();
            if (stack.isEmpty())
                  return true;

            UtilityComponent utilities = stack.get(ITraitData.UTILITIES);
            if (utilities != null)
                  return false;

            boolean equipment = EquipableComponent.testIfPresent(stack, EquipableComponent::traitRemovable);
            if (equipment)
                  return true;

            return !Traits.testIfPresent(stack, traits -> !traits.isEmpty(stack));
      }

      @Override
      public void setByPlayer(ItemStack pNewStack, ItemStack pOldStack) {
            this.owner.onEquipItem(EquipmentSlot.BODY, pOldStack, pNewStack);
            super.setByPlayer(pNewStack, pOldStack);
      }

      @Override
      public void setChanged() {
            super.setChanged();

            UtilityContainer utilities = UtilityContainer.get(owner);
            ItemStack newStack = getItem();
            utilities.size = UtilityComponent.getSize(newStack);
      }

      @Override
      public EquipmentSlot getSlot() {
            return EquipmentSlot.BODY;
      }

      @Nullable @Override
      public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/empty_slot_backpack"));
      }
}
