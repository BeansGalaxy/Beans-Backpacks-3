package com.beansgalaxy.backpacks.screen;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
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
      public static final int X = 77;
      public static final int Y = 44;

      private final Player owner;

      public BackSlot(Inventory inv, int slot) {
            super(inv, slot, X, Y);
            owner = inv.player;
      }

      public BackSlot(Inventory inv, int slot, int x, int y) {
            super(inv, slot, x + 32 + 20 + 16, y - 29 - 13);
            owner = inv.player;
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
            boolean standardCheck = stack.isEmpty();
            Boolean orElse = EquipableComponent.get(stack).map(equipable ->
                        equipable.slots().test(EquipmentSlot.BODY)
            ).orElse(false);
            return standardCheck || orElse;
      }

      @Override
      public boolean mayPickup(Player player) {
            ItemStack stack = getItem();
            boolean standardCheck = stack.isEmpty();
            boolean equipment = EquipableComponent.testIfPresent(stack, EquipableComponent::traitRemovable);
            boolean emptyTrait = !Traits.testIfPresent(stack, traits -> !traits.isEmpty(stack));
            return standardCheck || equipment || emptyTrait;
      }

      @Override
      public void setByPlayer(ItemStack pNewStack, ItemStack pOldStack) {
            this.owner.onEquipItem(EquipmentSlot.BODY, pOldStack, pNewStack);
            super.setByPlayer(pNewStack, pOldStack);
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
