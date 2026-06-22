package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.network.clientbound.SendItemComponentPatch;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.IMutableSelectionTrait;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.traits.generic.MutableChestLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.SlotSelectorData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class QuiverMutable extends MutableChestLike<QuiverTraits> implements IMutableSelectionTrait {
      private final SlotSelectorData selection;
      
      public QuiverMutable(QuiverTraits traits, ComponentHolder holder) {
            super(traits, holder);
            selection = new SlotSelectorData(holder, this::getItemStacks);
      }
      
      @Override
      public void push() {
            super.push();
            selection.push();
      }
      
      @Override
      public Boolean pickup(Player player, int slot, ItemStack backpack, ItemStack pickup, CallbackInfoReturnable<Boolean> cir) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            Fraction fraction = stacks == null || stacks.isEmpty()
                  ? Fraction.ZERO
                  : Traits.getWeight(stacks);
            
            int i = Fraction.getFraction(traits.size(), 1).compareTo(fraction);
            if (i <= 0)
                  return false;
            
            if (addItem(pickup) != null) {
                  cir.setReturnValue(true);
                  sound().toClient(player, ModSound.Type.INSERT, 1, 1);
                  push();
                  
                  if (player instanceof ServerPlayer serverPlayer)
                        SendItemComponentPatch.send(serverPlayer, slot, backpack);
            }
            
            return pickup.isEmpty();
      }
      
      @Override
      public boolean isAcceptableSelection(ItemStack stack) {
            return traits.canItemFit(holder, stack);
      }
      
      
      @Override
      public int getSelectedSlot(Player player) {
            return selection.getSelectedSlot(player);
      }
      
      @Override
      public void growSelectedSlot(int slot) {
            selection.growSelectedSlot(slot);
      }
      
      @Override
      public void limitSelectedSlot(int index) {
            selection.limitSelectedSlot(index, getItemStacks().size());
      }
      
      @Override
      public void setSelectedSlot(Player player, int slot) {
            selection.setSelectedSlot(player, slot);
      }
}
