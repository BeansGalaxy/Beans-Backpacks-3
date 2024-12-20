package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.shorthand.Shorthand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
      @Inject(method = "getDestroyProgress", locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Player;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
      private void invokeShorthandSearch(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos, CallbackInfoReturnable<Float> cir, float f) {
            Shorthand.get(player).onAttackBlock(blockState, f);
      }
}
