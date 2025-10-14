package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.OptionalInt;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftAccessor {
      @Shadow static Minecraft instance;
      @Shadow @Nullable public LocalPlayer player;
      @Shadow public ClientLevel level;

      @Shadow @Nullable public HitResult hitResult;
      @Shadow private int rightClickDelay;
      @Shadow @Final private DeltaTracker.Timer timer;

      @Shadow public abstract DeltaTracker getTimer();

      @Shadow private volatile boolean pause;
      @Unique public final EnderStorage beans_Backpacks_2$enderStorage = new EnderStorage();

      @Override
      public EnderStorage beans_Backpacks_2$getEnder() {
            return beans_Backpacks_2$enderStorage;
      }

      @Inject(method = "startUseItem", cancellable = true, at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I"))
      private void hotkeyUseItemOn(CallbackInfo ci) {
            boolean isBlockHit = HitResult.Type.BLOCK.equals(instance.hitResult.getType());
            if(!isBlockHit || !BackData.get(player).isActionKeyDown()) {
                  return;
            }

            BlockHitResult blockHitResult = (BlockHitResult) instance.hitResult;
            BlockPos blockPos = blockHitResult.getBlockPos();
            if (!instance.level.getWorldBorder().isWithinBounds(blockPos))
                  return;

            KeyPress keyPress = KeyPress.INSTANCE;
            LocalPlayer player = instance.player;

            if (keyPress.ACTION_KEY.isUnbound()) {
                  OptionalInt callback = keyPress.loadCoyoteClick(player, blockHitResult);
                  if (callback.isPresent()) {
                        rightClickDelay = callback.getAsInt();
                        ci.cancel();
                  }
            }
            else if (KeyPress.placeBackpack(player, blockHitResult) || keyPress.pickUpThru(player))
                  ci.cancel();
      }

      @Inject(method = "startUseItem", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/world/InteractionResult;shouldSwing()Z"))
      private void tryCoyoteClick(CallbackInfo ci, @Local(ordinal = 1) InteractionResult result) {
            KeyPress.INSTANCE.cancelCoyoteClick();
      }

      @Inject(method = "handleKeybinds", at = @At("TAIL"))
      private void handleBackpackKeybinds(CallbackInfo ci) {
            CommonClient.handleKeyBinds(player, hitResult);
      }

      @Inject(method = "runTick", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V"))
      private void runTickTest(boolean pRenderLevel, CallbackInfo ci) {
            if (player != null && !pause) {
                  DeltaTracker tracker = getTimer();
                  KeyPress.INSTANCE.tick(instance, player, tracker);
                  
                  CommonClient.testingHitbox(instance, hitResult, player);
            }
      }

      @Inject(method = "pickBlock", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(value = "JUMP", opcode = 159, ordinal = 0))
      private void pickFromBackpack(CallbackInfo ci, @Local int i, @Local ItemStack itemstack, @Local Inventory inventory) {
            if (i != -1)
                  return;

            BackpackTraits.runIfEquipped(player, (traits, slot) -> {
                  traits.pickBlockClient(player, slot, inventory, itemstack, ci);
                  return ci.isCancelled();
            });
      }
}
