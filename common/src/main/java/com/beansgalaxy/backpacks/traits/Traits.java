package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.FilterComponent;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.ender.EmptyEnderItem;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.alchemy.AlchemyCodecs;
import com.beansgalaxy.backpacks.traits.alchemy.AlchemyTraits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackCodecs;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.bundle.BundleCodecs;
import com.beansgalaxy.backpacks.traits.bundle.BundleTraits;
import com.beansgalaxy.backpacks.traits.chest.ChestCodecs;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxCodecs;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.traits.quiver.QuiverCodecs;
import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Traits {

      DataComponentType<DisplayComponent>
                  DISPLAY = register(DisplayComponent.NAME, DisplayComponent.CODEC, DisplayComponent.STREAM_CODEC);
      
      DataComponentType<FilterComponent>
                  FILTER = register(FilterComponent.NAME, FilterComponent.CODEC, FilterComponent.STREAM_CODEC);

      DataComponentType<ReferenceTrait>
                  REFERENCE = register("reference", ReferenceTrait.CODEC, ReferenceTrait.STREAM_CODEC);

      DataComponentType<EnderTraits>
                  ENDER = register("ender", EnderTraits.CODEC, EnderTraits.STREAM_CODEC);

      DataComponentType<EmptyEnderItem.UnboundEnderTraits>
                  EMPTY_ENDER = register("empty_ender", EmptyEnderItem.CODEC, EmptyEnderItem.STREAM_CODEC);

      TraitComponentKind<BundleTraits>
                  BUNDLE = TraitComponentKind.registerBundleLike(BundleTraits.NAME, BundleCodecs.INSTANCE);

      TraitComponentKind<LunchBoxTraits>
                  LUNCH_BOX = TraitComponentKind.registerBundleLike(LunchBoxTraits.NAME, LunchBoxCodecs.INSTANCE);

      TraitComponentKind<QuiverTraits>
                  QUIVER = TraitComponentKind.registerBundleLike(QuiverTraits.NAME, QuiverCodecs.INSTANCE);

      TraitComponentKind<AlchemyTraits>
                  ALCHEMY = TraitComponentKind.registerBundleLike(AlchemyTraits.NAME, AlchemyCodecs.INSTANCE);

      DataComponentType<Byte>
                  UTILITIES = register(UtilityComponent.NAME, UtilityComponent.SIZE_CODEC, ByteBufCodecs.BYTE);
      
      TraitComponentKind<ChestTraits>
                  CHEST = TraitComponentKind.registerBundleLike(ChestTraits.NAME, ChestCodecs.INSTANCE);

      TraitComponentKind<BackpackTraits>
                  BACKPACK = TraitComponentKind.registerBundleLike(BackpackTraits.NAME, BackpackCodecs.INSTANCE);

      Codec<List<ItemStack>> STACKS_CODEC = Codec.list(RecordCodecBuilder.create((in) ->
                                          in.group(
                                                      ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
                                                      Codec.INT.fieldOf("count").forGetter(ItemStack::getCount),
                                                      DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
                                          ).apply(in, ItemStack::new)));

      static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            DataComponentType.Builder<T> builder = DataComponentType.builder();
            DataComponentType<T> type = builder.persistent(codec).networkSynchronized(streamCodec).cacheEncoding().build();
            return Services.PLATFORM.register(name, type);
      }

      static Optional<GenericTraits> get(ItemStack stack) {
            return stack.isEmpty() ? Optional.empty() : get(ComponentHolder.of(stack));
      }

      static Optional<GenericTraits> get(ComponentHolder stack) {
            for (TraitComponentKind<? extends GenericTraits> type : TraitComponentKind.TRAITS) {
                  GenericTraits traits = stack.get(type);
                  if (traits != null)
                        return Optional.of(traits);
            }

            ReferenceTrait reference = stack.get(REFERENCE);
            if (reference != null)
                  return reference.getTrait();

            return Optional.empty();
      }

      static void runIfPresent(ItemStack stack, Consumer<GenericTraits> runnable) {
            if (!stack.isEmpty()) {
                  Optional<GenericTraits> traits = get(stack);
                  traits.ifPresent(runnable);
            }
      }

      static void runIfPresent(ItemStack stack, Consumer<GenericTraits> runnable, Runnable orElse) {
            if (!stack.isEmpty()) {
                  Optional<GenericTraits> traits = get(stack);
                  traits.ifPresentOrElse(runnable, orElse);
            }
            else orElse.run();
      }

      static boolean testIfPresent(ItemStack stack, Predicate<GenericTraits> predicate) {
            if (stack.isEmpty())
                  return false;

            return testIfPresent(ComponentHolder.of(stack), predicate);
      }

      static boolean testIfPresent(ComponentHolder stack, Predicate<GenericTraits> predicate) {
            Optional<GenericTraits> genericTraits = get(stack);
            if (genericTraits.isEmpty()) return false;

            GenericTraits traits = genericTraits.get();
            return predicate.test(traits);
      }

      static void runIfEquipped(Player player, BiPredicate<GenericTraits, EquipmentSlot> runnable) {
            NonNullList<Slot> slots = player.inventoryMenu.slots;
            for (int i = slots.size() - 1; i > 4 ; i--) {
                  Slot slot = slots.get(i);
                  if (slot instanceof EquipmentSlotAccess access) {
                        ItemStack stack = slot.getItem();
                        if (stack.isEmpty())
                              continue;

                        Optional<GenericTraits> traits = get(stack);
                        if (traits.isEmpty())
                              continue;

                        if (runnable.test(traits.get(), access.getSlot()))
                              return;
                  }
            }
      }

      static Fraction getWeight(List<ItemStack> stacks) {
            if (stacks.isEmpty())
                  return Fraction.ZERO;

            Fraction fraction = Fraction.ZERO;
            for (ItemStack stack : stacks) {
                  Fraction stackWeight = getItemWeight(stack).multiplyBy(Fraction.getFraction(stack.getCount(), 1));
                  fraction = fraction.add(stackWeight);
            }
            return fraction;
      }

      static Fraction getWeight(List<ItemStack> stacks, int denominator) {
            return getWeight(stacks).multiplyBy(Fraction.getFraction(1, denominator));
      }

      static Fraction getItemWeight(ItemStack stack) {
            return Fraction.getFraction(1, stack.getMaxStackSize());
      }

      static void register() {

      }

      @Nullable
      static <T extends GenericTraits> T get(ComponentHolder backpack, TraitComponentKind<T> kind) {
            T traits = backpack.get(kind);
            if (traits != null)
                  return traits;

            ReferenceTrait referenceTrait = backpack.get(Traits.REFERENCE);
            if (referenceTrait != null) {
                  Optional<GenericTraits> reference = referenceTrait.getTrait();
                  if (reference.isPresent()) {
                        GenericTraits genericTraits = reference.get();
                        if (kind.equals(genericTraits.kind()))
                              return (T) genericTraits;
                  }
            }

            return null;
      }

      @Nullable
      static <T extends GenericTraits> T get(ComponentHolder backpack, TraitComponentKind<T>[] kinds) {
            for (TraitComponentKind<T> kind : kinds) {
                  T traits = backpack.get(kind);
                  if (traits != null)
                        return traits;
            }

            ReferenceTrait referenceTrait = backpack.get(Traits.REFERENCE);
            if (referenceTrait == null)
                  return null;

            Optional<GenericTraits> reference = referenceTrait.getTrait();
            if (reference.isEmpty())
                  return null;

            GenericTraits genericTraits = reference.get();
            for (TraitComponentKind<T> kind : kinds) {
                  if (kind.equals(genericTraits.kind()))
                        return (T) genericTraits;
            }

            return null;
      }
}
