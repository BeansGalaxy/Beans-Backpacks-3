package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.client.renderer.BackpackCapeModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackModel;
import com.beansgalaxy.backpacks.client.renderer.RenderBackpack;
import com.beansgalaxy.backpacks.client.renderer.EntityRender;
import com.beansgalaxy.backpacks.screen.BurlapSackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Map;

@Mod(value = Constants.MOD_ID, dist = Dist.CLIENT)
public class NeoForgeClient {

      public NeoForgeClient(IEventBus eventBus, ModContainer container) {
            CommonClient.init();
      }

      @EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
      public static class ModEvents {

            @SubscribeEvent
            public static void registerScreens(RegisterMenuScreensEvent event) {
                  event.register(ModRegistry.BURLAP_SACK_MENU.get(), BurlapSackScreen::new);
            }

            @SubscribeEvent
            public static void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
                  event.registerLayerDefinition(RenderBackpack.BACKPACK_MODEL, BackpackModel::getTexturedModelData);
                  event.registerLayerDefinition(RenderBackpack.PACK_CAPE_MODEL, BackpackCapeModel::createBodyLayer);
            }

            @SubscribeEvent
            public static void registerEntityRenderer(final EntityRenderersEvent.RegisterRenderers event) {
                  event.registerEntityRenderer(CommonClass.BACKPACK_ENTITY.get(), EntityRender::new);
            }

            @SubscribeEvent
            public static void registerKeys(RegisterKeyMappingsEvent event) {
                  event.register(KeyPress.INSTANCE.ACTION_KEY);
                  event.register(KeyPress.INSTANCE.MENUS_KEY);
                  event.register(KeyPress.INSTANCE.INSTANT_KEY);
                  event.register(KeyPress.INSTANCE.SPYGLASS_KEY);
            }

            @SubscribeEvent
            public static void clientSetup(FMLClientSetupEvent event) {
                  event.enqueueWork(() -> {
                        CommonClass.CLIENT_CONFIG.read();
                  });
            }
      }
}
