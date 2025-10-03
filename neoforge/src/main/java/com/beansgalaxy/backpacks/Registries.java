package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.items.ModBlocks;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class Registries {

      public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.key(), Constants.MOD_ID);
      public static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);
      public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);
      public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Constants.MOD_ID);

      public static final Supplier<BlockEntityType<? extends AbstractBurlapSackEntity>> BURLAP_SACK_ENTITY =
                  BLOCK_ENTITIES.register("burlap_sack_entity", () ->
                              BlockEntityType.Builder.of(BurlapSackEntity::new, ModBlocks.BURLAP_SACK.get()).build(null));

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
