package com.beansgalaxy.backpacks.traits.generic;

import com.beansgalaxy.backpacks.network.serverbound.TraitMenuClick;
import com.beansgalaxy.backpacks.traits.IClientTraits;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
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

      public abstract Fraction fullness(ComponentHolder holder);

      public Fraction fullness(ItemStack stack) {
            return fullness(ComponentHolder.of(stack));
      }

      public boolean isFull(ItemStack stack) {
            return isFull(ComponentHolder.of(stack));
      }

      public boolean isFull(ComponentHolder holder) {
            Fraction fullness = fullness(holder);
            int i = fullness.compareTo(Fraction.ONE);
            return i >= 0;
      }

      public boolean isEmpty(ItemStack stack) {
            return isEmpty(ComponentHolder.of(stack));
      }

      public abstract boolean isEmpty(ComponentHolder holder);

      public abstract void stackedOnMe(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir);

      public abstract void stackedOnOther(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir);

      public void use(Level level, Player player, InteractionHand hand, ComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {

      }

      public abstract MutableTraits mutable(ComponentHolder holder);

      public boolean isStackable(ComponentHolder holder) {
            return false;
      }

      public int getAnalogOutput(ComponentHolder holder) {
            return 0;
      }

      public void onPlayerInteract(LivingEntity owner, Player player, ItemStack backpack, CallbackInfoReturnable<InteractionResult> cir) {

      }

      public void menuClick(ComponentHolder holder, int index, TraitMenuClick.Kind type, SlotAccess access, Player sender) {

      }
}
