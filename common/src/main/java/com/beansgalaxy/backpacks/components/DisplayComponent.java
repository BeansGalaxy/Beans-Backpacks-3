package com.beansgalaxy.backpacks.components;

import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record DisplayComponent(ResourceLocation location) {
      public static final String NAME = "display";

      public static final Codec<DisplayComponent> CODEC = ResourceLocation.CODEC.xmap(DisplayComponent::new, DisplayComponent::location);

      public static final StreamCodec<? super RegistryFriendlyByteBuf, DisplayComponent> STREAM_CODEC =
                  ResourceLocation.STREAM_CODEC.map(DisplayComponent::new, DisplayComponent::location);

      public static Optional<DisplayComponent> get(ItemStack stack) {
            return stack.isEmpty() ? Optional.empty() : Optional.ofNullable(stack.get(Traits.DISPLAY));
      }

      public ModelResourceLocation getModel() {
            return ModelResourceLocation.inventory(location);
      }
}
