package com.beansgalaxy.backpacks.traits.battery;

import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

public class BatteryTraits extends GenericTraits {
      public static final String NAME = "battery";
      private final int size;
      private final int speed;

      public BatteryTraits(ModSound sound, int size, int speed) {
            super(sound);
            this.size = size;
            this.speed = speed;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public IClientTraits<? extends GenericTraits> client() {
            return BatteryClient.INSTANCE;
      }

      @Override
      public BatteryEntity entity() {
            return BatteryEntity.INSTANCE;
      }

      @Override
      public BatteryMutable mutable(PatchedComponentHolder holder) {
            return new BatteryMutable(this, holder);
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BATTERY;
      }

      public int size() {
            return size;
      }

      public int speed() {
            return speed;
      }

      @Override
      public Fraction fullness(PatchedComponentHolder holder) {
            Long aLong = holder.get(ITraitData.LONG);
            if (aLong == null || aLong == 0)
                  return Fraction.ZERO;
            return Fraction.getFraction(aLong.intValue(), size());
      }

      @Override
      public boolean isEmpty(PatchedComponentHolder holder) {
            return holder.has(ITraitData.LONG) || holder.has(ITraitData.SOLO_STACK);
      }

      @Override
      public void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
            BatteryMutable mutable = mutable(backpack);
            if (mutable.item.get().isEmpty() && other.isEmpty()) {
                  return;
            }

            boolean empty = !EquipableComponent.testIfPresent(backpack, equipable -> !equipable.traitRemovable());
            if (empty) {
                  if (ClickAction.SECONDARY.equals(click)) {
                        ItemStack itemStack = mutable.insert(other, player);
                        access.set(itemStack);
                        mutable.push(cir);
                  }
            }
            else if (EquipableComponent.canEquip(backpack, slot)) {
                  ItemStack itemStack = mutable.insert(other, player);
                  access.set(itemStack);
                  mutable.push(cir);
            }
      }

      @Override
      public void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {
            if (ClickAction.SECONDARY.equals(click) && EquipableComponent.get(backpack).isEmpty()) {
                  BatteryMutable mutable = mutable(backpack);
                  ItemStack itemStack = mutable.insert(other, player);
                  slot.set(itemStack);
                  mutable.push(cir);
            }
      }

      @Override
      public void inventoryTick(PatchedComponentHolder backpack, Level level, Entity entity, int slot, boolean selected) {
            ItemStack stack = backpack.get(ITraitData.SOLO_STACK);
            if (stack == null || stack.isEmpty())
                  return;

      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BatteryTraits that)) return false;
            if (!super.equals(o)) return false;
            return size == that.size
                        && speed == that.speed
                        && sound() == that.sound();
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), size, speed);
      }

      @Override
      public String toString() {
            return "BatteryFields{" +
                        "size=" + size +
                        ", speed=" + speed +
                        ", sound=" + sound() +
                        '}';
      }
}
