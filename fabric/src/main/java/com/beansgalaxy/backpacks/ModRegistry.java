package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModRegistry {

      public static final BlockEntityType<? extends AbstractBurlapSackEntity> BURLAP_SACK_ENTITY =
                  Registry.register(
                        BuiltInRegistries.BLOCK_ENTITY_TYPE,
                        Constants.defaultLocation("burlap_sack_entity"),
                        FabricBlockEntityTypeBuilder.create(BurlapSackEntity::new, ModBlocks.BURLAP_SACK.get()).build()
                  );

      public static final MenuType<BurlapSackMenu> BURLAP_SACK_MENU =
                  Registry.register(BuiltInRegistries.MENU,
                                    Constants.defaultLocation("burlap_sack_menu"),
                                    new ExtendedScreenHandlerType<>(BurlapSackMenu::new, BlockPos.STREAM_CODEC)
                  );

      public static void register() {

      }
}
