package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.DecoratedPotEntityAccess;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DecoratedPotBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
      @Shadow @Final protected ServerPlayer player;
      
      @Shadow protected ServerLevel level;
      
      @Inject(method="destroyBlock", cancellable = true, at=@At(value="INVOKE", target="Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"))
      private void destroyBlock(BlockPos pPos, CallbackInfoReturnable<Boolean> cir, @Local Block block) {
            if (!player.isCreative())
                  return;
            
            if (block instanceof DecoratedPotBlock && DecoratedPotEntityAccess.attack(level, pPos, player))
                  cir.cancel();
      }
}
