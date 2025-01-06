package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.container.BackSlot;
import com.beansgalaxy.backpacks.container.Shorthand;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayMixin {

      @Shadow public ServerPlayer player;

      @Inject(method = "handleSetCreativeModeSlot", cancellable = true,
                  at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z"))
      public void setCreativeBackSlot(ServerboundSetCreativeModeSlotPacket ctx, CallbackInfo ci) {
            int slotIndex = ctx.slotNum();
            ItemStack stack = ctx.itemStack();

            boolean outOfRange = player.inventoryMenu.slots.size() < slotIndex || slotIndex < 0;
            boolean flag2 = stack.isEmpty() || stack.getDamageValue() >= 0 && stack.getCount() <= 64 && !stack.isEmpty();
            if (!outOfRange && flag2) {
                  Slot slot = this.player.inventoryMenu.getSlot(slotIndex);
                  if (slot instanceof BackSlot) {
                        slot.setByPlayer(stack);
                        this.player.inventoryMenu.broadcastChanges();
                        ci.cancel();
                  }
            }
      }

      @Inject(method = "handleSetCarriedItem", at = @At(value = "HEAD", shift = At.Shift.AFTER), cancellable = true)
      private void handleShorthandCarriedItem(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
            int size = player.getInventory().items.size();
            int selectedSlot = packet.getSlot();
            if (selectedSlot >= size) {
                  Shorthand shorthand = Shorthand.get(player);
                  int tools = shorthand.tools.getContainerSize();
                  int weapons = shorthand.weapons.getContainerSize();
                  int max = size + tools + weapons;
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
      private void shorthandOnSwapOffhand(ServerboundPlayerActionPacket pPacket, CallbackInfo ci) {
            Inventory inventory = player.getInventory();
            Shorthand shorthand = Shorthand.get(player);
            shorthand.resetSelected(inventory);
      }
}
