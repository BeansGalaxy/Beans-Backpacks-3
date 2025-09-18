package com.beansgalaxy.backpacks.items;

import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.bundle.BundleCodecs;
import com.beansgalaxy.backpacks.traits.bundle.BundleTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;

public abstract class AbstractBurlapSackEntity extends BlockEntity implements Container {
      protected static final Component NAME = Component.literal("Burlap Sack");

      public AbstractBurlapSackEntity(BlockPos pPos, BlockState pBlockState) {
            super(Services.PLATFORM.getBurlapSackEntityType(), pPos, pBlockState);
      }

      @Override
      protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
            super.saveAdditional(pTag, pRegistries);
            DataResult<Tag> result = STACKS_CODEC.encodeStart(NbtOps.INSTANCE, stacks);
            result.ifSuccess(tag -> pTag.put("stacks", tag));

      }

      @Override
      protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
            super.loadAdditional(pTag, pRegistries);
            DataResult<List<ItemStack>> result = STACKS_CODEC.parse(NbtOps.INSTANCE, pTag.get("stacks"));
            result.ifSuccess(stacks -> {
                  this.stacks.clear();
                  this.stacks.addAll(stacks);
            });
      }

      public abstract void openMenu(Player player);

      private List<ItemStack> stacks = Lists.newArrayList();

      private static Codec<List<ItemStack>> STACKS_CODEC = Codec.list(RecordCodecBuilder.create((in) ->
                              in.group(
                                          ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                                          Codec.INT.fieldOf("count").forGetter(ItemStack::getCount),
                                          DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
                              ).apply(in, ItemStack::new)));

      public List<ItemStack> getItemStacks() {
            return stacks;
      }

      public int getSize() {
            return stacks.size();
      }

      public int getRemainingSpace(ItemStack input) {
            Fraction size = Fraction.getFraction(16, 1);
            Fraction weight = Traits.getWeight(getItemStacks());
            Fraction weightLeft = size.subtract(weight);
            return Math.max(weightLeft.divideBy(Traits.getItemWeight(input)).intValue(), 0);
      }

      public void addItem(ItemStack input) {
            addItem(getSize(), input);
      }

      public void addItem(int slot, ItemStack input) {
            addItem(slot, input, input.getCount());
      }

      public void addItem(int slot, ItemStack input, int increment) {
            if (input.isEmpty())
                  return;

            int space = getRemainingSpace(input);
            int toAdd = Math.min(increment, space);

            if (toAdd == 0)
                  return;

            int size = getSize();
            for (int i = 0; i < size; i++) {
                  ItemStack stored = getItemStacks().get(i);
                  if (!ItemStack.isSameItemSameComponents(input, stored))
                        continue;

                  stored.grow(toAdd);
                  input.shrink(toAdd);

                  getItemStacks().remove(i);

                  int min = Math.min(size - 1, slot);
                  getItemStacks().add(min, stored);
                  setChanged();
                  return;
            }

            int min = Math.min(size, slot);
            getItemStacks().add(min, input.copyWithCount(toAdd));
            input.shrink(toAdd);
            setChanged();
      }

      @Override
      public ItemStack removeItem(int slot, int amount) {
            int size = getSize();
            if (slot >= size)
                  return ItemStack.EMPTY;

            ItemStack stack = getItemStacks().get(slot);

            amount = Math.min(stack.getMaxStackSize(), amount);
            int count = stack.getCount();
            if (count <= amount) {
                  ItemStack removed = getItemStacks().remove(slot);
                  setChanged();
                  return removed;
            }

            ItemStack removed = stack.copyWithCount(amount);
            stack.shrink(amount);
            return removed;
      }

      @Override
      public ItemStack removeItemNoUpdate(int slot) {
            return getSize() > slot ? getItemStacks().remove(slot) : ItemStack.EMPTY;
      }

      @Override
      public void setItem(int slot, ItemStack stack) {
            int size = getSize();
            boolean isEmpty = stack.isEmpty();

            if (!isEmpty) {
                  if (size > slot)
                        getItemStacks().set(slot, stack);
                  else
                        getItemStacks().add(size, stack);
            }
            else if (size > slot)
                  getItemStacks().remove(slot);

            setChanged();
      }

      @Override
      public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(this, player);
      }

      @Override
      public int getContainerSize() {
            int i = 1 - Traits.getWeight(stacks, 16).intValue();
            int value = getSize() + i;
            return value;
      }

      @Override
      public boolean canPlaceItem(int pSlot, ItemStack pStack) {
            return Traits.getWeight(stacks, 16).intValue() != 1;
      }

      @Override
      public boolean isEmpty() {
            return stacks.isEmpty();
      }

      @Override
      public ItemStack getItem(int slot) {
            return getSize() > slot ? getItemStacks().get(slot) : ItemStack.EMPTY;
      }

      public void dropAll() {
            Containers.dropContents(level, getBlockPos(), this);
      }

      @Override
      public void clearContent() {

      }
}
