package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public record ReferenceTrait(ResourceLocation location) {

      public static void ifAttributesPresent(ItemStack stack, Consumer<ItemAttributeModifiers> run) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null) {
                  referenceTrait.getAttributes().ifPresent(run);
            }
      }

      @Override
      public boolean equals(Object o) {
            if (this == o)
                  return true;
            if (o instanceof ReferenceTrait that)
                  return Objects.equals(location, that.location);
            return false;
      }

      public static ReferenceTrait of(String location) {
            return of(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, location));
      }

      public static ReferenceTrait of(String namespace, String location) {
            return of(ResourceLocation.fromNamespaceAndPath(namespace, location));
      }

      private static ReferenceTrait of(ResourceLocation location) {
            return new ReferenceTrait(location);
      }

      @NotNull
      public Optional<GenericTraits> getTrait() {
            ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
            if (reference == null)
                  return Optional.empty();

            return Optional.of(reference.traits());
      }

      @NotNull
      public Optional<ItemAttributeModifiers> getAttributes() {
            ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
            if (reference == null)
                  return Optional.empty();

            return Optional.of(reference.modifiers());
      }

      @NotNull
      public Optional<Byte> getUtilities() {
            ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
            if (reference == null)
                  return Optional.empty();

            byte utilities = reference.utilities();
            return utilities == 0 ? Optional.empty() : Optional.of(utilities);
      }

      @NotNull
      public Optional<DisplayComponent> getDisplay() {
            ReferenceRegistry reference = ReferenceRegistry.getNullable(location);
            if (reference == null)
                  return Optional.empty();

            return Optional.ofNullable(reference.display());
      }

      public static final Codec<ReferenceTrait> CODEC = ResourceLocation.CODEC.flatXmap(
                location -> {
                      ReferenceRegistry referenceRegistry = ReferenceRegistry.getNullable(location);
                      if (referenceRegistry == null)
                            return DataResult.error(() -> "No trait is registered using the given location; " + location, new ReferenceTrait(location));

                      return DataResult.success(new ReferenceTrait(location));
                },
                reference -> DataResult.success(reference.location)
      );

      public static final StreamCodec<? super RegistryFriendlyByteBuf, ReferenceTrait> STREAM_CODEC =
                  ResourceLocation.STREAM_CODEC.map(ReferenceTrait::new, ReferenceTrait::location);
}
