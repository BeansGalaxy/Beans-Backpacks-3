package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.FilterComponent;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public record ReferenceRegistry(GenericTraits traits,
                                ItemAttributeModifiers modifiers,
                                byte utilities,
                                DisplayComponent display,
                                FilterComponent filter
) {
      public static final HashMap<ResourceLocation, ReferenceRegistry> REFERENCES = new HashMap<>();

      public static ReferenceRegistry get(ResourceLocation location) {
            ReferenceRegistry reference = REFERENCES.get(location);
            if (reference == null)
                  return createEmptyReference();

            return reference;
      }

      public static ReferenceRegistry createEmptyReference() {
            return new ReferenceRegistry(NonTrait.INSTANCE, ItemAttributeModifiers.EMPTY, (byte) 0, null, FilterComponent.EMPTY);
      }

      @Nullable
      public static ReferenceRegistry getNullable(ResourceLocation location) {
            return REFERENCES.get(location);
      }

      public static void put(ResourceLocation location, ReferenceRegistry referenceRegistry) {
            REFERENCES.put(location, referenceRegistry);
      }


// ===================================================================================================================== CODECS


      public static StreamCodec<RegistryFriendlyByteBuf, ReferenceRegistry> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, ReferenceRegistry reference) {
                  TraitComponentKind<? extends GenericTraits> kind = reference.traits.kind();
                  TraitComponentKind.STREAM_CODEC.encode(buf, kind);
                  encode(buf, kind.codec(), reference.traits);

                  ItemAttributeModifiers.STREAM_CODEC.encode(buf, reference.modifiers);

                  boolean hasDisplay = reference.display != null;
                  buf.writeBoolean(hasDisplay);
                  if (hasDisplay)
                        DisplayComponent.STREAM_CODEC.encode(buf, reference.display);

                  buf.writeByte(reference.utilities);
                  
                  FilterComponent.STREAM_CODEC.encode(buf, reference.filter);
            }

            private <T extends GenericTraits> void encode(RegistryFriendlyByteBuf buf, Codec<T> codec, GenericTraits fields) {
                  buf.writeJsonWithCodec(codec, (T) fields);
            }

            @Override
            public ReferenceRegistry decode(RegistryFriendlyByteBuf buf) {
                  TraitComponentKind<? extends GenericTraits> kind = TraitComponentKind.STREAM_CODEC.decode(buf);
                  GenericTraits fields = buf.readJsonWithCodec(kind.codec());

                  ItemAttributeModifiers modifiers = ItemAttributeModifiers.STREAM_CODEC.decode(buf);


                  boolean hasDisplay = buf.readBoolean();
                  DisplayComponent display = hasDisplay ? DisplayComponent.STREAM_CODEC.decode(buf) : null;

                  byte utilities = buf.readByte();
                  
                  FilterComponent filter = FilterComponent.STREAM_CODEC.decode(buf);
                  
                  return new ReferenceRegistry(fields, modifiers, utilities, display, filter);
            }
      };
      
}
