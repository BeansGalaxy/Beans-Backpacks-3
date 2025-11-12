package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.access.DecoratedPotEntityAccess;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiplayerGameModeMixin {
      @Shadow @Final private Minecraft minecraft;
      
      @Inject(method="destroyBlock", cancellable = true, at=@At(value="INVOKE",
            target="Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
      private void destroyBlock(BlockPos pPos, CallbackInfoReturnable<Boolean> cir, @Local Level level, @Local Block block) {
            LocalPlayer player = minecraft.player;
            if (!player.isCreative())
                  return;
            
            if (block instanceof DecoratedPotBlock && DecoratedPotEntityAccess.attack(level, pPos, player))
                  cir.cancel();
      }
}
