package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.Int2ValueMap;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class UtilityComponent {
      public static final UtilityComponent BLANK = new UtilityComponent(((Void) null)) {
            @Override public boolean isBlank() {return true;}
      };

      public static final String NAME = "utilities";

      private final Int2ObjectMap<ItemStack> slots;

      public UtilityComponent(Int2ObjectArrayMap<ItemStack> map) {
            map.defaultReturnValue(ItemStack.EMPTY);
            ImmutableList.Builder<Type> builder = ImmutableList.builder();
            map.forEach((integer, stack) -> {
                  Type util = getUtilities(stack);
                  if (!Type.NONE.equals(util))
                        builder.add(util);
            });

            this.slots = Int2ObjectMaps.unmodifiable(map);
      }

      private UtilityComponent(Void ignored) {
            this.slots = new Int2ValueMap<>(ItemStack.EMPTY);
      }

      public static Optional<Mutable> get(ItemStack stack) {
            byte size;
            Byte utilities = stack.get(Traits.UTILITIES);
            if (utilities != null) {
                  size = utilities;
            }
            else {
                  ReferenceTrait reference = stack.get(Traits.REFERENCE);
                  if (reference == null)
                        return Optional.empty();

                  Optional<Byte> optional = reference.getUtilities();
                  if (optional.isEmpty())
                        return Optional.empty();

                  size = optional.get();
            }

            UtilityComponent component = stack.getOrDefault(ITraitData.UTILITIES, UtilityComponent.BLANK);
            Mutable mutable = new Mutable(size, component, stack);
            return Optional.of(mutable);
      }

      public static byte getSize(ItemStack stack) {
            if (stack.isEmpty())
                  return 0;

            Byte utilities = stack.get(Traits.UTILITIES);
            if (utilities != null)
                  return utilities;

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null) {
                  Optional<Byte> optional = reference.getUtilities();
                  if (optional.isPresent())
                        return optional.get();
            }
            return 0;
      }

      public static boolean testItems(Player pPlayer, BiPredicate<ItemStack, Mutable> predicate) {
            ItemStack backpack = pPlayer.getItemBySlot(EquipmentSlot.BODY);
            Optional<Mutable> optional = get(backpack);
            if (optional.isEmpty())
                  return false;

            Mutable mutable = optional.get();
            for (Int2ObjectMap.Entry<ItemStack> entry : mutable.slots.int2ObjectEntrySet()) {
                  ItemStack value = entry.getValue();
                  if (predicate.test(value, mutable))
                        return true;
            }

            return false;
      }

      public boolean has(Type type) {
            return get(type) != null;
      }

      @Nullable
      public ItemStack get(Type type) {
            ObjectCollection<ItemStack> values = this.slots.values();
            for (ItemStack value : values) {
                  if (type.test(value))
                        return value;
            }
            return null;
      }

      @NotNull
      private static UtilityComponent.Type getUtilities(ItemStack stack) {
            return Type.NONE;
      }

      public ItemStack get(int i) {
            return slots.get(i);
      }

      public boolean isEmpty() {
            return slots.isEmpty();
      }

      public boolean isBlank() {
            return false;
      }

      public void clear() {
            slots.clear();
      }

      public Iterator<ItemStack> iterator() {
            return slots.values().iterator();
      }

      public enum Type {
            SPYGLASS(Items.SPYGLASS),
            CLOCK(Items.CLOCK),
            COMPASS(Items.COMPASS),
            RECOVERY(Items.RECOVERY_COMPASS),
            LODESTONE(stack -> stack.is(Items.COMPASS) && stack.has(DataComponents.LODESTONE_TRACKER)),
            NONE(Items.AIR);

            private final Predicate<ItemStack> predicate;
            Type(Item item) {
                  this.predicate = stack -> stack.is(item);
            }

            Type(Predicate<ItemStack> predicate) {
                  this.predicate = predicate;
            }

            public boolean test(ItemStack item) {
                  return predicate.test(item);
            }
      }

      public static class Mutable {
            public final Int2ObjectArrayMap<ItemStack> slots;
            public final byte size;
            private final ItemStack holder;

            Mutable(byte size, UtilityComponent component, ItemStack holder) {
                  Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>(component.slots);
                  map.defaultReturnValue(ItemStack.EMPTY);

                  this.slots = map;
                  this.size = size;
                  this.holder = holder;
            }

            public UtilityComponent freeze() {
                  slots.int2ObjectEntrySet().removeIf(stack -> stack.getValue().isEmpty());
                  UtilityComponent component;
                  if (slots.isEmpty()) {
                        holder.remove(ITraitData.UTILITIES);
                        component = BLANK;
                  }
                  else {
                        component = new UtilityComponent(slots);
                        holder.set(ITraitData.UTILITIES, component);
                  }
                  return component;
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UtilityComponent component)) return false;
            return Objects.equals(slots, component.slots);
      }

      @Override
      public int hashCode() {
            return Objects.hash(slots);
      }

      // ===================================================================================================================== CODECS

      private record Slots(int slot, ItemStack stack) {
            private static final Codec<Slots> CODEC = RecordCodecBuilder.create(in ->
                        in.group(
                                    PrimitiveCodec.INT.fieldOf("slot").forGetter(Slots::slot),
                                    ItemStack.CODEC.fieldOf("item").forGetter(Slots::stack)
                        ).apply(in, Slots::new));
      }

      public static final Codec<Byte> SIZE_CODEC = PrimitiveCodec.BYTE.validate(i ->
                  i > 0 ? i < 3 ? DataResult.success(i)
                                : DataResult.error(() -> "\"size\" cannot be larger than 2; input: " + i)
                                : DataResult.error(() -> "\"size\" cannot be smaller than 1; input: " + i)
                  );

      public static final Codec<UtilityComponent> CODEC = Slots.CODEC.listOf().xmap(items -> {
            Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>(items.size());
            map.defaultReturnValue(ItemStack.EMPTY);
            for (Slots slot : items)
                  map.put(slot.slot, slot.stack);

            return new UtilityComponent(map);

      }, utility -> {
            ImmutableList.Builder<Slots> list = ImmutableList.builder();
            utility.slots.forEach(((integer, stack) -> list.add(new Slots(integer, stack))));
            return list.build();
      });

      public static final StreamCodec<RegistryFriendlyByteBuf, UtilityComponent> STREAM_CODEC = new StreamCodec<>() {
            @Override public UtilityComponent decode(RegistryFriendlyByteBuf buf) {
                  boolean isBlank = buf.readBoolean();
                  if (isBlank)
                        return BLANK;

                  int size = buf.readVarInt();
                  Int2ObjectArrayMap<ItemStack> map = new Int2ObjectArrayMap<>(size);
                  map.defaultReturnValue(ItemStack.EMPTY);

                  for (int i = 0; i < size; i++) {
                        int key = buf.readInt();
                        ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
                        map.put(key, stack);
                  }

                  return new UtilityComponent(map);
            }

            @Override public void encode(RegistryFriendlyByteBuf buf, UtilityComponent component) {
                  boolean isBlank = component.isBlank();
                  buf.writeBoolean(isBlank);
                  if (isBlank)
                        return;

                  Map<Integer, ItemStack> slots = component.slots;
                  buf.writeVarInt(slots.size());
                  slots.forEach((key, stack) -> {
                        buf.writeInt(key);
                        ItemStack.STREAM_CODEC.encode(buf, stack);
                  });
            }
      };
}
