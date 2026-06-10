package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DeathProtection;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
      @Shadow protected abstract boolean doesEmitEquipEvent(EquipmentSlot pSlot);

      @Shadow public abstract void remove(RemovalReason pReason);

      @Unique public final LivingEntity instance = (LivingEntity) (Object) this;

      public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Inject(method = "onEquipItem", cancellable = true,
                  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSilent()Z"))
      private void backpackOnEquip(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci, @Local Equippable equipment) {
            BackpackTraits nTraits = BackpackTraits.get(newItem);
            if (nTraits == null) {
                  if (equipment != null)
                        return;

                  BackpackTraits oTraits = BackpackTraits.get(oldItem);
                  if (oTraits != null) {
                        if (this.doesEmitEquipEvent(slot)) {
                              this.gameEvent(GameEvent.UNEQUIP);
                              ci.cancel();
                        }
                  }
                  return;
            }

            ci.cancel();
            if (this.doesEmitEquipEvent(slot))
                  this.gameEvent(GameEvent.EQUIP);

            if (!isSilent() && nTraits.slots().test(slot)) {
                  SoundEvent sound;
                  if (instance instanceof Player player) {
                        if (player.isCreative()) return;
                        sound = nTraits.sound().get(ModSound.Type.EQUIP);
                  }
                  else sound = nTraits.sound().get(ModSound.Type.PLACE);

                  level().playSeededSound(null, getX(), getY(), getZ(), sound, getSoundSource(), 1F, 1F, random.nextLong());
            }
      }

      @Inject(method = "checkTotemDeathProtection", cancellable = true, at =@At(value="CONSTANT", args="nullValue=true", ordinal = 0))
      private void backpacks_utilityDeathProtection(DamageSource pDamageSource, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = instance.getItemBySlot(EquipmentSlot.BODY);
            Optional<UtilityComponent.Mutable> optional = UtilityComponent.get(backpack);
            if (optional.isEmpty())
                  return;

            UtilityComponent.Mutable mutable = optional.get();
            for (Int2ObjectMap.Entry<ItemStack> entry : mutable.slots.int2ObjectEntrySet()) {
                  ItemStack utility = entry.getValue();
                  if (utility == null)
                        continue;
                  
                  DeathProtection deathProtection = utility.get(DataComponents.DEATH_PROTECTION);
                  if (deathProtection == null)
                        continue;
                  
                  deathProtection.applyEffects(utility, instance);
                  if (instance instanceof ServerPlayer serverplayer) {
                        serverplayer.awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
                        CriteriaTriggers.USED_TOTEM.trigger(serverplayer, utility);
                        this.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
                  }
                  
                  utility.shrink(1);
                  mutable.freeze();
                  cir.setReturnValue(true);
                  return;
            }
      }
}
