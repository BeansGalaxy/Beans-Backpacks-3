package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
      @Shadow @Nullable public LocalPlayer player;

      @SuppressWarnings("InvalidInjectorMethodSignature")
      @Inject(method = "pickBlock", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"))
      private void pickFromBackpack(CallbackInfo ci, boolean flag, BlockEntity blockentity, HitResult.Type hitresult$type, ItemStack itemstack, Inventory inventory) {
            ItemStorageTraits.runIfEquipped(player, (traits, equipmentSlot) -> {
                  traits.clientPickBlock(equipmentSlot, flag, inventory, itemstack, player, ci);
                  return ci.isCancelled();
            });
      }
}
