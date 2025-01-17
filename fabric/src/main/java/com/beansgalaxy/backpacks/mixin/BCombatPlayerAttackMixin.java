package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.container.Shorthand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(PlayerAttackHelper.class)
public abstract class BCombatPlayerAttackMixin {
      @Shadow private static void setAttributesForOffHandAttack(Player player, boolean useOffHand) {
      }

      @Inject(method = "swapHandAttributes(Lnet/minecraft/world/entity/player/Player;ZLjava/lang/Runnable;)V",
                  cancellable = true, at = @At("HEAD"))
      private static void shorthandSwapHandAttributes(Player player, boolean useOffHand, Runnable runnable, CallbackInfo ci) {
            if (useOffHand) {
                  Inventory inventory = player.getInventory();
                  int itemsSize = inventory.items.size();
                  if (inventory.selected < itemsSize)
                        return;

                  Shorthand shorthand = Shorthand.get(player);
                  int i = inventory.selected - itemsSize;
                  if (i < 0) {
                        shorthand.resetSelected(inventory);
                        return;
                  } else ci.cancel();

                  synchronized (player) {
                        ItemStack mainHandStack = shorthand.getItem(i);
                        ItemStack offHandStack = inventory.offhand.get(0);

                        setAttributesForOffHandAttack(player, true);
                        shorthand.putItem(i, offHandStack);
                        inventory.offhand.set(0, offHandStack);
                        runnable.run();
                        shorthand.putItem(i, mainHandStack);
                        inventory.offhand.set(0, offHandStack);
                        setAttributesForOffHandAttack(player, false);
                  }
            }
      }
}
