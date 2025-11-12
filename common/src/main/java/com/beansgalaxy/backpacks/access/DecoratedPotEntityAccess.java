package com.beansgalaxy.backpacks.access;

import com.beansgalaxy.backpacks.components.BulkComponent;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public interface DecoratedPotEntityAccess {
      int SIZE = 108;
      
      default Fraction getWeight() {
            BulkComponent bulk = getBulkComponent();
            if (bulk == null)
                  return Traits.getStackWeight(item().get());
            
            Fraction bulkWeight = bulk.weight();
            Fraction stackWeight = Traits.getStackWeight(item().get());
            
            return bulkWeight.add(stackWeight);
      }
      
      default float getFullness() {
            return getWeight().multiplyBy(Fraction.getFraction(1, SIZE)).floatValue();
      }
      
      default ItemStack takeFromFocus() {
            ItemStack returned = item().get();
            if (returned.isEmpty()) {
                  return ItemStack.EMPTY;
            }
            
            BulkComponent bulk = getBulkComponent();
            if (bulk == null) {
                  item().set(ItemStack.EMPTY);
                  return returned;
            }
            
            ArrayList<BulkComponent.ItemlessStack> stacks = new ArrayList<>(bulk.stacks());
            ItemStack stack = stacks.removeLast().withItem(bulk.item());
            item().set(stack);
            
            if (stacks.isEmpty()) {
                  setBulkComponent(null);
                  return returned;
            }
            
            tryFillFocusedItem(bulk.item(), stacks);
            return returned;
      }
      
      default boolean insertIntoFocus(ItemStack stack) {
            if (!stack.getItem().canFitInsideContainerItems())
                  return false;
            
            ItemStack focused = item().get();
            if (focused.isEmpty()) {
                  item().set(stack);
                  return true;
            }
            
            Holder<Item> item = focused.getItemHolder();
            if (!stack.is(item))
                  return false;
            
            BulkComponent bulk = getBulkComponent();
            ArrayList<BulkComponent.ItemlessStack> stacks = bulk == null
                  ? new ArrayList<>()
                  : new ArrayList<>(bulk.stacks());
            
            BulkComponent.ItemlessStack itemless = new BulkComponent.ItemlessStack(focused.getComponentsPatch(), focused.getCount());
            stacks.add(itemless);
            
            item().set(stack);
            tryFillFocusedItem(item, stacks);
            return true;
      }
      
      private void tryFillFocusedItem(Holder<Item> item, ArrayList<BulkComponent.ItemlessStack> stacks) {
            ItemStack focused = item().get();
            
            if (focused.isEmpty()) {
                  focused = stacks.removeLast().withItem(item);
                  item().set(focused);
                  setBulkComponent(new BulkComponent(item, ImmutableList.copyOf(stacks)));
            }
            
            int toAdd = focused.getMaxStackSize() - focused.getCount();
            if (toAdd == 0) {
                  setBulkComponent(new BulkComponent(item, ImmutableList.copyOf(stacks)));
                  return;
            }
            
            while (toAdd != 0) {
                  ItemStack stack = stacks.removeLast().withItem(item);
                  if (!ItemStack.isSameItemSameComponents(focused, stack))
                        return;
                  
                  int count = Math.min(stack.getCount(), toAdd);
                  stack.shrink(count);
                  focused.grow(count);
                  toAdd -= count;
                  
                  if (!stack.isEmpty()) {
                        BulkComponent.ItemlessStack newItemless = new BulkComponent.ItemlessStack(stack.getComponentsPatch(), stack.getCount());
                        stacks.add(newItemless);
                        setBulkComponent(new BulkComponent(item, ImmutableList.copyOf(stacks)));
                        return;
                  }
            }
            
            if (stacks.isEmpty())
                  setBulkComponent(null);
            else
                  setBulkComponent(new BulkComponent(item, ImmutableList.copyOf(stacks)));
      }
      
      static boolean attack(Level level, BlockPos pos, Player player) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DecoratedPotEntityAccess entity) {
                  BlockPos above = pos.above();
                  BlockState blockState = level.getBlockState(above);
                  if (blockState.isRedstoneConductor(level, above)) {
                        return true;
                  }
                  
                  ItemStack returned = entity.takeFromFocus();
                  if (returned.isEmpty())
                        return false;
                  
                  if (!level.isClientSide) {
                        double d2 = pos.getX() + 0.5;
                        double d3 = pos.getY() + 1.0;
                        double d4 = pos.getZ() + 0.5;
                        double d5 = Mth.nextDouble(level.random, -0.1, 0.1);
                        double d6 = 0.2;
                        double d7 = Mth.nextDouble(level.random, -0.1, 0.1);
                        
                        ItemEntity itementity = new ItemEntity(level, d2, d3, d4, returned, d5, d6, d7);
                        itementity.setPickUpDelay(0);
                        level.addFreshEntity(itementity);
                  }
                  
                  float fullness = entity.getFullness();
                  entity.success(level, pos, player, fullness);
                  return true;
            }
            return false;
      }
      
      static ItemInteractionResult useItemOn(
            ItemStack inHand,
            Level level,
            BlockPos pos,
            Player player
      ) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DecoratedPotEntityAccess entity) {
                  BlockPos above = pos.above();
                  BlockState blockState = level.getBlockState(above);
                  if (blockState.isRedstoneConductor(level, above)) {
                        return ItemInteractionResult.SUCCESS;
                  }
                  
                  int toAdd = entity.amountToAdd(inHand);
                  if (toAdd <= 0) {
                        entity.fail(level, pos, player);
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                  }
                  
                  ItemStack insert = inHand.copyWithCount(toAdd);
                  if (entity.insertIntoFocus(insert)) {
                        inHand.shrink(toAdd);
                        entity.success(inHand, level, pos, player, entity.getFullness());
                        return ItemInteractionResult.SUCCESS;
                  }
                  else entity.fail(level, pos, player);
            }
            
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
      }
      
      static InteractionResult useWithoutItem(
            Level level,
            BlockPos pos,
            Player player
      ) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DecoratedPotEntityAccess entity) {
                  BlockPos above = pos.above();
                  BlockState blockState = level.getBlockState(above);
                  if (blockState.isRedstoneConductor(level, above)) {
                        return InteractionResult.SUCCESS;
                  }
                  
                  ItemStack focused = entity.item().get();
                  if (focused.isEmpty())
                        return InteractionResult.PASS;
                  
                  Inventory inventory = player.getInventory();
                  Stream<ItemStack> aStream = Stream.concat(inventory.items.stream(), inventory.armor.stream());
                  Stream<ItemStack> bStream = Stream.of(player.getItemBySlot(EquipmentSlot.BODY));
                  Iterator<ItemStack> iterator = Stream.concat(aStream, bStream).iterator();
                  
                  while (iterator.hasNext()) {
                        ItemStack item = iterator.next();
                        
                        Optional<BundleLikeTraits> optional = BundleLikeTraits.get(ComponentHolder.of(item));
                        if (optional.isPresent()) {
                              BundleLikeTraits traits = optional.get();
                              MutableBundleLike<?> mutable = traits.mutable(ComponentHolder.of(item));
                              for (ItemStack stack : mutable.getItemStacks()) {
                                    int toAdd = entity.amountToAdd(stack);
                                    ItemStack copy = stack.copyWithCount(toAdd);
                                    if (entity.insertIntoFocus(copy)) {
                                          entity.success(stack, level, pos, player, entity.getFullness());
                                          stack.shrink(toAdd);
                                          mutable.push();
                                          return InteractionResult.SUCCESS;
                                    }
                              }
                        }
                        
                        int toAdd = entity.amountToAdd(item);
                        ItemStack copy = item.copyWithCount(toAdd);
                        if (entity.insertIntoFocus(copy)) {
                              entity.success(item, level, pos, player, entity.getFullness());
                              item.shrink(toAdd);
                              return InteractionResult.SUCCESS;
                        }
                  }
                  
                  
            }
            return InteractionResult.PASS;
      }
      
      static void onRemove(
            Level level,
            BlockPos pos
      ) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DecoratedPotEntityAccess entity) {
                  BulkComponent bulk = entity.getBulkComponent();
                  if (bulk == null)
                        return;
                  
                  Holder<Item> item = bulk.item();
                  for (BulkComponent.ItemlessStack stack : bulk.stacks()) {
                        ItemStack itemStack = stack.withItem(item);
                        Block.popResource(level, pos, itemStack);
                  }
                  
                  entity.setBulkComponent(null);
            }
      }
      
      static int getAnalogOutputSignal(Level level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof DecoratedPotEntityAccess entity) {
                  if (entity.item().get().isEmpty())
                        return 0;
                  
                  Fraction fraction = entity.getWeight().multiplyBy(Fraction.getFraction(13, SIZE));
                  return fraction.intValue() + 1;
            }
            
            return 0;
      }
      
      private int amountToAdd(ItemStack added) {
            Fraction size = Fraction.getFraction(SIZE, 1);
            Fraction weight = getWeight();
            Fraction weightLeft = size.subtract(weight);
            int i = weightLeft.divideBy(Traits.getItemWeight(added)).intValue();
            return Math.min(i, added.getCount());
      }
      
      private void fail(Level level, BlockPos pos, Player player) {
            level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1.0F, 1.0F);
            wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
      }
      
      private void success(ItemStack inHand, Level level, BlockPos pos, Player player, float fullness) {
            player.awardStat(Stats.ITEM_USED.get(inHand.getItem()));
            success(level, pos, player, fullness);
      }
      
      private void success(Level level, BlockPos pos, Player player, float fullness) {
            wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);
            
            level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * fullness);
            if (level instanceof ServerLevel serverlevel) {
                  Vec3 center = pos.getCenter();
                  serverlevel.sendParticles(ParticleTypes.DUST_PLUME, center.x, pos.getY() + 1.2, center.z, 7, 0.0, 0.0, 0.0, 0.0);
            }
            
            setChanged();
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
      }
      
      @Nullable
      BulkComponent getBulkComponent();
      
      void setBulkComponent(@Nullable BulkComponent bulk);
      
      SlotAccess item();
      
      void wobble(DecoratedPotBlockEntity.WobbleStyle style);
      
      void setChanged();
}
