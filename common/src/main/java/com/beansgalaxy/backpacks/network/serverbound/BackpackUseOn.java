package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

public class BackpackUseOn implements Packet2S {
      public static final Type<BackpackUseOn> ID = new Type<>(ResourceLocation.parse(Constants.MOD_ID + ":place_backpack_s"));
      private final BlockHitResult blockHitResult;
      private final EquipmentSlot equipmentSlot;

      public BackpackUseOn(RegistryFriendlyByteBuf buf) {
            this(buf.readBlockHitResult(), buf.readEnum(EquipmentSlot.class));
      }

      private BackpackUseOn(BlockHitResult blockHitResult, EquipmentSlot slot) {
            this.blockHitResult = blockHitResult;
            this.equipmentSlot = slot;
      }

      public static void send(BlockHitResult hitResult, EquipmentSlot slot) {
            new BackpackUseOn(hitResult, slot).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.PLACE_BACKPACK_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeBlockHitResult(blockHitResult);
            buf.writeEnum(equipmentSlot);
      }

      @Override
      public void handle(Player sender) {
            ItemStack backpack = sender.getItemBySlot(equipmentSlot);
            BackpackTraits traits = BackpackTraits.get(backpack);
            if (traits == null) {
                  return;
            }

            placeBackpack(sender, blockHitResult, backpack, traits);
      }

      public static boolean placeBackpack(Player player, BlockHitResult hitResult, ItemStack backpack, BackpackTraits traits) {
            UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hitResult);
            BackpackEntity entity = BackpackEntity.create(context, backpack, traits);
            if (entity == null) {
                  return false;
            }

            traits.sound().at(entity, ModSound.Type.PLACE);
            return true;
      }

      @Override
      public Type<BackpackUseOn> type() {
            return ID;
      }
}
