package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.BurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockItems {

      public static final BlockEntityType<BurlapSackEntity> BURLAP_SACK_ENTITY =
                  Registry.register(
                              BuiltInRegistries.BLOCK_ENTITY_TYPE,
                              ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "burlap_sack_entity"),
                              BlockEntityType.Builder.of(
                                          BurlapSackEntity::new,
                                          ModBlocks.BURLAP_SACK.get()
                              ).build(null)
                  );

      public static void register() {

      }
}
