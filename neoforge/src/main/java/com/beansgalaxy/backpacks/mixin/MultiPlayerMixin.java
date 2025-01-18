package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.network.serverbound.SyncShorthand;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerMixin {
      @Shadow @Final private Minecraft minecraft;

      @Inject(method = "attack", at = @At("HEAD"))
      private void shorthand_attack(Player pPlayer, Entity pTargetEntity, CallbackInfo ci) {
            Inventory inventory = pPlayer.getInventory();
            Shorthand shorthand = Shorthand.get(pPlayer);
            int timer = shorthand.getTimer();
            if (inventory.selected >= inventory.items.size() && timer > 0) {
                  shorthand.resetSelected(inventory);

            }
      }

      @Inject(method = "lambda$startDestroyBlock$1", at = @At("HEAD"))
      private void shorthand_startDestroyBlock(
                  BlockState blockstate, PlayerInteractEvent.LeftClickBlock event, BlockPos pLoc,
                  Direction pFace, int p_233728_, CallbackInfoReturnable<Packet> cir
      ) {
            if (!CommonClass.CLIENT_CONFIG.shorthand_control.get().autoEquips())
                  return;

            if (blockstate.isAir())
                  return;

            Shorthand shorthand = Shorthand.get(minecraft.player);
            float destroySpeed = blockstate.getDestroySpeed(minecraft.level, pLoc);
            shorthand.onAttackBlock(blockstate, destroySpeed);
            SyncShorthand.send(shorthand);
      }

      @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
      private void shorthand_continueDestroyBlock(BlockPos posBlock, Direction directionFacing, CallbackInfoReturnable<Boolean> cir) {
            Shorthand shorthand = Shorthand.get(minecraft.player);
            if (shorthand.getTimer() > 0) {
                  shorthand.loadTimer();
            }
      }
}
