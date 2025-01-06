package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class ShorthandSlot extends Slot {
      private final ShortContainer shortContainer;

      public ShorthandSlot(ShortContainer shortContainer, int pSlot, int pX, int pY) {
            super(shortContainer, pSlot, pX, pY);
            this.shortContainer = shortContainer;
      }

      private static boolean stackHasAttribute(ItemStack stack) {
            ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (modifiers == null) {
                  ReferenceTrait reference = stack.get(Traits.REFERENCE);
                  if (reference == null)
                        return false;

                  Optional<ItemAttributeModifiers> optional = reference.getAttributes();
                  if (optional.isEmpty())
                        return false;

                  modifiers = optional.get();
            }

            double compute = modifiers.compute(1.0, EquipmentSlot.MAINHAND);
            return compute != 1.0;
      }

      public static boolean isTool(ItemStack stack) {
            Item item = stack.getItem();
            return item instanceof DiggerItem
            || item instanceof ShearsItem
            || ServerSave.CONFIG.tool_belt_additions.get().contains(item);
      }

      @Override
      public boolean isActive() {
            int containerSlot = getContainerSlot();
            int containerSize = shortContainer.getContainerSize();
            return containerSlot < containerSize;
      }

      public static class WeaponSlot extends ShorthandSlot {
            private final ResourceLocation icon;

            public WeaponSlot(Shorthand shorthand, int slot) {
                  super(shorthand.weapons, slot, getX(slot), getY(slot));
                  icon = getIcon(slot);
            }

            private static ResourceLocation getIcon(int i) {
                  return switch (i % 4) {
                        default -> ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
                        case 1 -> ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
                        case 2 -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/empty_slot_shears");
                        case 3 -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/empty_slot_bone");
                  };
            }

            public static int getX(int slot) {
                  return 152 - (slot * 18);
            }

            public static int getY(int slot) {
                  return 164;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                  Item item = stack.getItem();
                  if (ShorthandSlot.isTool(stack) || ServerSave.CONFIG.shorthand_additions.get().contains(item))
                        return true;

                  if (!ServerSave.CONFIG.allow_shorthand_weapons.get())
                        return false;

                  if (ShorthandSlot.stackHasAttribute(stack))
                        return true;

                  return item instanceof ProjectileWeaponItem;
            }

            @Nullable @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                  return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
            }
      }

      public static class ToolSlot extends ShorthandSlot {
            private final ResourceLocation icon;
            private final Shorthand shorthand;

            public ToolSlot(Shorthand shorthand, int slot) {
                  super(shorthand.tools, slot, getX(slot), getY(slot));
                  this.shorthand = shorthand;
                  this.icon = getIcon(slot);
            }

            private static ResourceLocation getIcon(int i) {
                  return switch (i % 6) {
                        case 0 -> ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
                        default -> ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
                        case 2 -> ResourceLocation.withDefaultNamespace("item/empty_slot_hoe");
                        case 4 -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/empty_slot_stick");
                        case 5 -> ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
                  };
            }

            public static int getX(int slot) {
                  return 151 - (slot * 18);
            }

            public static int getY(int slot) {
                  return 164;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                  return ShorthandSlot.isTool(stack);
            }

            @Nullable @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                  return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
            }

            public ItemStack getItem() {
                  return this.container.getItem(this.getContainerSlot());
            }

            public void set(ItemStack pStack) {
                  this.container.setItem(this.getContainerSlot(), pStack);
                  this.setChanged();
            }

            public ItemStack remove(int pAmount) {
                  return this.container.removeItem(this.getContainerSlot(), pAmount);
            }

            public int getContainerSlot() {
                  return super.getContainerSlot() - shorthand.getWeaponsSize();
            }

            @Override
            public boolean isActive() {
                  return getContainerSlot() >= 0 && super.isActive();
            }
      }
}
