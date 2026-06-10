package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public class InstantKeyPress implements Packet2S {
      private final int entity;

      private InstantKeyPress(int entity) {
            this.entity = entity;
      }

      public InstantKeyPress(RegistryFriendlyByteBuf buf) {
            this(buf.readInt());
      }

      public static void send(int entity) {
            new InstantKeyPress(entity).send2S();
      }

      @Override public Network2S getNetwork() {
            return Network2S.INSTANT_KEY_2S;
      }

      @Override public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(entity);
      }

      @Override public void handle(Player sender) {
            Entity entity = sender.level().getEntity(this.entity);
            if (entity instanceof BackpackEntity backpack)
                  backpack.tryEquip(sender);
            else if (entity instanceof ArmorStand armorStand)
                  CommonClass.swapBackWith(armorStand, sender);
            else if (entity instanceof Allay allay)
                  CommonClass.swapBackWith(allay, sender);
      }

      public static Type<InstantKeyPress> ID = new Type<>(Constants.defaultLocation("instant_key_press_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
