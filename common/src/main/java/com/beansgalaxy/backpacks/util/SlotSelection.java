package com.beansgalaxy.backpacks.util;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

public class SlotSelection {
      private static int SLOT_SELECTION_COUNT = 0;
      private final Int2IntArrayMap slots = defaultSlotMap();
      private final int id;

      public SlotSelection() {
            this.id = SLOT_SELECTION_COUNT;
            SLOT_SELECTION_COUNT++;
      }

      public void addAll(SlotSelection slotSelection) {
            slots.putAll(slotSelection.slots);
      }

      public int getSelectedSlot(Player player) {
            return slots.get(player.getId());
      }

      public void setSelectedSlot(Player player, int selectedSlot) {
            slots.put(player.getId(), selectedSlot);
      }

      public int modSelectedSlot(Player player, @NotNull IntUnaryOperator operation) {
            int selectedSlot = slots.get(player.getId());
            int i = operation.applyAsInt(selectedSlot);
            slots.put(player.getId(), i);
            return i;
      }

      @NotNull
      private static Int2IntArrayMap defaultSlotMap() {
            Int2IntArrayMap map = new Int2IntArrayMap();
            map.defaultReturnValue(0);
            return map;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, SlotSelection> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(RegistryFriendlyByteBuf buf, SlotSelection slotSelection) {
                  int size = slotSelection.slots.size();
                  buf.writeInt(size);
                  slotSelection.slots.forEach((key, slot) -> {
                        buf.writeInt(key);
                        buf.writeInt(slot);
                  });
            }

            @Override
            public SlotSelection decode(RegistryFriendlyByteBuf buf) {
                  int size = buf.readInt();
                  SlotSelection slotSelection = new SlotSelection();
                  for (int i = 0; i < size; i++) {
                        int key = buf.readInt();
                        int slot = buf.readInt();
                        slotSelection.slots.put(key, slot);
                  }

                  return slotSelection;
            }
      };

      public void limit(int slot, int size) {
            if (size == 0) {
                  slots.clear();
                  return;
            }

            for (int key : slots.keySet()) {
                  int selectedSlot = slots.get(key);
                  int i;
                  if (selectedSlot == 0)
                        i = 0;
                  else {
                        int safeSlot = selectedSlot - 1;
                        i = safeSlot < slot ? selectedSlot : safeSlot;
                  }

                  slots.put(key, i);
            }
      }

      public void grow(int slot) {
            for (int key : slots.keySet()) {
                  int selectedSlot = slots.get(key);
                  int i;
                  if (slot == 0)
                        i = selectedSlot + 1;
                  else
                        i = selectedSlot < slot ? selectedSlot : selectedSlot + 1;

                  slots.put(key, i);
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SlotSelection that)) return false;
            return slots == that.slots;
      }

      @Override
      public int hashCode() {
            return Objects.hashCode(id);
      }
}
