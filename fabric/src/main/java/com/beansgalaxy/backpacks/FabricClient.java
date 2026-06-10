package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.client.renderer.BackpackCapeModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackModel;
import com.beansgalaxy.backpacks.client.renderer.RenderBackpack;
import com.beansgalaxy.backpacks.client.renderer.EntityRender;
import com.beansgalaxy.backpacks.events.NetworkPackages;
import com.beansgalaxy.backpacks.screen.BurlapSackScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;

public class FabricClient implements ClientModInitializer {

      @Override
      public void onInitializeClient() {
            CommonClient.init();
            NetworkPackages.registerClient();
            
            ResourceLocation.withDefaultNamespace("dye");

            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.ACTION_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.MENUS_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.INSTANT_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.SPYGLASS_KEY);

            EntityModelLayerRegistry.registerModelLayer(RenderBackpack.BACKPACK_MODEL, BackpackModel::getTexturedModelData);
            EntityModelLayerRegistry.registerModelLayer(RenderBackpack.PACK_CAPE_MODEL, BackpackCapeModel::createBodyLayer);
            EntityRendererRegistry.register(CommonClass.BACKPACK_ENTITY.get(), EntityRender::new);
            EntityRendererRegistry.register(CommonClass.LEGACY_ENDER_ENTITY.get(), EntityRender::new);
            EntityRendererRegistry.register(CommonClass.LEGACY_WINGED_ENTITY.get(), EntityRender::new);
            
            MenuScreens.register(ModRegistry.BURLAP_SACK_MENU, BurlapSackScreen::new);

            CommonClass.CLIENT_CONFIG.read();
      }

}
