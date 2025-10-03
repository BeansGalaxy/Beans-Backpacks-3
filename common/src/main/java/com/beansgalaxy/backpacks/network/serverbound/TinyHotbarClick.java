package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TinyHotbarClick implements Packet2S {
      private final int entityId;
      private final int index;
      private final TinyClickType clickType;

      public TinyHotbarClick(RegistryFriendlyByteBuf buf) {
            this.entityId = buf.readInt();
            this.index = buf.readInt();
            clickType = buf.readEnum(TinyClickType.class);
      }

      public TinyHotbarClick(int entityId, int index, TinyClickType clickType) {
            this.entityId = entityId;
            this.index = index;
            this.clickType = clickType;
      }

      public static void send(ViewableBackpack backpack, int index, TinyClickType clickType) {
            new TinyHotbarClick(backpack.getId(), index, clickType).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TINY_HOTBAR_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeInt(entityId);
            buf.writeInt(index);
            buf.writeEnum(clickType);
      }

      @Override
      public void handle(Player sender) {
            Entity entity = sender.level().getEntity(entityId);
            if (entity instanceof BackpackEntity backpack) {
                  IEntityTraits<?> traits = backpack.getTraits();
                  traits.tinyHotbarClick(backpack, index, clickType, sender.inventoryMenu, sender);
            }
            else {
                  LivingEntity owner;
                  if (entity instanceof ViewableAccessor)
                        owner = (LivingEntity) entity;
                  else return;

                  ItemStack backpack = owner.getItemBySlot(EquipmentSlot.BODY);
                  Optional<GenericTraits> optional = Traits.get(backpack);
                  if (optional.isPresent() && optional.get() instanceof ItemStorageTraits storageTraits) {
                        storageTraits.tinyHotbarClick(ComponentHolder.of(backpack), index, clickType, sender.inventoryMenu, sender);
                  }
            }
      }

      public static Type<TinyHotbarClick> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":tiny_hotbar_click_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
