package com.beansgalaxy.backpacks.traits.chest;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.alchemy.AlchemyTraits;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ChestCodecs implements ITraitCodec<ChestTraits> {
      public static final ChestCodecs INSTANCE = new ChestCodecs();

      public static final Codec<ChestTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").validate(size ->
                              size < 256 ? size > 0 ? DataResult.success(size)
                              : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                              : DataResult.error(() -> "The provided field \"size\" must be smaller than 256; Provided=" + size, 255)
                        ).forGetter(BundleLikeTraits::size),
                        ModSound.MAP_CODEC.forGetter(ChestTraits::sound)
            ).apply(in, (size, sound) -> new ChestTraits(sound, size))
      );

      @Override
      public Codec<ChestTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, ChestTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
                  ModSound.STREAM_CODEC.encode(buf, traits.sound());
                  buf.writeInt(traits.size());
      }, buf ->
            new ChestTraits(
                        ModSound.STREAM_CODEC.decode(buf),
                        buf.readInt()
            )
      );

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, ChestTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
