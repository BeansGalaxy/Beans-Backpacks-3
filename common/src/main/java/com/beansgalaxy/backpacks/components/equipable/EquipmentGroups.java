package com.beansgalaxy.backpacks.components.equipable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public enum EquipmentGroups implements StringRepresentable {
      FEET(0, "feet", EquipmentSlot.FEET, EquipmentSlotGroup.FEET),
      LEGS(1, "legs", EquipmentSlot.LEGS, EquipmentSlotGroup.LEGS),
      CHEST(2, "chest", EquipmentSlot.CHEST, EquipmentSlotGroup.CHEST),
      HEAD(3, "head", EquipmentSlot.HEAD, EquipmentSlotGroup.HEAD),
      ARMOR(4, "armor", slot -> EquipmentSlot.Type.HUMANOID_ARMOR.equals(slot.getType()), EquipmentSlotGroup.ARMOR),
      BODY(5, "body", EquipmentSlot.BODY, EquipmentSlotGroup.BODY),
      TORSO(6, "torso", slot -> EquipmentSlot.BODY.equals(slot) || EquipmentSlot.CHEST.equals(slot), EquipmentSlotGroup.BODY),
      ;

      public static final IntFunction<EquipmentGroups> BY_ID = ByIdMap.continuous(EquipmentGroups::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
      public static final Codec<EquipmentGroups> CODEC = StringRepresentable.fromEnum(EquipmentGroups::values);
      public static final StreamCodec<ByteBuf, EquipmentGroups> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, EquipmentGroups::getId);

      private final int id;
      private final String name;
      private final Predicate<EquipmentSlot> predicate;
      private final Predicate<EquipmentSlotGroup> groupPredicate;
      private final List<EquipmentSlot> values;

      EquipmentGroups(int id, String name, EquipmentSlot equipmentSlot, EquipmentSlotGroup slotGroup) {
            this(id, name, equipmentSlot::equals, slotGroup);
      }

      EquipmentGroups(int id, String name, Predicate<EquipmentSlot> predicate, EquipmentSlotGroup slotGroup) {
            this.id = id;
            this.name = name;
            this.predicate = predicate;
            this.groupPredicate = slotGroup::equals;

            EquipmentSlot[] values = EquipmentSlot.values();
            ImmutableList.Builder<EquipmentSlot> list = ImmutableList.builder();

            for (int i = values.length - 1; i >= 0; i--) {
                  EquipmentSlot value = values[i];
                  if (predicate.test(value))
                        list.add(value);
            }

            this.values = list.build();
      }

      @Override
      public String getSerializedName() {
            return name;
      }

      public boolean test(EquipmentSlot slot) {
            return predicate.test(slot);
      }

      public boolean test(EquipmentSlotGroup slot) {
            return groupPredicate.test(slot);
      }

      public int getId() {
            return id;
      }

      public List<EquipmentSlot> getValues() {
            return values;
      }
}
