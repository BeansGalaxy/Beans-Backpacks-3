package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.DecoratedPotEntityAccess;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DecoratedPotBlock.class)
public abstract class DecoratedPotBlockMixin extends BaseEntityBlock {
      @Unique public final DecoratedPotBlock instance = (DecoratedPotBlock) (Object) this;
      
      protected DecoratedPotBlockMixin(Properties pProperties) {
            super(pProperties);
      }
      
      @Override
      protected void attack(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer) {
            DecoratedPotEntityAccess.attack(pLevel, pPos, pPlayer);
            super.attack(pState, pLevel, pPos, pPlayer);
      }
      
      @Inject(method="useItemOn", cancellable = true, at=@At("HEAD"))
      private void useItemOn(ItemStack inHand, BlockState p_316562_, Level level, BlockPos pos, Player player, InteractionHand p_316424_, BlockHitResult p_316345_, CallbackInfoReturnable<InteractionResult> cir) {
            cir.setReturnValue(DecoratedPotEntityAccess.useItemOn(inHand, level, pos, player));
      }
      
      @Inject(method="useWithoutItem", cancellable = true, at=@At("HEAD"))
      private void useWithoutItem(BlockState p_316866_, Level level, BlockPos pos, Player player, BlockHitResult p_316860_, CallbackInfoReturnable<InteractionResult> cir) {
            cir.setReturnValue(DecoratedPotEntityAccess.useWithoutItem(level, pos, player));
      }
      
      @Inject(method="getDrops", at=@At(value="INVOKE", target="Lnet/minecraft/world/level/storage/loot/LootParams$Builder;withDynamicDrop(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/world/level/storage/loot/LootParams$DynamicDrop;)Lnet/minecraft/world/level/storage/loot/LootParams$Builder;"))
      private void getDrops(BlockState state, LootParams.Builder loot, CallbackInfoReturnable<List<ItemStack>> cir, @Local DecoratedPotBlockEntity entity) {
            if (entity instanceof DecoratedPotEntityAccess access) {
                  loot.withDynamicDrop(Constants.defaultLocation("bulk"), access::collectDrops);
            }
      }
      
      @Inject(method="getAnalogOutputSignal", at=@At("HEAD"))
      private void getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
            cir.setReturnValue(DecoratedPotEntityAccess.getAnalogOutputSignal(level, pos));
      }
      
      
}
