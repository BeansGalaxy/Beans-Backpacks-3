package com.beansgalaxy.backpacks.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayMixin {

      @Shadow public ServerPlayer player;

      @Inject(method = "handleSetCreativeModeSlot",
                  at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
      public void setCreativeBackSlot(ServerboundSetCreativeModeSlotPacket ctx, CallbackInfo ci, @Local(ordinal = 1) LocalBooleanRef withinBounds) {
            if (withinBounds.get())
                  return;

            int slotIndex = ctx.slotNum();
            if (slotIndex < 0)
                  return;

            NonNullList<Slot> slots = player.inventoryMenu.slots;
            if (slotIndex >= slots.size())
                  return;

            withinBounds.set(true);
      }
}
