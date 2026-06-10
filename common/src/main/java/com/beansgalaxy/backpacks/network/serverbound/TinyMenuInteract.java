package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class TinyMenuInteract implements Packet2S {
      private final int entity;
      private final boolean isOpen;

      private TinyMenuInteract(int entity, boolean isOpen) {
            this.entity = entity;
            this.isOpen = isOpen;
      }

      public TinyMenuInteract(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), buf.readBoolean());
      }

      public static void send(int entity, boolean isOpen) {
            new TinyMenuInteract(entity, isOpen).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TINY_INTERACT_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(entity);
            buf.writeBoolean(isOpen);
      }

      @Override
      public void handle(Player sender) {
            Entity entity = sender.level().getEntity(this.entity);
            if (entity instanceof BackpackEntity backpack) {
                  if (isOpen)
                        backpack.viewable.onOpen(sender);
                  else
                        backpack.viewable.onClose(sender);
            }
            else {
                  ViewableBackpack viewable;
                  if (entity instanceof ViewableAccessor accessor)
                        viewable = accessor.getViewable();
                  else return;

                  if (isOpen)
                        viewable.onOpen(sender);
                  else
                        viewable.onClose(sender);
            }
      }

      public static Type<TinyMenuInteract> ID = new Type<>(Constants.defaultLocation("tiny_menu_interact_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
