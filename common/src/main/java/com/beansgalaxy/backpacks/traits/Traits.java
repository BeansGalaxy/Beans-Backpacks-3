package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.ender.EmptyEnderItem;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.alchemy.AlchemyCodecs;
import com.beansgalaxy.backpacks.traits.alchemy.AlchemyTraits;
import com.beansgalaxy.backpacks.traits.bulk.BulkCodecs;
import com.beansgalaxy.backpacks.traits.bulk.BulkTraits;
import com.beansgalaxy.backpacks.traits.bundle.BundleCodecs;
import com.beansgalaxy.backpacks.traits.bundle.BundleTraits;
import com.beansgalaxy.backpacks.traits.chest.ChestCodecs;
import com.beansgalaxy.backpacks.traits.chest.ChestTraits;
import com.beansgalaxy.backpacks.traits.experience.XpCodecs;
import com.beansgalaxy.backpacks.traits.experience.XpTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxCodecs;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.traits.quiver.QuiverCodecs;
import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Traits {
      DataComponentType<PlaceableComponent>
                  PLACEABLE = register(PlaceableComponent.NAME, PlaceableComponent.CODEC, PlaceableComponent.STREAM_CODEC);

      DataComponentType<EquipableComponent>
                  EQUIPABLE = register(EquipableComponent.NAME, EquipableComponent.CODEC, EquipableComponent.STREAM_CODEC);

      DataComponentType<ReferenceTrait>
                  REFERENCE = register("reference", ReferenceTrait.CODEC, ReferenceTrait.STREAM_CODEC);

      DataComponentType<EnderTraits>
                  ENDER = register("ender", EnderTraits.CODEC, EnderTraits.STREAM_CODEC);

      DataComponentType<EmptyEnderItem.UnboundEnderTraits>
                  EMPTY_ENDER = register("empty_ender", EmptyEnderItem.CODEC, EmptyEnderItem.STREAM_CODEC);

      TraitComponentKind<BundleTraits>
                  BUNDLE = TraitComponentKind.register(BundleTraits.NAME, BundleCodecs.INSTANCE);

      TraitComponentKind<BulkTraits>
                  BULK = TraitComponentKind.register(BulkTraits.NAME, BulkCodecs.INSTANCE);

      TraitComponentKind<LunchBoxTraits>
                  LUNCH_BOX = TraitComponentKind.register(LunchBoxTraits.NAME, LunchBoxCodecs.INSTANCE);

      TraitComponentKind<? extends GenericTraits>
                  BUCKET = Services.PLATFORM.registerBucket();

      TraitComponentKind<? extends GenericTraits>
                  BATTERY = Services.PLATFORM.registerBattery();

      TraitComponentKind<XpTraits>
                  EXPERIENCE = TraitComponentKind.register(XpTraits.NAME, XpCodecs.INSTANCE);

      TraitComponentKind<QuiverTraits>
                  QUIVER = TraitComponentKind.register(QuiverTraits.NAME, QuiverCodecs.INSTANCE);

      TraitComponentKind<AlchemyTraits>
                  ALCHEMY = TraitComponentKind.register(AlchemyTraits.NAME, AlchemyCodecs.INSTANCE);

      TraitComponentKind<ChestTraits>
                  CHEST = TraitComponentKind.register(ChestTraits.NAME, ChestCodecs.INSTANCE);

      DataComponentType<Byte>
                  UTILITIES = register(UtilityComponent.NAME, UtilityComponent.SIZE_CODEC, ByteBufCodecs.BYTE);

      List<TraitComponentKind<? extends GenericTraits>> ALL_TRAITS = List.of(
                  BUNDLE,     LUNCH_BOX,        BULK,       BUCKET,
                  BATTERY,    EXPERIENCE,       QUIVER,     ALCHEMY,
                  CHEST
      );

      List<TraitComponentKind<? extends ItemStorageTraits>> STORAGE_TRAITS = List.of(
                  BUNDLE,     LUNCH_BOX,        BULK,       QUIVER,
                  ALCHEMY,    CHEST
      );

      List<TraitComponentKind<? extends BundleLikeTraits>> BUNDLE_TRAITS = List.of(
                  BUNDLE,     LUNCH_BOX,        QUIVER,     ALCHEMY
      );

      static <T> DataComponentType<T> register(String name, Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            DataComponentType.Builder<T> builder = DataComponentType.builder();
            DataComponentType<T> type = builder.persistent(codec).networkSynchronized(streamCodec).cacheEncoding().build();
            return Services.PLATFORM.register(name, type);
      }

      static Optional<GenericTraits> get(ItemStack stack) {
            return stack.isEmpty() ? Optional.empty() : get(PatchedComponentHolder.of(stack));
      }

      static Optional<GenericTraits> get(PatchedComponentHolder stack) {
            for (TraitComponentKind<? extends GenericTraits> type : ALL_TRAITS) {
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

            return testIfPresent(PatchedComponentHolder.of(stack), predicate);
      }

      static boolean testIfPresent(PatchedComponentHolder stack, Predicate<GenericTraits> predicate) {
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

}
