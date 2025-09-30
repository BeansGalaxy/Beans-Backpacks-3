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
}
