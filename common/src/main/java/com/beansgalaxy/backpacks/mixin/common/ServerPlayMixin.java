package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.container.Shorthand;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
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

      @Inject(method = "handleSetCarriedItem", at = @At(value = "HEAD", shift = At.Shift.AFTER), cancellable = true)
      private void handleShorthandCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
            int size = player.getInventory().items.size();
            int selectedSlot = packet.getSlot();
            if (selectedSlot >= size) {
                  Shorthand shorthand = Shorthand.get(player);
                  int weapons = shorthand.getContainerSize();
                  int max = size + weapons;
                  if (selectedSlot > max)
                        return;

                  if (this.player.getInventory().selected != selectedSlot && this.player.getUsedItemHand() == InteractionHand.MAIN_HAND)
                        this.player.stopUsingItem();

                  this.player.getInventory().selected = selectedSlot;
                  this.player.resetLastActionTime();
                  ci.cancel();
            }
      }

      @Inject(method = "handlePlayerAction", at = @At(value = "INVOKE", ordinal = 0,
                  target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
      private void shorthand_swapOffhand(ServerboundPlayerActionPacket pPacket, CallbackInfo ci) {
            Inventory inventory = player.getInventory();
            Shorthand shorthand = Shorthand.get(player);
            shorthand.resetSelected(inventory);
      }
}
