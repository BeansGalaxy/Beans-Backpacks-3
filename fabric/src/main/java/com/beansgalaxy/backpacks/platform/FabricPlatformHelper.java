package com.beansgalaxy.backpacks.platform;

import com.beansgalaxy.backpacks.BlockItems;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.items.BurlapSackEntity;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.services.IPlatformHelper;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.nio.file.Path;
import java.util.Optional;
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
    public Supplier<Item> register(String name, Supplier<Item> item) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        Item register = Registry.register(BuiltInRegistries.ITEM, resourceLocation, item.get());
          return () -> register;
    }

    @Override
    public Supplier<Block> registerBlock(String id, Supplier<Block> item) {
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id);
        Block register = Registry.register(BuiltInRegistries.BLOCK, resourceLocation, item.get());
        return () -> register;
    }

    @Override
    public BlockEntityType<BurlapSackEntity> getBurlapSackEntityType() {
        return BlockItems.BURLAP_SACK_ENTITY;
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
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                    type
        );
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> type) {
        EntityType<T> registered = Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                    type.build(name)
        );
        return () -> registered;
    }

    @Override
    public SoundEvent register(String name, SoundEvent event) {
        return Registry.register(
                    BuiltInRegistries.SOUND_EVENT,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                    event
        );
    }

    @Override
    public Holder<Attribute> register(String name, Attribute attribute) {
        return Registry.registerForHolder(
                    BuiltInRegistries.ATTRIBUTE,
                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name),
                    attribute
        );
    }

    @Override public Supplier<Activity> registerActivity(String name) {
        Activity register = Registry.register(BuiltInRegistries.ACTIVITY, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), new Activity(name));
        return () -> register;
    }

    @Override public <T> Supplier<MemoryModuleType<T>> registerMemoryModule(String name, Codec<T> codec) {
        MemoryModuleType<T> register = Registry.register(BuiltInRegistries.MEMORY_MODULE_TYPE, ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name), new MemoryModuleType<>(Optional.of(codec)));
        return () -> register;
    }

    @Override
    public ModelResourceLocation getModelVariant(ResourceLocation location) {
        return new ModelResourceLocation(location, "fabric_resource");
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Optional<Path> getModFeaturesDir() {
        Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(Constants.MOD_ID);
        if (optional.isEmpty())
            return Optional.empty();

        ModContainer container = optional.get();
        Optional<Path> path = container.findPath("features");
        return path;
    }
}
