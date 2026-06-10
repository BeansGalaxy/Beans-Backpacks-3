package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Optional;

@Mixin(ComparatorBlock.class)
public class ComparatorMixin {
      @Inject(method = "getInputSignal", cancellable = true,
                  at = @At(value = "INVOKE_ASSIGN", target = "Ljava/lang/Math;max(II)I"))
      private void injectBackpackComparatorSignal(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir, @Local Direction direction, @Local(ordinal = 1) BlockPos blockPos, @Local(ordinal = 0) int i) {
            List<BackpackEntity> backpacks = getBackpack(level, direction, blockPos);
            int signal = i;
            for (BackpackEntity backpack : backpacks) {
                  IEntityTraits<?> traits = backpack.getTraits();
                  int analog = traits.getAnalogOutput(backpack);
                  if (analog > signal) {
                        signal = analog;
                  }
            }

            if (signal > i)
                  cir.setReturnValue(signal);
      }

      @Unique
      private List<BackpackEntity> getBackpack(Level level, Direction direction, BlockPos blockPos) {
            AABB box = new AABB(blockPos.getX(), blockPos.getY() + 2/8f, blockPos.getZ(),
                        blockPos.getX() + 1, blockPos.getY() + 4/8f, blockPos.getZ() + 1);

            List<BackpackEntity> list = level.getEntitiesOfClass(BackpackEntity.class, box, (backpack) -> {
                  if (backpack == null)
                        return false;
                  
                  Direction direction1 = backpack.getDirection();
                  return direction1 == direction;
            });

            return list;
      }
}
