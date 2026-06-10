package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
      @Inject(method="applyDamage", at=@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
      private void applyDamage(int damage, ServerPlayer player, Consumer<Item> onBreak, CallbackInfo ci) {
            ItemStack instance = (ItemStack) (Object) this;
            ItemStorageTraits.runIfPresent(instance, traits ->
                  traits.breakTrait(player, instance)
            );
      }
}
