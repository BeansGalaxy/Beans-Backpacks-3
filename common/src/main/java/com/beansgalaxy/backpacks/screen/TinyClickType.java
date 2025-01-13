package com.beansgalaxy.backpacks.screen;

import net.minecraft.world.entity.EquipmentSlot;

public enum TinyClickType {
      LEFT(-1),
      RIGHT(-1),
      SHIFT(-1),
      I_SHIFT(-1),
      ACTION(-1),
      DROP(-1),
      H_0(0),
      H_1(1),
      H_2(2),
      H_3(3),
      H_4(4),
      H_5(5),
      H_6(6),
      H_7(7),
      H_8(8),
      ;

      public final int hotbarSlot;
      public final EquipmentSlot equipmentSlot;

      TinyClickType(int slot) {
            hotbarSlot = slot;
            equipmentSlot = null;
      }

      public boolean isHotbar() {
            return hotbarSlot != -1;
      }

      public static TinyClickType getHotbar(int hotbarSlot) {
            return switch (hotbarSlot) {
                  case 0 -> H_0;
                  case 1 -> H_1;
                  case 2 -> H_2;
                  case 3 -> H_3;
                  case 4 -> H_4;
                  case 5 -> H_5;
                  case 6 -> H_6;
                  case 7 -> H_7;
                  case 8 -> H_8;
                  default -> SHIFT;
            };
      }

      public boolean isRight() {
            return this == RIGHT;
      }

      public boolean isShift() {
            return this == SHIFT || this == I_SHIFT;
      }

      public boolean isAction() {
            return this == ACTION;
      }

      public boolean isDrop() {
            return this == DROP;
      }
}
