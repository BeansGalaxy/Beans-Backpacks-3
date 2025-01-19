package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.data.config.CommonConfig;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
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
      @Shadow public abstract PlayerAdvancements getAdvancements();

      public ServerPlayerMixin(Level pLevel, BlockPos pPos, float pYRot, GameProfile pGameProfile) {
            super(pLevel, pPos, pYRot, pGameProfile);
      }

      @Inject(method = "restoreFrom", at = @At(value = "FIELD", ordinal = 1, target = "Lnet/minecraft/server/level/ServerPlayer;enchantmentSeed:I"))
      private void backpackRestoreFrom(ServerPlayer that, boolean pKeepEverything, CallbackInfo ci) {
            if (pKeepEverything || level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) || that.isSpectator())
                  return;

            CommonConfig config = ServerSave.CONFIG;
            if (config.keep_back_on_death.get()) {
                  ItemStack backpack = that.getItemBySlot(EquipmentSlot.BODY);
                  this.setItemSlot(EquipmentSlot.BODY, backpack);
            }

            if (config.keep_shorthand_on_death.get()) {
                  Shorthand shorthand = Shorthand.get(this);
                  shorthand.replaceWith(Shorthand.get(that));

            }
      }

      @Inject(method = "giveExperienceLevels", at = @At("TAIL"))
      private void backpacks_experienceLevels(int pLevels, CallbackInfo ci) {
            if (experienceLevel > 30) {
                  AdvancementHolder holder = getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shorthand/level_30"));
                  if (holder == null)
                        return;

                  PlayerAdvancements playerAdvancements = getAdvancements();
                  playerAdvancements.award(holder, "level");
            }
      }
}
