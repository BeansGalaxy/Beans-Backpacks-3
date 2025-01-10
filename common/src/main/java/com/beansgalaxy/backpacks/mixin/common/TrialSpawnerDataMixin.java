package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(TrialSpawnerData.class)
public class TrialSpawnerDataMixin {

      @Inject(method = "tryDetectPlayers", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isPresent()Z"))
      private void backpacks_detectPlayers(ServerLevel pLevel, BlockPos pPos, TrialSpawner pSpawner, CallbackInfo ci, @Local Optional<Pair<Player, Holder<MobEffect>>> optional, @Local List<UUID> list) {
            if (optional.isPresent())
                  return;


            for (UUID uuid : list) {
                  Player player = pLevel.getPlayerByUUID(uuid);
                  if (player != null) {
                        boolean consumed = UtilityComponent.consumeOminous(player, () ->
                                    player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, 18000, 0, true, true))
                        );

                        if (consumed)
                              return;
                  }
            }
      }

}
