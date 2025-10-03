package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.traits.abstract_traits.IProjectileTrait;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.traits.abstract_traits.MutableSlotSelector;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class QuiverTraits extends BundleLikeTraits implements IProjectileTrait, ISlotSelectorTrait, IDraggingTrait {
      public static final String NAME = "quiver";

      public QuiverTraits(ModSound sound, int size) {
            super(sound, size);
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public QuiverClient client() {
            return QuiverClient.INSTANCE;
      }


      @Override
      public boolean isFull(ComponentHolder holder) {
            List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
            if (stacks == null || stacks.isEmpty() || stacks.size() < size())
                  return false;

            for (ItemStack stack : stacks) {
                  int maxStackSize = stack.getMaxStackSize();
                  int count = stack.getCount();
                  if (maxStackSize != count)
                        return false;
            }

            return true;
      }

      public boolean pickupToBackpack(Player player, EquipmentSlot equipmentSlot, Inventory inventory, ItemStack backpack, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            Fraction fraction = stacks == null || stacks.isEmpty()
                        ? Fraction.ZERO
                        : Traits.getWeight(stacks);

            int i = Fraction.getFraction(size(), 1).compareTo(fraction);
            if (i > 0) {
                  MutableBundleLike<QuiverTraits> mutable = this.mutable(ComponentHolder.of(backpack));
                  if (mutable.addItem(stack) != null) {
                        cir.setReturnValue(true);
                        sound().toClient(player, ModSound.Type.INSERT, 1, 1);
                        mutable.push();

                        if (player instanceof ServerPlayer serverPlayer) {
                              List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(equipmentSlot, backpack));
                              ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                              serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                        }
                  }

                  return stack.isEmpty();
            }
            return false;
      }

      @Override
      public boolean overflowFromInventory(EquipmentSlot equipmentSlot, Player player, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            ItemStack backpack = player.getItemBySlot(equipmentSlot);
            if (isFull(backpack))
                  return false;
            else
                  return super.overflowFromInventory(equipmentSlot, player, stack, cir);
      }

      @Override
      public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            return canInsertProjectile(inserted.getItem()) && super.canItemFit(holder, inserted);
      }

      public boolean canInsertProjectile(Item item) {
            return item instanceof ArrowItem;
      }

      @Override
      public MutableSlotSelector<QuiverTraits> mutable(ComponentHolder holder) {
            return new MutableSlotSelector<>(this, holder);
      }

      @Override
      public int getSelectedSlot(ComponentHolder holder, Player instance) {
            return ISlotSelectorTrait.super.getSelectedSlot(holder, instance);
      }

      @Override
      public String toString() {
            return "QuiverTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.QUIVER;
      }
}
