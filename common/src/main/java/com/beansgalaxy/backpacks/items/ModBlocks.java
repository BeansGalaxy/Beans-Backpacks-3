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

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public enum ModBlocks {
      BURLAP_SACK("burlap_sack", () -> new BurlapSackBlock(BlockBehaviour.Properties.of()
                                                                       .mapColor(MapColor.COLOR_YELLOW)
                                                                       .instrument(NoteBlockInstrument.GUITAR)
                                                                       .pushReaction(PushReaction.BLOCK)
                                                                       .noOcclusion()
                                                                       .sound(SoundType.WOOL)
                                                                       .strength(0.3F)
      ))
      ;

      public final String id;
      public final Supplier<Block> block;
      public final Supplier<Item> item;
      public final boolean creativeIncluded;


      ModBlocks(String id, Supplier<Block> item) {
            this(id, item, true);
      }

      ModBlocks(String id, Supplier<Block> block, boolean creativeIncluded) {
            this.id = id;
            this.block = Services.PLATFORM.registerBlock(id, block);
            this.item = Services.PLATFORM.register(id, () -> new BlockItem(null, new Item.Properties()) {
                  @Override public Block getBlock() {
                        return ModBlocks.this.block.get();
                  }
            });

            this.creativeIncluded = creativeIncluded;
      }

      public static void register() {

      }

      public Block get() {
            return block.get();
      }
}
