package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.Traits;
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
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class TinyMenuClick implements Packet2S {
      private final int entityId;
      private final int index;
      private final TinyClickType clickType;

      public TinyMenuClick(RegistryFriendlyByteBuf buf) {
            this.entityId = buf.readInt();
            this.index = buf.readInt();
            this.clickType = buf.readEnum(TinyClickType.class);
      }

      public TinyMenuClick(int entityId, int index, TinyClickType clickType) {
            this.entityId = entityId;
            this.index = index;
            this.clickType = clickType;
      }

      public static void send(ViewableBackpack backpack, int index, TinyClickType clickType) {
            new TinyMenuClick(backpack.getId(), index, clickType).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.TINY_MENU_2S;
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
                  Optional<GenericTraits> optional = backpack.getTraits();
                  if (optional.isPresent() && optional.get() instanceof ItemStorageTraits bundleLikeTraits) {
                        InventoryMenu menu = sender.inventoryMenu;
                        SlotAccess carriedAccess = new SlotAccess() {
                              public ItemStack get() {
                                    return menu.getCarried();
                              }

                              public boolean set(ItemStack p_150452_) {
                                    menu.setCarried(p_150452_);
                                    return true;
                              }
                        };

                        bundleLikeTraits.tinyMenuClick(backpack, index, clickType, carriedAccess, sender);
                  }
            }
            else {
                  LivingEntity owner;
                  if (entity instanceof ViewableAccessor)
                        owner = (LivingEntity) entity;
                  else return;

                  ItemStack backpack = owner.getItemBySlot(EquipmentSlot.BODY);
                  Optional<GenericTraits> optional = Traits.get(backpack);
                  if (optional.isPresent() && optional.get() instanceof ItemStorageTraits storageTraits) {
                        InventoryMenu menu = sender.inventoryMenu;
                        SlotAccess carriedAccess = new SlotAccess() {
                              public ItemStack get() {
                                    return menu.getCarried();
                              }

                              public boolean set(ItemStack p_150452_) {
                                    menu.setCarried(p_150452_);
                                    return true;
                              }
                        };

                        storageTraits.tinyMenuClick(ComponentHolder.of(backpack), index, clickType, carriedAccess, sender);
                  }
            }
      }

      public static Type<TinyMenuClick> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":tiny_menu_click_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }

}
