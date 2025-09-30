package com.beansgalaxy.backpacks.components;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SlotSelection {
      private static int SLOT_SELECTION_COUNT = 0;
      private final Int2IntArrayMap slots = defaultSlotMap();
      private final int id;

      public SlotSelection() {
            this.id = SLOT_SELECTION_COUNT;
            SLOT_SELECTION_COUNT++;
      }

      public int getSelectedSlot(Player player) {
            return slots.get(player.getId());
      }

      public void setSelectedSlot(Player player, int selectedSlot) {
            slots.put(player.getId(), selectedSlot);
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
            for (int key : slots.keySet()) {
                  int selectedSlot = slots.get(key);
                  if (selectedSlot == 0)
                        continue;

                  int safeSlot = selectedSlot - 1;
                  int i = safeSlot < slot ? selectedSlot : safeSlot;
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

      public void clear() {
            slots.clear();
      }

      public void ceil(int max) {
            for (int key : slots.keySet()) {
                  int i = slots.get(key);
                  if (i > max)
                        slots.put(key, max);
            }
      }
}
