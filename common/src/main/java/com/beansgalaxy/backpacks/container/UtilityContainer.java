package com.beansgalaxy.backpacks.container;

import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.traits.ITraitData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class UtilityContainer implements Container {
      private final BackData owner;
      public byte size;

      public UtilityContainer(BackData owner) {
            this.owner = owner;
      }

      public static UtilityContainer get(Player player) {
            BackData backData = BackData.get(player);
            return backData.getUtility();
      }

      @Override
      public int getContainerSize() {
            return size;
      }

      @Override
      public boolean isEmpty() {
            return getUtility().isEmpty();
      }

      @Override
      public ItemStack getItem(int i) {
            return getUtility().get(i);
      }

      @Override
      public ItemStack removeItem(int i, int amount) {
            return mapMutable(mute -> {
                  ItemStack itemStack = mute.slots.get(i);
                  if (itemStack.isEmpty()) {
                        return mute.slots.remove(i);
                  }

                  int count = itemStack.getCount();
                  if (count <= amount) {
                        return mute.slots.remove(i);
                  }

                  ItemStack copy = itemStack.copyWithCount(amount);
                  itemStack.shrink(amount);
                  return copy;
            }, ItemStack.EMPTY);
      }

      @Override
      public ItemStack removeItemNoUpdate(int i) {
            return mapMutable(mute -> mute.slots.remove(i), ItemStack.EMPTY);
      }

      @Override
      public void setItem(int i, ItemStack stack) {
            getMutable().ifPresent(mute -> {
                  mute.slots.put(i, stack);
                  mute.freeze();
            });
      }

      private Optional<UtilityComponent.Mutable> getMutable() {
            ItemStack backpack = owner.beans_Backpacks_3$getBody().getFirst();
            if (backpack.isEmpty())
                  return Optional.empty();

            return UtilityComponent.get(backpack);
      }

      private <T> T mapMutable(Function<UtilityComponent.Mutable, T> map, T orElse) {
            Optional<UtilityComponent.Mutable> mutable = getMutable();
            if (mutable.isEmpty())
                  return orElse;

            UtilityComponent.Mutable mute = mutable.get();
            T apply = map.apply(mute);
            mute.freeze();
            return apply;
      }

      private UtilityComponent getUtility() {
            ItemStack backpack = owner.beans_Backpacks_3$getBody().getFirst();
            return backpack.getOrDefault(ITraitData.UTILITIES, UtilityComponent.BLANK);
      }

      @Override
      public void setChanged() {

      }

      @Override
      public boolean stillValid(Player player) {
            return true;
      }

      @Override
      public void clearContent() {
            ItemStack backpack = owner.beans_Backpacks_3$getBody().getFirst();
            backpack.remove(ITraitData.UTILITIES);
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UtilityContainer container)) return false;
            return size == container.size && Objects.equals(owner, container.owner);
      }

      @Override
      public int hashCode() {
            return Objects.hash(owner, size);
      }


      private static int EFFECT_COOLDOWN = 40;
      private int effectCooldown = 0;
      public void tick(Inventory inventory) {
            if (effectCooldown > 0) {
                  effectCooldown--;
                  return;
            }

            Player player = inventory.player;

            Optional<UtilityComponent.Mutable> optionalMute = getMutable();
            if (optionalMute.isEmpty())
                  return;

            UtilityComponent.Mutable mutable = optionalMute.get();

            ItemStack first = mutable.slots.get(0);
            ItemStack second = mutable.slots.get(1);
            UtilityComponent.Type firstType = UtilityComponent.getType(first);
            UtilityComponent.Type secondType = UtilityComponent.getType(second);

            if (player.isInWaterOrBubble() || player.level().isRaining()) {
                  if (firstType.equals(UtilityComponent.Type.CONDUIT)) {
                        if (secondType.equals(UtilityComponent.Type.CONDUIT)) {
                              MobEffectInstance effect = new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 1, true, true);
                              player.addEffect(effect);
                              return;
                        }
                        else {
                              MobEffectInstance effect = new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true);
                              player.addEffect(effect);
                        }
                        effectCooldown = EFFECT_COOLDOWN;
                  }
                  else if (secondType.equals(UtilityComponent.Type.CONDUIT)) {
                        MobEffectInstance effect = new MobEffectInstance(MobEffects.CONDUIT_POWER, 260, 0, true, true);
                        player.addEffect(effect);
                        effectCooldown = EFFECT_COOLDOWN;
                  }
            }

            if (player.level() instanceof ServerLevel serverLevel) {
                  if (firstType.equals(UtilityComponent.Type.OMINOUS))
                        consumeOminousEffect(player, serverLevel, mutable, 0);
                  else if (secondType.equals(UtilityComponent.Type.OMINOUS))
                        consumeOminousEffect(player, serverLevel, mutable, 1);
            }
      }

      private int omenCooldown = 0;
      private void consumeOminousEffect(Player player, ServerLevel serverLevel, UtilityComponent.Mutable mutable, int i) {
            if (omenCooldown > 0) {
                  omenCooldown--;
                  return;
            }
            MobEffectInstance effect = player.getEffect(MobEffects.BAD_OMEN);
            if (effect == null)
                  effect = player.getEffect(MobEffects.TRIAL_OMEN);
            if (effect == null)
                  effect = player.getEffect(MobEffects.RAID_OMEN);
            if (effect != null) {
                  omenCooldown = (int) (effect.getDuration() / (EFFECT_COOLDOWN * 0.55f));
            }
            else if (isInStructure(serverLevel, player, StructureTags.VILLAGE) || isInStructure(serverLevel, player, StructureTags.ON_TRIAL_CHAMBERS_MAPS)) {
                  MobEffectInstance omen = new MobEffectInstance(MobEffects.BAD_OMEN, 5999, 0, true, true);
                  player.addEffect(omen);

                  mutable.slots.get(i).shrink(1);
                  mutable.freeze();
            }
      }

      private boolean isInStructure(ServerLevel level, Player player, TagKey<Structure> tag) {
            Optional<HolderLookup.RegistryLookup<Structure>> optional = level.registryAccess().lookup(Registries.STRUCTURE);
            if (optional.isEmpty())
                  return false;

            HolderLookup.RegistryLookup<Structure> lookup = optional.get();
            Optional<HolderSet.Named<Structure>> optionalReference = lookup.get(tag);
            if (optionalReference.isEmpty())
                  return false;

            HolderSet.Named<Structure> aThrow = optionalReference.get();
            StructureManager manager = level.structureManager();
            StructureStart at = manager.getStructureWithPieceAt(player.blockPosition(), aThrow);
            return at.isValid();
      }

      private void applyConduitEffect(int amplifier, Player player) {
      }
}
