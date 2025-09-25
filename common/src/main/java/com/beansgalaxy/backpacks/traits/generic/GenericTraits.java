package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public abstract class GenericTraits {
      private final ModSound sound;

      public GenericTraits(ModSound sound) {
            this.sound = sound;
      }

      public ModSound sound() {
            return sound;
      }

      public abstract String name();

      public abstract <T extends GenericTraits> IClientTraits<T> client();

      public abstract <T extends GenericTraits> IEntityTraits<T> entity();

      abstract public TraitComponentKind<? extends GenericTraits> kind();

      public abstract Fraction fullness(PatchedComponentHolder holder);

      public Fraction fullness(ItemStack stack) {
            return fullness(PatchedComponentHolder.of(stack));
      }

      public boolean isFull(ItemStack stack) {
            return isFull(PatchedComponentHolder.of(stack));
      }

      public boolean isFull(PatchedComponentHolder holder) {
            Fraction fullness = fullness(holder);
            int i = fullness.compareTo(Fraction.ONE);
            return i >= 0;
      }

      public boolean isEmpty(ItemStack stack) {
            return isEmpty(PatchedComponentHolder.of(stack));
      }

      public abstract boolean isEmpty(PatchedComponentHolder holder);

      public abstract void stackedOnMe(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir);

      public abstract void stackedOnOther(PatchedComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir);

      public void use(Level level, Player player, InteractionHand hand, PatchedComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

      }

      public abstract MutableTraits mutable(PatchedComponentHolder holder);

      public boolean isStackable(PatchedComponentHolder holder) {
            return false;
      }

      public int getAnalogOutput(PatchedComponentHolder holder) {
            return 0;
      }

      public void onPlayerInteract(LivingEntity owner, Player player, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {

      }
}
