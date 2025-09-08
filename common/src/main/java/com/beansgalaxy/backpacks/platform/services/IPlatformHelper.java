package com.beansgalaxy.backpacks.platform.services;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.items.BurlapSackEntity;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.mojang.serialization.Codec;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
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

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    Supplier<Item> register(String name, Supplier<Item> item);

    Supplier<Block> registerBlock(String id, Supplier<Block> item);

    BlockEntityType<BurlapSackEntity> getBurlapSackEntityType();

    <T> DataComponentType<T> register(String name, DataComponentType<T> type);

    <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> type);

    SoundEvent register(String name, SoundEvent event);

    Holder<Attribute> register (String name, Attribute attribute);

    Supplier<Activity> registerActivity(String name);

    <T> Supplier<MemoryModuleType<T>> registerMemoryModule(String name, Codec<T> codec);

    void send(Network2C network, Packet2C packet2C, ServerPlayer to);

    void send(Network2C network, Packet2C packet2C, MinecraftServer server);

    void send(Network2C network, Packet2C packet2C, MinecraftServer server, ServerPlayer player);

    void send(Network2S network, Packet2S packet2S);

    ModelResourceLocation getModelVariant(ResourceLocation location);

    Path getConfigDir();

    Optional<Path> getModFeaturesDir();

    default Path getConfigPath() {
        return getConfigDir().resolve(Constants.MOD_ID);
    }
}