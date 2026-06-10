package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.data.config.CommonConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
      @Shadow public abstract ServerLevel level();
      
      public ServerPlayerMixin(Level level, GameProfile gameProfile) {
            super(level, gameProfile);
      }
      
      @Inject(method = "restoreFrom", at = @At(value = "FIELD", ordinal = 1, target = "Lnet/minecraft/server/level/ServerPlayer;enchantmentSeed:I"))
      private void backpackRestoreFrom(ServerPlayer that, boolean pKeepEverything, CallbackInfo ci) {
            CommonConfig config = ServerSave.CONFIG;
            if (pKeepEverything || config.keepBackpack(level())) {
                  ItemStack backpack = that.getItemBySlot(EquipmentSlot.BODY);
                  this.setItemSlot(EquipmentSlot.BODY, backpack);
            }
      }
}
