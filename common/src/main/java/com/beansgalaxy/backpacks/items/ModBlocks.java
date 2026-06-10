package com.beansgalaxy.backpacks.items;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public enum ModBlocks {
      BURLAP_SACK("burlap_sack", (properties) -> new BurlapSackBlock(properties
                                                                       .mapColor(MapColor.COLOR_YELLOW)
                                                                       .instrument(NoteBlockInstrument.GUITAR)
                                                                       .pushReaction(PushReaction.BLOCK)
                                                                       .noOcclusion()
                                                                       .sound(SoundType.WOOL)
                                                                       .strength(0.3F)
      ))
      ;

      public final String id;
      public final Supplier<BlockItem> item;
      public final boolean creativeIncluded;


      ModBlocks(String id, Function<BlockBehaviour.Properties, Block> factory) {
            this(id, factory, true);
      }

      ModBlocks(String id, Function<BlockBehaviour.Properties, Block> factory, boolean creativeIncluded) {
            this.id = id;
            this.item = Services.PLATFORM.registerBlock(id, factory);

            this.creativeIncluded = creativeIncluded;
      }

      public static void register() {

      }

      public Block get() {
            return item.get().getBlock();
      }
}
