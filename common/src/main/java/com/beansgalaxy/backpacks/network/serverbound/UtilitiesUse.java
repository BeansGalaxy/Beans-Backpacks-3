package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.network.Network2S;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class UtilitiesUse implements Packet2S {
      private final Kind kind;

      private UtilitiesUse(Kind kind) {
            this.kind = kind;
      }

      public UtilitiesUse(RegistryFriendlyByteBuf buf) {
            this(buf.readEnum(Kind.class));
      }

      public static void sendRocket() {
            new UtilitiesUse(Kind.ROCKET).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.UTILITY_ROCKET_USE;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeEnum(kind);
      }

      @Override
      public void handle(Player pPlayer) {
            switch (kind) {
                  case ROCKET -> useRocket(pPlayer);
            }
      }

      private static void useRocket(Player pPlayer) {
            if (!pPlayer.isFallFlying())
                  return;

            UtilityComponent.testItems(pPlayer, (itemstack, mute) -> {
                  Item rocket = Items.FIREWORK_ROCKET;
                  if (!itemstack.is(rocket))
                        return false;

                  Level pLevel = pPlayer.level();
                  FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(pLevel, itemstack, pPlayer);
                  pLevel.addFreshEntity(fireworkrocketentity);
                  itemstack.consume(1, pPlayer);
                  pPlayer.awardStat(Stats.ITEM_USED.get(rocket));
                  mute.freeze();
                  return true;
            });
      }

      public static Type<UtilitiesUse> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":utility_rocket_use_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }

      private enum Kind {
            ROCKET,
      }
}
