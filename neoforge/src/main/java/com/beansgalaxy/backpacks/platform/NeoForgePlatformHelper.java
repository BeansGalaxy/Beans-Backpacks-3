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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgePlatformHelper implements IPlatformHelper {
      
      public static final DeferredRegister.Blocks
            BLOCK_REGISTRY = DeferredRegister.createBlocks(Constants.MOD_ID);
      public static final DeferredRegister.Items
            ITEMS_REGISTRY = DeferredRegister.createItems(Constants.MOD_ID);
      public static final DeferredRegister.DataComponents
            COMPONENTS_REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Constants.MOD_ID);
      public static final DeferredRegister<EntityType<?>>
            ENTITY_REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Constants.MOD_ID);
      public static final DeferredRegister<SoundEvent>
            SOUND_REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Constants.MOD_ID);
      public static final DeferredRegister<Attribute>
            ATTRIBUTE_REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, Constants.MOD_ID);
      public static final DeferredRegister<Activity>
            ACTIVITY_REGISTRY = DeferredRegister.create(BuiltInRegistries.ACTIVITY, Constants.MOD_ID);
      public static final DeferredRegister<MemoryModuleType<?>>
            MEMORY_MODULE_REGISTRY = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, Constants.MOD_ID);
      
      @Override
      public String getPlatformName() {
            return "NeoForge";
      }
      
      @Override
      public boolean isModLoaded(String modId) {
            return ModList.get().isLoaded(modId);
      }
      
      @Override
      public boolean isDevelopmentEnvironment() {
            return !FMLLoader.getCurrent().isProduction();
      }
      
      @Override
      public Holder<Item> register(String name, Function<Item.Properties, Item> item) {
            return ITEMS_REGISTRY.registerItem(name, item);
      }
      
      @Override
      public Supplier<BlockItem> registerBlock(String id, Function<BlockBehaviour.Properties, Block> block) {
            DeferredBlock<Block> register = NeoForgePlatformHelper.BLOCK_REGISTRY.registerBlock(id, block);
            return ITEMS_REGISTRY.registerSimpleBlockItem(register);
      }
      
      @Override
      public BlockEntityType<? extends AbstractBurlapSackEntity> getBurlapSackEntityType() {
            return ModRegistry.BURLAP_SACK_ENTITY.get();
      }
      
      @Override
      public <T> DataComponentType<T> register(String name, DataComponentType<T> type) {
            COMPONENTS_REGISTRY.register(name, () -> type);
            return type;
      }
      
      public <T extends Entity> Supplier<EntityType<T>> register(String name, EntityType.Builder<T> type) {
            ResourceKey<EntityType<?>> entityType = ResourceKey.create(Registries.ENTITY_TYPE, Constants.defaultLocation(name));
            return ENTITY_REGISTRY.register(name, () -> type.build(entityType));
      }
      
      @Override
      public SoundEvent register(String name, SoundEvent event) {
            SOUND_REGISTRY.register(name, () -> event);
            return event;
      }
      
      @Override
      public Holder<Attribute> register(String name, Attribute attribute) {
            return ATTRIBUTE_REGISTRY.register(name, () -> attribute);
      }
      
      @Override
      public Supplier<Activity> registerActivity(String name) {
            return ACTIVITY_REGISTRY.register(name, () -> new Activity(name));
      }
      
      @Override
      public <T> Supplier<MemoryModuleType<T>> registerMemoryModule(String name, Codec<T> codec) {
            return MEMORY_MODULE_REGISTRY.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
      }
      
      @Override
      public void send(Network2C network, Packet2C packet2C, ServerPlayer to) {
            PacketDistributor.sendToPlayer(to, packet2C);
      }
      
      @Override
      public void send(Network2C network, Packet2C packet2C, MinecraftServer server) {
            PacketDistributor.sendToAllPlayers(packet2C);
      }
      
      @Override
      public void send(Network2C network, Packet2C packet2C, MinecraftServer server, ServerPlayer player) {
            PacketDistributor.sendToPlayersTrackingEntity(player, packet2C);
      }
      
      @Override
      public void send(Network2S network, Packet2S packet2S) {
            ClientPacketDistributor.sendToServer(packet2S);
      }
      
      @Override
      public Path getConfigDir() {
            return FMLPaths.CONFIGDIR.get();
      }
      
      @Override
      public MenuType<BurlapSackMenu> getBurlapSackMenuType() {
            return ModRegistry.BURLAP_SACK_MENU.get();
      }
      
      @Override
      public BlockEntity createBurlapSackEntity(BlockPos pos, BlockState state) {
            return new BurlapSackEntity(pos, state);
      }
      
}