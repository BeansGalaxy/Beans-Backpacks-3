package com.beansgalaxy.backpacks.components.ender;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.reference.ReferenceRegistry;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EmptyEnderItem extends Item {

      public EmptyEnderItem(Properties properties, String location) {
            super(properties.stacksTo(1).component(Traits.EMPTY_ENDER, new EmptyEnderItem.UnboundEnderTraits(location)));
      }

      @Override
      public InteractionResult use(Level level, Player player, InteractionHand hand) {
            ItemStack thisStack = player.getItemInHand(hand);
            UnboundEnderTraits traits = thisStack.get(Traits.EMPTY_ENDER);

            if (traits == null)
                  return InteractionResult.PASS;

            if (level.isClientSide())
                  return InteractionResult.SUCCESS;

            else {
                  thisStack.consume(1, player);
                  player.awardStat(Stats.ITEM_USED.get(this));
                  ItemStack newStack = EnderTraits.createItem(player, traits.location);

                  if (thisStack.isEmpty())
                        return InteractionResult.CONSUME;
                  else {
                        if (!player.getInventory().add(-1, newStack.copy()))
                              player.drop(newStack, false);

                        return InteractionResult.CONSUME;
                  }
            }
      }
      
      public static final Codec<UnboundEnderTraits> CODEC = Codec.of(ResourceLocation.CODEC.comap(UnboundEnderTraits::location),
                  ResourceLocation.CODEC.flatMap(location -> ReferenceRegistry.get(location) == null
                              ? DataResult.error(() -> "No trait is registered using the given location: " + location)
                              : DataResult.success(new UnboundEnderTraits(location))
                  )
      );

      public static final StreamCodec<? super RegistryFriendlyByteBuf, UnboundEnderTraits> STREAM_CODEC =
                  ResourceLocation.STREAM_CODEC.map(UnboundEnderTraits::new, UnboundEnderTraits::location);

      public record UnboundEnderTraits(ResourceLocation location) {
            UnboundEnderTraits(String location) {
                  this(Constants.defaultLocation(location));
            }
      }
}
