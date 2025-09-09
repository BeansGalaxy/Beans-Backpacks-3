package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.HashMap;

public record ReferenceRegistry(GenericTraits traits, ItemAttributeModifiers modifiers,
                                PlaceableComponent placeable, EquipableComponent equipable,
                                byte utilities, DisplayComponent display
) {
      public static final HashMap<ResourceLocation, ReferenceRegistry> REFERENCES = new HashMap<>();

      public static ReferenceRegistry get(ResourceLocation location) {
            ReferenceRegistry reference = REFERENCES.get(location);
            if (reference == null)
                  return createEmptyReference();

            return reference;
      }

      public static ReferenceRegistry createEmptyReference() {
            return new ReferenceRegistry(NonTrait.INSTANCE, ItemAttributeModifiers.EMPTY, null, null, (byte) 0, null);
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

                  boolean isPlaceable = reference.placeable != null;
                  buf.writeBoolean(isPlaceable);
                  if (isPlaceable)
                        PlaceableComponent.STREAM_CODEC.encode(buf, reference.placeable);

                  boolean isEquipment = reference.equipable != null;
                  buf.writeBoolean(isEquipment);
                  if (isEquipment)
                        EquipableComponent.STREAM_CODEC.encode(buf, reference.equipable);

                  boolean hasDisplay = reference.display != null;
                  buf.writeBoolean(hasDisplay);
                  if (hasDisplay)
                        DisplayComponent.STREAM_CODEC.encode(buf, reference.display);

                  buf.writeByte(reference.utilities);
            }

            private <T extends GenericTraits> void encode(RegistryFriendlyByteBuf buf, Codec<T> codec, GenericTraits fields) {
                  buf.writeJsonWithCodec(codec, (T) fields);
            }

            @Override
            public ReferenceRegistry decode(RegistryFriendlyByteBuf buf) {
                  TraitComponentKind<? extends GenericTraits> kind = TraitComponentKind.STREAM_CODEC.decode(buf);
                  GenericTraits fields = buf.readJsonWithCodec(kind.codec());

                  ItemAttributeModifiers modifiers = ItemAttributeModifiers.STREAM_CODEC.decode(buf);

                  boolean isPlaceable = buf.readBoolean();
                  PlaceableComponent placeableComponent = isPlaceable ? PlaceableComponent.STREAM_CODEC.decode(buf) : null;

                  boolean isEquipable = buf.readBoolean();
                  EquipableComponent equipableComponent = isEquipable ? EquipableComponent.STREAM_CODEC.decode(buf) : null;

                  boolean hasDisplay = buf.readBoolean();
                  DisplayComponent displayComponent = hasDisplay ? DisplayComponent.STREAM_CODEC.decode(buf) : null;

                  byte utilities = buf.readByte();

                  return new ReferenceRegistry(fields, modifiers, placeableComponent, equipableComponent, utilities, displayComponent);
            }
      };
}
