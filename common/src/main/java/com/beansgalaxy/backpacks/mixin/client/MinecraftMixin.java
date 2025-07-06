package com.beansgalaxy.backpacks.mixin.client;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.data.EnderStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements MinecraftAccessor {
      @Shadow static Minecraft instance;
      @Shadow @Nullable public LocalPlayer player;
      @Shadow public ClientLevel level;

      @Shadow @Nullable public HitResult hitResult;
      @Unique public final EnderStorage beans_Backpacks_2$enderStorage = new EnderStorage();

      @Override
      public EnderStorage beans_Backpacks_2$getEnder() {
            return beans_Backpacks_2$enderStorage;
      }

      @Inject(method = "startUseItem", cancellable = true, at = @At(value = "INVOKE",
                  ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;getCount()I"))
      private void hotkeyUseItemOn(CallbackInfo ci) {
            if(BackData.get(player).isActionKeyDown() && KeyPress.INSTANCE.consumeActionUseOn(instance, (BlockHitResult) instance.hitResult))
                  ci.cancel();
      }

      @Inject(method = "startUseItem", cancellable = true, at = @At(value = "FIELD", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/InteractionResult;FAIL:Lnet/minecraft/world/InteractionResult;"))
      private void tryCoyoteClick(CallbackInfo ci) {
            if (KeyPress.INSTANCE.tryCoyoteClick(player, (BlockHitResult) instance.hitResult))
                  ci.cancel();
      }

      @Inject(method = "handleKeybinds", at = @At("TAIL"))
      private void handleBackpackKeybinds(CallbackInfo ci) {
            CommonClient.handleKeyBinds(player, hitResult);
      }
}
