package com.beansgalaxy.backpacks.traits.battery;

import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class BatteryCodecs implements ITraitCodec<BatteryTraits> {
      public static final BatteryCodecs INSTANCE = new BatteryCodecs();

      public static final Codec<BatteryTraits> CODEC = RecordCodecBuilder.create(in ->
                  in.group(
                              PrimitiveCodec.INT.fieldOf("size").validate(size ->
                                    size > 0 ? DataResult.success(size)
                                    : DataResult.error(() -> "The provided field \"size\" must be greater than 0; Provided=" + size + 'L', 1)
                              ).forGetter(BatteryTraits::size),
                              PrimitiveCodec.INT.fieldOf("speed").validate(size ->
                                    size > 0 ? DataResult.success(size)
                                    : DataResult.error(() -> "The provided field \"speed\" must be greater than 0; Provided=" + size + 'L', 1)
                              ).forGetter(BatteryTraits::speed),
                              ModSound.MAP_CODEC.forGetter(BatteryTraits::sound)
                  ).apply(in, (size, speed, sound) -> new BatteryTraits(sound, size, speed))
      );

      @Override
      public Codec<BatteryTraits> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, BatteryTraits> STREAM_CODEC = StreamCodec.of((buf, traits) -> {
            ModSound.STREAM_CODEC.encode(buf, traits.sound());
            buf.writeInt(traits.size());
            buf.writeInt(traits.speed());
      }, buf -> new BatteryTraits(
                  ModSound.STREAM_CODEC.decode(buf),
                  buf.readInt(),
                  buf.readInt()
      ));

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, BatteryTraits> streamCodec() {
            return STREAM_CODEC;
      }

}