package com.beansgalaxy.backpacks.traits.alchemy;

import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.IDraggingTrait;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.ChestLikeTraits;
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
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

public class AlchemyTraits extends ChestLikeTraits implements ISlotSelectorTrait, IDraggingTrait {
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
            int selectedSlot = mutable.getSelectedSlot(player);
            ItemStack selected = mutable.getItemStacks().get(selectedSlot);
            Item item = selected.getItem();

            if (Items.HONEY_BOTTLE.equals(item)) {
                  if (!player.canEat(false))
                        return;

                  FoodProperties foodproperties = selected.get(DataComponents.FOOD);
                  if (foodproperties == null)
                        return;

                  useHoneyBottle(level, player, selected, foodproperties, item);
            }
            else if (Items.MILK_BUCKET.equals(item))
                  useMilkBucketItem(mutable, level, player, item, selected);
            else {
                  PotionContents potioncontents = selected.get(DataComponents.POTION_CONTENTS);
                  if (potioncontents != null)
                        usePotionLikeItem(potioncontents, level, player, selected, item);
                  else return;
            }

            if (selected.isEmpty())
                  mutable.limitSelectedSlot(selectedSlot);

            mutable.push();
            ItemStack backpack = player.getItemInHand(hand);
            cir.setReturnValue(InteractionResultHolder.sidedSuccess(backpack, level.isClientSide));
      }

      private static void useHoneyBottle(Level level, Player player, ItemStack selected, FoodProperties foodproperties, Item item) {
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
            double x = player.getX() + Math.sin(yRot) * 0.5 * Math.cos(xRot);
            double z = player.getZ() + Math.cos(yRot) * 0.5 * Math.cos(xRot);
            double yO = Math.sin(xRot) * 0.3 - 0.2;

            Vec3 movement = player.getDeltaMovement().multiply(2, 1, 2);

            for (int j = 0; j < 5; j++) {
                  double random = level.random.nextDouble();
                  double y = Mth.lerp(random * random, lowY, eyeY) + yO;
                  double xySpeed = (random - 0.5);

                  double xSpeed = movement.x + xySpeed;
                  double ySpeed = movement.y - random * 0.4;
                  double zSpeed = movement.z + xySpeed;

                  level.addParticle(particleOption, x, y, z, xSpeed, ySpeed, zSpeed);
            }
      }

      private static void useMilkBucketItem(AlchemyMutable mutable, Level level, Player player, Item item, ItemStack selected) {
            ItemStack consumedStack = item.finishUsingItem(selected.copyWithCount(1), level, player);

            if (!player.hasInfiniteMaterials()) {
                  selected.shrink(1);
                  List<ItemStack> itemStacks = mutable.getItemStacks();
                  if (!consumedStack.isEmpty()) {
                        for (int i = 0; i < itemStacks.size(); i++) {
                              ItemStack nonEdible = itemStacks.get(i);
                              if (ItemStack.isSameItemSameComponents(nonEdible, consumedStack)) {
                                    ItemStack removed = itemStacks.remove(i);
                                    consumedStack.grow(removed.getCount());
                                    mutable.limitSelectedSlot(i);
                              }
                        }

                        if (!consumedStack.isEmpty()) {
                              itemStacks.addFirst(consumedStack);
                              mutable.selection().set(player, mutable.getSelectedSlot(player));

                              mutable.growSelectedSlot(0);
                        }
                  }
            }

            player.playSound(SoundEvents.PLAYER_SPLASH);

            Vec3 movement = player.getDeltaMovement().multiply(1.75, 0.5, 1.75);

            for (int j = 0; j < 6; j++) {
                  double random = level.random.nextDouble();
                  double centered = random - 0.5;
                  double xySpeed = centered * (Math.abs(movement.x) + Math.abs(movement.z)) * 0.5;
                  double xSpeed = movement.x + xySpeed;
                  double ySpeed = movement.y;
                  double zSpeed = movement.z + xySpeed;
                  double centered1 = level.random.nextDouble() - 0.5;
                  double centered2 = level.random.nextDouble() - 0.5;

                  double top = player.getBbHeight() + player.getY();
                  level.addParticle(ParticleTypes.SNOWFLAKE, player.getX() + centered1, top, player.getZ() + centered2, xSpeed + xySpeed, ySpeed, zSpeed + xySpeed);

                  double y = Mth.lerp(random, player.getY(), player.getEyeY());
                  level.addParticle(ParticleTypes.WHITE_SMOKE, player.getX() + centered2, y, player.getZ() + centered1, xSpeed, ySpeed, zSpeed);
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
            }
      }

      private static void usePotionLikeItem(PotionContents potioncontents, Level level, Player player, ItemStack selected, Item item) {
            Optional<Holder<Potion>> potion = potioncontents.potion();
            Boolean waterLike = potion.map(holder ->
                        Potions.AWKWARD.equals(holder) ||
                                    Potions.MUNDANE.equals(holder) ||
                                    Potions.THICK.equals(holder) ||
                                    Potions.WATER.equals(holder)
            ).orElse(false);

            Vec3 movement = player.getDeltaMovement().multiply(2, 0.5, 2);

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

                        double xSpeed = movement.x + xySpeed;
                        double ySpeed = movement.y - random * 0.2;
                        double zSpeed = movement.z + xySpeed;

                        level.addParticle(ParticleTypes.FALLING_WATER, player.getX() + centered1, y, player.getZ() + centered2, xSpeed, ySpeed, zSpeed);
                  }

                  ColorParticleOption particleOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(100, -13083194));
                  for (int j = 0; j < 10; j++) {
                        double random = level.random.nextDouble();
                        double y = Mth.lerp(Math.sqrt(random), player.getY(), player.getEyeY());
                        double xySpeed = (random - 0.5) * .1;

                        double xSpeed = movement.x + xySpeed;
                        double ySpeed = movement.y - random * 0.2;
                        double zSpeed = movement.z + xySpeed;

                        level.addParticle(particleOption, player.getX(), y, player.getZ(), xSpeed, ySpeed, zSpeed);
                        level.addParticle(ParticleTypes.SPLASH, player.getX(), y, player.getZ(), xSpeed, ySpeed, zSpeed);
                  }

                  ColorParticleOption alphaOption = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(200, -13083194));
                  for (int j = 0; j < 8; j++) {
                        double random = level.random.nextDouble();
                        double y = Mth.lerp(random * random, player.getY(), player.getEyeY());
                        double centered = random - 0.5;
                        double xySpeed = centered * 10;

                        double xSpeed = movement.x + xySpeed;
                        double ySpeed = movement.y - random * 0.2;
                        double zSpeed = movement.z + xySpeed;

                        level.addParticle(alphaOption, player.getX(), y, player.getZ(), xSpeed, ySpeed, zSpeed);
                        level.addParticle(ParticleTypes.SPLASH, player.getX(), y, player.getZ(), xSpeed, ySpeed, zSpeed);
                  }

            }
            else potioncontents.forEachEffect(effect -> {
                  for (int j = 0; j < 20; j++) {
                        double random = level.random.nextDouble();
                        double y = Mth.lerp(random * random, player.getY(), player.getEyeY());
                        double xySpeed = (random - 0.5);

                        double xSpeed = movement.x + xySpeed;
                        double ySpeed = movement.y - random * 0.2;
                        double zSpeed = movement.z + xySpeed;

                        level.addParticle(effect.getParticleOptions(), player.getX(), y, player.getZ(), xSpeed, ySpeed, zSpeed);
                  }
            });

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
