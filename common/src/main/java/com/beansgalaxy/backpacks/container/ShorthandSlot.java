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

public class ShorthandSlot extends Slot {
      private final Shorthand shorthand;
      private final ResourceLocation icon;

      public ShorthandSlot(Shorthand shorthand, int pSlot) {
            super(shorthand, pSlot, getX(pSlot), getY(pSlot));
            this.shorthand = shorthand;
            this.icon = getIcon(pSlot);
      }

      private static ResourceLocation getIcon(int i) {
            return switch (i % 9) {
                  default -> ResourceLocation.withDefaultNamespace("item/empty_slot_pickaxe");
                  case 1, 5 -> ResourceLocation.withDefaultNamespace("item/empty_slot_shovel");
                  case 2, 6 -> ResourceLocation.withDefaultNamespace("item/empty_slot_axe");
                  case 3 -> ResourceLocation.withDefaultNamespace("item/empty_slot_hoe");
                  case 7 -> ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "item/empty_slot_bone");
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
            return ShorthandSlot.isTool(stack) || ServerSave.CONFIG.shorthand_additions.get().contains(item);
      }

      @Nullable @Override
      public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
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
            || ServerSave.CONFIG.shorthand_additions.get().contains(item);
      }

      @Override
      public boolean isActive() {
            int containerSlot = getContainerSlot();
            int containerSize = shorthand.getContainerSize();
            return containerSlot < containerSize;
      }
}
