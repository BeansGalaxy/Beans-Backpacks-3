package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.container.Shorthand;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerMixin {
      @Inject(method = "attack", at = @At("HEAD"))
      private void shorthand_attack(Player pPlayer, Entity pTargetEntity, CallbackInfo ci) {
            Inventory inventory = pPlayer.getInventory();
            Shorthand shorthand = Shorthand.get(pPlayer);
            int timer = shorthand.getTimer();
            if (inventory.selected >= inventory.items.size() && timer > 0) {
                  shorthand.resetSelected(inventory);

            }
      }
}
