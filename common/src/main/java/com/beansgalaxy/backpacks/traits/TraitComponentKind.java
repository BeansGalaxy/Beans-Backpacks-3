package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class TraitComponentKind<T extends GenericTraits> implements DataComponentType<T> {
      public static final List<TraitComponentKind<? extends GenericTraits>> TRAITS = Lists.newArrayList();
      public static final List<TraitComponentKind<? extends ItemStorageTraits>> STORAGE_TRAITS = Lists.newArrayList();
      public static final List<TraitComponentKind<? extends BundleLikeTraits>> BUNDLE_TRAITS = Lists.newArrayList();

      public static final StreamCodec<RegistryFriendlyByteBuf, TraitComponentKind<? extends GenericTraits>> STREAM_CODEC =
                  StreamCodec.of((buf, type) -> buf.writeInt(type.i), buf -> {
                        int integer = buf.readInt();
                        return get(integer);
                  });

      public static final Codec<TraitComponentKind<? extends GenericTraits>> CODEC = PrimitiveCodec.INT.comapFlatMap(
                              integer -> {
                                    TraitComponentKind<? extends GenericTraits> type = get(integer);
                                    return type == null ? DataResult.error(() -> "failure to parse TraitComponentType")
                                                : DataResult.success(type);
                              }, type -> type.i
      );

      public static TraitComponentKind<? extends GenericTraits> get(int integer) {
            for (TraitComponentKind<? extends GenericTraits> trait : TRAITS)
                  if (trait.i == integer)
                        return trait;
            return NonTrait.KIND;
      }

      public static TraitComponentKind<? extends GenericTraits>  get(String kind) {
            for (TraitComponentKind<? extends GenericTraits> trait : TRAITS)
                  if (Objects.equals(trait.name, kind))
                        return trait;
            return NonTrait.KIND;
      }

      public final int i;
      private final String name;
      private final ITraitCodec<T> codecs;

      public TraitComponentKind(int i, String name, ITraitCodec<T> codecs) {
            this.i = i;
            this.name = name;
            this.codecs = codecs;
      }

      public static <T extends GenericTraits> TraitComponentKind<T> register(String name, ITraitCodec<T> codecs) {
            int i = TraitComponentKind.TRAITS.size();
            TraitComponentKind<T> componentType = new TraitComponentKind<>(i, name, codecs);
            TRAITS.add(componentType);
            Services.PLATFORM.register(name, componentType);
            return componentType;
      }

      public static <T extends ItemStorageTraits> TraitComponentKind<T> registerItemStorage(String name, ITraitCodec<T> codecs) {
            TraitComponentKind<T> kind = register(name, codecs);
            STORAGE_TRAITS.add(kind);
            return kind;
      }

      public static <T extends BundleLikeTraits> TraitComponentKind<T> registerBundleLike(String name, ITraitCodec<T> codecs) {
            TraitComponentKind<T> kind = registerItemStorage(name, codecs);
            BUNDLE_TRAITS.add(kind);
            return kind;
      }

      @Override @NotNull
      public Codec<T> codec() {
            return codecs.codec();
      }

      @Override
      public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
            return codecs.streamCodec();
      }

      public String toString() {
            return "TraitComponentKind[" + name + ']';
      }


      public static <T extends GenericTraits> void encode(RegistryFriendlyByteBuf buf, T traits) {
            TraitComponentKind<T> kind = (TraitComponentKind<T>) traits.kind();
            kind.streamCodec().encode(buf, traits);
      }

      public static <T extends GenericTraits, A> RecordBuilder<A> encode(T traits, DynamicOps<A> ops, RecordBuilder<A> prefix) {
            TraitComponentKind<T> kind = (TraitComponentKind<T>) traits.kind();
            return kind.codec().fieldOf("trait").encode(traits, ops, prefix);
      }
}
