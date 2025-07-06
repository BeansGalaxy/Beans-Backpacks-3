package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.network.Network2C;
import com.beansgalaxy.backpacks.network.Network2S;
import com.beansgalaxy.backpacks.network.clientbound.ConfigureConfig;
import com.beansgalaxy.backpacks.network.clientbound.ConfigureReferences;
import com.beansgalaxy.backpacks.network.clientbound.Packet2C;
import com.beansgalaxy.backpacks.network.serverbound.Packet2S;
import com.beansgalaxy.backpacks.platform.NeoForgePlatformHelper;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.util.ModItems;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(Constants.MOD_ID)
public class NeoForgeMain {

    public static final DataComponentType<FluidStack>
                DATA_FLUID = Traits.register("data_fluid", FluidStack.CODEC, FluidStack.STREAM_CODEC);

    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS.key(), Constants.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TAB_REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public NeoForgeMain(IEventBus eventBus) {
        NeoForgePlatformHelper.ITEMS_REGISTRY.register(eventBus);
        NeoForgePlatformHelper.SOUND_REGISTRY.register(eventBus);
        NeoForgePlatformHelper.COMPONENTS_REGISTRY.register(eventBus);
        NeoForgePlatformHelper.ENTITY_REGISTRY.register(eventBus);
        NeoForgePlatformHelper.ATTRIBUTE_REGISTRY.register(eventBus);
        NeoForgePlatformHelper.ACTIVITY_REGISTRY.register(eventBus);
        NeoForgePlatformHelper.MEMORY_MODULE_REGISTRY.register(eventBus);
        ENTITY_SERIALIZERS.register(eventBus);
        ENTITY_SERIALIZERS.register("placeable_backpack", BackpackEntity.PLACEABLE::serializer);
        CREATIVE_TAB_REGISTRY.register(eventBus);
        CREATIVE_TAB_REGISTRY.register("backpacks",
                    () -> ModItems.CREATIVE_TAB.apply(CreativeModeTab.builder()).build());
        CommonClass.init();
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void registerRegisterPayloads(final RegisterPayloadHandlersEvent event) {
            PayloadRegistrar registrar = event.registrar("1");
            for (Network2S network : Network2S.values()) {
                register(registrar, network.packet);
            }
            for (Network2C value : Network2C.values()) {
                register(registrar, value.packet);
            }
        }

        private static <M extends Packet2C> void register(PayloadRegistrar registrar, Network2C.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, M> packet) {
            registrar.playToClient(packet.type, packet, (m, iPayloadContext) ->
                        iPayloadContext.enqueueWork(m::handle)
            );
        }

        private static <M extends Packet2S> void register(PayloadRegistrar registrar, Network2S.DynamicLoaderPacket<? super RegistryFriendlyByteBuf, M> packet) {
            registrar.playToServer(packet.type, packet, (m, iPayloadContext) ->
                        iPayloadContext.enqueueWork(() ->
                                    m.handle(iPayloadContext.player())
                        )
            );
        }
    }

    @EventBusSubscriber(modid = Constants.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
    public static class GameEvents {

        @SubscribeEvent
        public static void serverStartingEvent(final ServerStartingEvent event) {
            ServerSave.CONFIG.read();
        }

        @SubscribeEvent
        public static void syncDataPacks(final OnDatapackSyncEvent event) {
            event.getRelevantPlayers().forEach(ConfigureReferences::send);
            ServerPlayer player = event.getPlayer();
            if (player != null)
                ConfigureConfig.send(player);
        }

        @SubscribeEvent
        public static void serverStartedEvent(final ServerStartedEvent event) {
            MinecraftServer server = event.getServer();
            ServerSave.getSave(server, false);
        }
    }
}