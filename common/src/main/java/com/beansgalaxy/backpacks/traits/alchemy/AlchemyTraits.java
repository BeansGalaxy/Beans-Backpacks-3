package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

public class AlchemyTraits extends BundleLikeTraits {
      public static final String NAME = "alchemy";

      public AlchemyTraits(ModSound sound, int size) {
            super(sound, size);
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public AlchemyClient client() {
            return AlchemyClient.INSTANCE;
      }

      @Override
      public AlchemyEntity entity() {
            return AlchemyEntity.INSTANCE;
      }

      @Override
      public boolean canItemFit(ComponentHolder holder, ItemStack inserted) {
            Item item = inserted.getItem();
            boolean isPotion = item instanceof PotionItem || Items.HONEY_BOTTLE.equals(item) || Items.MILK_BUCKET.equals(item);
            return isPotion && super.canItemFit(holder, inserted);
      }

      @Override
      public AlchemyMutable mutable(ComponentHolder holder) {
            return new AlchemyMutable(this, holder);
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            if (isEmpty(holder))
                  return;

            AlchemyMutable mutable = mutable(holder);
            int selectedSlot = getSelectedSlotSafe(holder, player);
            ItemStack selected = mutable.getItemStacks().get(selectedSlot);
            Item item = selected.getItem();

            if (Items.HONEY_BOTTLE.equals(item)) {
                  if (!player.canEat(false))
                        return;

                  FoodProperties foodproperties = selected.get(DataComponents.FOOD);
                  if (foodproperties == null) {
                        player.drop(selected, true);
                        mutable.push();
                        ItemStack backpack = player.getItemInHand(hand);
                        cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide));
                        return;
                  }

                  player.eat(level, selected, foodproperties);
                  player.playSound(SoundEvents.GLASS_BREAK);
                  if (player instanceof ServerPlayer serverplayer) {
                        CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, selected);
                        serverplayer.awardStat(Stats.ITEM_USED.get(item));
                        player.removeEffect(MobEffects.POISON);
                  }

                  BlockState blockstate = Blocks.HONEY_BLOCK.defaultBlockState();
                  BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, blockstate);
                  double eyeY = player.getBoundingBox().maxY;
                  double lowY = (player.getY() + eyeY + eyeY) * 0.334;
                  double rad = Math.PI / 180;
                  double yRot = -player.getYHeadRot() * rad;
                  double xRot = -player.getXRot() * rad;
                  double x = player.getX() + Math.sin(yRot) * 0.4 * Math.cos(xRot);
                  double z = player.getZ() + Math.cos(yRot) * 0.4 * Math.cos(xRot);
                  double yO = Math.sin(xRot) * 0.3;

                  for (int j = 0; j < 5; j++) {
                        double random = level.random.nextDouble();
                        double y = Mth.lerp(random * random, lowY, eyeY) + yO;
                        double xySpeed = (random - 0.5);

                        level.addParticle(particleOption, x, y, z, xySpeed, -random * 0.2, xySpeed);
                  }
            }
            else if (Items.MILK_BUCKET.equals(item))
                  useMilkBucketItem(level, player, item, selected);
            else
                  usePotionLikeItem(level, player, selected, item);

            int size = mutable.getItemStacks().size();
            limitSelectedSlot(holder, selectedSlot, size);
            mutable.push();
            ItemStack backpack = player.getItemInHand(hand);
            cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide));
      }

      private static void useMilkBucketItem(Level level, Player player, Item item, ItemStack selected) {
            item.finishUsingItem(selected, level, player);
            player.addItem(Items.BUCKET.getDefaultInstance());
            player.playSound(SoundEvents.PLAYER_SPLASH_HIGH_SPEED);
            for (int j = 0; j < 4; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                  double centered = random - 0.5;
                  double xySpeed = centered * .1;
                  double centered1 = level.random.nextDouble() - 0.5;
                  double centered2 = level.random.nextDouble() - 0.5;
                  level.addParticle(ParticleTypes.SNOWFLAKE, player.getX() + centered1, y, player.getZ() + centered2, xySpeed, -random * 0.2, xySpeed);
            }

            ColorParticleOption particleOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(100, 0xFFFFFF));
            for (int j = 0; j < 10; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                  double xySpeed = (random - 0.5) * .1;
                  level.addParticle(particleOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
            }

            ColorParticleOption alphaOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(200, 0xFFFFFF));
            for (int j = 0; j < 8; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(random, player.getY(), player.getEyeY());
                  double centered = random - 0.5;
                  double xySpeed = centered * 10;
                  level.addParticle(alphaOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);

                  double centered1 = level.random.nextDouble() - 0.5;
                  double centered2 = level.random.nextDouble() - 0.5;
                  level.addParticle(ParticleTypes.WHITE_SMOKE, player.getX() + centered1, y, player.getZ() + centered2, 0, -random * 0.2, 0);
            }
      }

      private static void usePotionLikeItem(Level level, Player player, ItemStack selected, Item item) {
            PotionContents potioncontents = selected.get(DataComponents.POTION_CONTENTS);
            if (potioncontents != null) {
                  Optional<Holder<Potion>> potion = potioncontents.potion();
                  Boolean waterLike = potion.map(holder ->
                              Potions.AWKWARD.equals(holder) ||
                                          Potions.MUNDANE.equals(holder) ||
                                          Potions.THICK.equals(holder) ||
                                          Potions.WATER.equals(holder)
                  ).orElse(false);

                  if (waterLike) {
                        player.extinguishFire();
                        player.playSound(SoundEvents.PLAYER_SPLASH);

                        for (int j = 0; j < 4; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                              double centered = random - 0.5;
                              double xySpeed = centered * .1;
                              double centered1 = level.random.nextDouble() - 0.5;
                              double centered2 = level.random.nextDouble() - 0.5;
                              level.addParticle(ParticleTypes.FALLING_WATER, player.getX() + centered1, y, player.getZ() + centered2, xySpeed, -random * 0.2, xySpeed);
                        }

                        ColorParticleOption particleOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(100, -13083194));
                        for (int j = 0; j < 10; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                              double xySpeed = (random - 0.5) * .1;
                              level.addParticle(particleOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                              level.addParticle(ParticleTypes.SPLASH, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                        }

                        ColorParticleOption alphaOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(200, -13083194));
                        for (int j = 0; j < 8; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(random * random, player.getY(), player.getEyeY());
                              double centered = random - 0.5;
                              double xySpeed = centered * 10;
                              level.addParticle(alphaOption, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                              level.addParticle(ParticleTypes.SPLASH, player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                        }

                  }
                  else potioncontents.forEachEffect(effect -> {
                        for (int j = 0; j < 20; j++) {
                              double random = level.random.nextDouble();
                              double y = Mth.lerp(random * random, player.getY(), player.getEyeY());
                              double xySpeed = (random - 0.5);
                              level.addParticle(effect.getParticleOptions(), player.getX(), y, player.getZ(), xySpeed, -random * 0.2, xySpeed);
                        }
                  });
            }

            item.finishUsingItem(selected, level, player);
            player.playSound(SoundEvents.GLASS_BREAK);
      }

      @Override
      public String toString() {
            return "AlchemyTraits{" +
                        "size=" + size() +
                        ", sound=" + sound() +
                        '}';
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.ALCHEMY;
      }
}
