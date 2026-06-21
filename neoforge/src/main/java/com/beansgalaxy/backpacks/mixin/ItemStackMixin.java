package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
      @Inject(method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
                  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;", ordinal = 1))
      private void emptyBrokenArmorTraits(int damage, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
            if (entity instanceof ServerPlayer player) {
                  ItemStack instance = (ItemStack) (Object) this;
                  ItemStorageTraits.runIfPresent(instance, traits ->
                              traits.breakTrait(player, instance)
                  );
            }
      }
}
