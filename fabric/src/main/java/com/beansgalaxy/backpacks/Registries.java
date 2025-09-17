package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class Registries {

      public static final BlockEntityType<? extends AbstractBurlapSackEntity> BURLAP_SACK_ENTITY =
                  Registry.register(
                              BuiltInRegistries.BLOCK_ENTITY_TYPE,
                              ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "burlap_sack_entity"),
                              BlockEntityType.Builder.of(
                                          BurlapSackEntity::new,
                                          ModBlocks.BURLAP_SACK.get()
                              ).build(null)
                  );

      public static final MenuType<BurlapSackMenu> BURLAP_SACK_MENU =
                  Registry.register(BuiltInRegistries.MENU,
                                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "burlap_sack_menu"),
                                    new ExtendedScreenHandlerType<>(BurlapSackMenu::new, BlockPos.STREAM_CODEC)
                  );

      public static void register() {

      }
}
