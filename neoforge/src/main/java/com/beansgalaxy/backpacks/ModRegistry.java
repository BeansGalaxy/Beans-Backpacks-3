package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegistry {
      
      public static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
      public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
      public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Constants.MOD_ID);

      public static final Supplier<BlockEntityType<? extends AbstractBurlapSackEntity>> BURLAP_SACK_ENTITY =
                  BLOCK_ENTITIES.register("burlap_sack_entity", () ->
                        new BlockEntityType<>(BurlapSackEntity::new, false, ModBlocks.BURLAP_SACK.get())
                  );

      public static final Supplier<MenuType<BurlapSackMenu>> BURLAP_SACK_MENU =
                  MENUS.register("burlap_sack_menu", () -> IMenuTypeExtension.create(BurlapSackMenu::new));

      public static void register(IEventBus eventBus) {
            BLOCK_ENTITIES.register(eventBus);
            CREATIVE_TAB_REGISTRY.register(eventBus);
            CREATIVE_TAB_REGISTRY.register("backpacks",
                                           () -> ModItems.CREATIVE_TAB.apply(CreativeModeTab.builder()).build());
            MENUS.register(eventBus);
      }
}
