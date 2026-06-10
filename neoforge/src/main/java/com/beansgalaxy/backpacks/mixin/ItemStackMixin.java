package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
      @Inject(method="applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
      private void applyDamage(int p_361754_, LivingEntity livingEntity, Consumer<Item> p_360895_, CallbackInfo ci) {
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                  ItemStack instance = (ItemStack) (Object) this;
                  ItemStorageTraits.runIfPresent(instance, traits ->
                        traits.breakTrait(serverPlayer, instance)
                  );
            }
      }
}
