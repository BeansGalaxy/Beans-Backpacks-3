package com.beansgalaxy.backpacks.traits.lunch_box;

import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.bundle.BundleEntity;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class LunchBoxEntity extends BundleEntity {
      public static final LunchBoxEntity INSTANCE = new LunchBoxEntity();

      @Override
      public InteractionResult interact(BackpackEntity backpackEntity, BundleLikeTraits traits, Player player, InteractionHand hand) {
            if (player.level().isClientSide)
                  LunchBoxScreen.openScreen(backpackEntity.viewable, traits, player);

            return InteractionResult.SUCCESS;
      }

      @Override
      public void onBreak(BackpackEntity backpack, BundleLikeTraits traits, boolean dropItems) {
            super.onBreak(backpack, traits, dropItems);

            List<ItemStack> stacks = backpack.get(ITraitData.NON_EDIBLES);
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

            backpack.remove(ITraitData.NON_EDIBLES);
      }
}
