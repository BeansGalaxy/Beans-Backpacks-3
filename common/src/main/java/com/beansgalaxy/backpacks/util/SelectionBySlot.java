package com.beansgalaxy.backpacks.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.List;

public record SelectionBySlot(int slot, int selection) {
      public static Codec<SelectionBySlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("slot").forGetter(SelectionBySlot::slot),
            Codec.INT.fieldOf("selection").forGetter(SelectionBySlot::selection)
      ).apply(instance, SelectionBySlot::new));
      
      public static Codec<List<SelectionBySlot>> LIST_CODEC = ExtraCodecs.compactListCodec(CODEC);
}
