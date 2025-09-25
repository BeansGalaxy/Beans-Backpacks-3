package com.beansgalaxy.backpacks.traits.quiver;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class QuiverCodecs implements ITraitCodec<QuiverTraits> {
      public static final QuiverCodecs INSTANCE = new QuiverCodecs();

      public static final Codec<QuiverTraits> CODEC = RecordCodecBuilder.create(in ->
            in.group(
                        PrimitiveCodec.INT.fieldOf("size").validate(size ->
                              size < 256 ? size > 0 ? DataResult.success(size)
                              : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size, 1)
                              : DataResult.error(() -> "The provided field \"size\" must be smaller than 256; Provided=" + size, 255)
                        ).forGetter(BundleLikeTraits::size),
                        ModSound.MAP_CODEC.forGetter(QuiverTraits::sound)
            ).apply(in, (size, sound) -> new QuiverTraits(sound, size))
      );

      @Override
      public Codec<QuiverTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, QuiverTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
      }, buf -> new QuiverTraits(
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt()
      ));

            @Override
      public StreamCodec<RegistryFriendlyByteBuf, QuiverTraits> streamCodec() {
            return STREAM_CODEC;
      }

}
