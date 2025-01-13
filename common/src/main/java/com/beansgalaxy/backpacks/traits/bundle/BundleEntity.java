package com.beansgalaxy.backpacks.traits.bundle;

import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BundleEntity implements IEntityTraits<BundleLikeTraits> {
      public static final BundleEntity INSTANCE = new BundleEntity();

      @Override
      public InteractionResult interact(BackpackEntity backpackEntity, BundleLikeTraits traits, Player player, InteractionHand hand) {
            if (player.level().isClientSide)
                  BundleScreen.openScreen(player, backpackEntity.viewable, traits);

            return InteractionResult.SUCCESS;
      }

      @Override
      public void onBreak(BackpackEntity backpack, BundleLikeTraits traits, boolean dropItems) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return;

            Level level = backpack.level();
            double x = backpack.getX();
            double y = backpack.getY();
            double z = backpack.getZ();
            if (dropItems && !level.isClientSide) for (ItemStack stack : stacks) {
                  ItemEntity itementity = new ItemEntity(level, x, y, z, stack);
                  itementity.setDefaultPickUpDelay();
                  RandomSource random = backpack.getRandom();
                  double a = random.nextDouble() - 0.5;
                  double b = a * Math.abs(a);
                  double c = random.nextDouble() - 0.5;
                  double d = c * Math.abs(c);
                  Vec3 vec3 = new Vec3(b, Math.abs(a * c) + 0.5, d).scale(0.35);
                  itementity.setDeltaMovement(vec3);
                  level.addFreshEntity(itementity);
            }

            backpack.remove(ITraitData.ITEM_STACKS);
      }

      @Override
      public Container createHopperContainer(BackpackEntity backpack, BundleLikeTraits traits) {
            return new BundleHopper(backpack, traits);
      }
}
