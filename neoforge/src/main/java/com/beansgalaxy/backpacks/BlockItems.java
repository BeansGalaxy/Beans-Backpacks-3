package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.BurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;

import java.util.function.Supplier;

public class BlockItems {

      public static final Supplier<BlockEntityType<BurlapSackEntity>> BURLAP_SACK_ENTITY =
                  NeoForgeMain.BLOCK_ENTITIES.register("burlap_sack_entity", () -> BlockEntityType.Builder.of(BurlapSackEntity::new, ModBlocks.BURLAP_SACK.get()).build(null));

      public static void register(IEventBus eventBus) {
      }
}
