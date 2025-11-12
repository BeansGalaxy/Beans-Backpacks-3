package com.beansgalaxy.backpacks.traits.backpack;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.equipable.EquipmentGroups;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public class BackpackCodecs implements ITraitCodec<BackpackTraits> {
      public static final BackpackCodecs INSTANCE = new BackpackCodecs();

      public static final Codec<BackpackTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").validate(size ->
                              size < 256 ? size > 0 ? DataResult.success(size)
                              : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                              : DataResult.error(() -> "The provided field \"size\" must be smaller than 256; Provided=" + size, 255)
                        ).forGetter(BundleLikeTraits::size),
                        ModSound.MAP_CODEC.forGetter(GenericTraits::sound),
                        EquipmentGroups.CODEC.optionalFieldOf("slot", EquipmentGroups.BODY).forGetter(BackpackTraits::slots),
                        ResourceLocation.CODEC.optionalFieldOf("texture", ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "null")).forGetter(BackpackTraits::getTexture)
            ).apply(in, (size, sound, slot, texture) -> new BackpackTraits(sound, size, slot, texture))
      );

      @Override
      public Codec<BackpackTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BackpackTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
            buf.writeEnum(traits.slots());
            buf.writeResourceLocation(traits.getTexture());
      }, buf -> new BackpackTraits(
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt(),
                  buf.readEnum(EquipmentGroups.class),
                  buf.readResourceLocation())
      );

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BackpackTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
