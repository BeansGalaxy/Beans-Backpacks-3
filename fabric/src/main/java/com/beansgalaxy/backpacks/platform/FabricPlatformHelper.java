package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.BurlapSackEntity;
import com.beansgalaxy.backpacks.ModRegistry;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
 
    @Override
    public Holder<Item> register(String id, Function<Item.Properties, Item> factory) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Constants.defaultLocation(id));
        Item item = factory.apply(new Item.Properties().setId(itemKey));
        return Registry.registerForHolder(BuiltInRegistries.ITEM, itemKey, item);
    }

    @Override
    public Supplier<BlockItem> registerBlock(String id, Function<BlockBehaviour.Properties, Block> factory) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Constants.defaultLocation(id));
        Block block = factory.apply(BlockBehaviour.Properties.of().setId(blockKey));
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
        
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Constants.defaultLocation(id));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
        
        BlockItem blockItem = Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return () -> blockItem;
    }

    @Override
    public BlockEntityType<? extends AbstractBurlapSackEntity> getBurlapSackEntityType() {
        return ModRegistry.BURLAP_SACK_ENTITY;
    }

    @Override
    public MenuType<BurlapSackMenu> getBurlapSackMenuType() {
        return ModRegistry.BURLAP_SACK_MENU;
    }

    @Override
    public BlockEntity createBurlapSackEntity(BlockPos pos, BlockState state) {
        return new BurlapSackEntity(pos, state);
    }

    @Override
    public void send(Network2S network, Packet2S msg) {
        ClientPlayNetworking.send(msg);
    }

    @Override
    public void send(Network2C network, Packet2C msg, ServerPlayer to) {
        ServerPlayNetworking.send(to, msg);
    }

    @Override
    public void send(Network2C network2C, Packet2C msg, MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            send(network2C, msg, player);
        }
    }

    @Override
    public void send(Network2C network2C, Packet2C msg, MinecraftServer server, ServerPlayer sender) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player != sender)
                send(network2C, msg, player);
        }
    }

    @Override
    public <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
        return Registry.register(
                    BuiltInRegistries.DATA_COMPONENT_TYPE,
                    Constants.defaultLocation(name),
                    type
        );
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> type) {
        EntityType<T> register = Registry.register(
              BuiltInRegistries.ENTITY_TYPE,
              Constants.defaultLocation(name),
              type.build(ResourceKey.create(Registries.ENTITY_TYPE, Constants.defaultLocation(name)))
        );
        return () -> register;
    }

    @Override
    public SoundEvent register(String name, SoundEvent event) {
        return Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    Constants.defaultLocation(name),
                    event
        );
    }

    @Override
    public Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(
                    BuiltInRegistries.ATTRIBUTE,
                    Constants.defaultLocation(name),
                    attribute
        );
    }

    @Override public Supplier<Activity> registerActivity(String name) {
        Activity register = Registry.register(BuiltInRegistries.ACTIVITY, Constants.defaultLocation(name), new Activity(name));
        return () -> register;
    }

    @Override public <T> Supplier<MemoryModuleType<T>> registerMemoryModule(String name, Codec<T> codec) {
        MemoryModuleType<T> register = Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, Constants.defaultLocation(name), new MemoryModuleType<>(Optional.of(codec)));
        return () -> register;
    }
    
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
    
}
