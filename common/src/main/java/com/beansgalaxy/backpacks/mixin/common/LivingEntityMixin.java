package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
      @Shadow protected abstract boolean doesEmitEquipEvent(EquipmentSlot pSlot);

      @Shadow public abstract void remove(RemovalReason pReason);

      @Shadow public abstract void setHealth(float pHealth);

      @Shadow public abstract boolean removeAllEffects();

      @Shadow public abstract boolean addEffect(MobEffectInstance pEffectInstance);

      @Unique public final LivingEntity instance = (LivingEntity) (Object) this;

      public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Inject(method = "getDrinkingSound", at = @At("HEAD"), cancellable = true)
      private void lunchBoxDrinkingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
            LunchBoxTraits.firstIsPresent(pStack, instance, food -> {
                  cir.setReturnValue(food.getDrinkingSound());
            });
      }

      @Inject(method = "getEatingSound", at = @At("HEAD"), cancellable = true)
      private void lunchBoxEatingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
            LunchBoxTraits.firstIsPresent(pStack, instance, food -> {
                  cir.setReturnValue(food.getEatingSound());
            });
      }

      @Inject(method = "onEquipItem", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD,
                  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSilent()Z"))
      private void backpackOnEquip(EquipmentSlot equipmentSlot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci, boolean flag, Equipable equipment) {
            Optional<EquipableComponent> optional = EquipableComponent.get(newItem);
            if (optional.isEmpty()) {
                  if (equipment != null)
                        return;

                  EquipableComponent.get(oldItem).ifPresent(equipable -> {
                        if (this.doesEmitEquipEvent(equipmentSlot)) {
                              this.gameEvent(GameEvent.UNEQUIP);
                              ci.cancel();
                        }
                  });
                  return;
            }

            ci.cancel();
            if (this.doesEmitEquipEvent(equipmentSlot))
                  this.gameEvent(GameEvent.EQUIP);

            EquipableComponent equipable = optional.get();
            if (!isSilent() && equipable.slots().test(equipmentSlot)) {

                  Optional<Holder<SoundEvent>> equipSound;
                  if (instance instanceof Player player) {
                        if (player.isCreative()) return;
                        equipSound = equipable.getEquipSound();
                  }
                  else equipSound = equipable.getUnEquipOrFallback();

                  equipSound.ifPresent(sound -> {
                        level().playSeededSound(null, getX(), getY(), getZ(), sound, getSoundSource(), 1F, 1F, random.nextLong());
                  });
            }
      }

      @Inject(method = "checkTotemDeathProtection", cancellable = true, at = @At(value = "JUMP", opcode = 198, ordinal = 0))
      private void backpacks_utilityDeathProtection(DamageSource pDamageSource, CallbackInfoReturnable<Boolean> cir, @Local ItemStack itemStack) {
            if (itemStack != null)
                  return;

            ItemStack backpack = instance.getItemBySlot(EquipmentSlot.BODY);
            Optional<UtilityComponent.Mutable> optional = UtilityComponent.get(backpack);
            if (optional.isEmpty())
                  return;

            UtilityComponent.Mutable mutable = optional.get();
            for (Int2ObjectMap.Entry<ItemStack> entry : mutable.slots.int2ObjectEntrySet()) {
                  ItemStack utility = entry.getValue();
                  if (utility != null && utility.is(Items.TOTEM_OF_UNDYING)) {
                        itemStack = utility.copy();
                        utility.shrink(1);
                        if (instance instanceof ServerPlayer serverplayer) {
                              serverplayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                              CriteriaTriggers.USED_TOTEM.trigger(serverplayer, itemStack);
                              this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                        }

                        this.setHealth(1.0F);
                        this.removeAllEffects();
                        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
                        this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
                        this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
                        this.level().broadcastEntityEvent(this, (byte)35);
                        mutable.freeze();
                        cir.setReturnValue(true);
                        return;
                  }
            }
      }
}
