package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.client.renderer.BackpackCapeModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackModel;
import com.beansgalaxy.backpacks.client.renderer.BackpackRender;
import com.beansgalaxy.backpacks.client.renderer.EntityRender;
import com.beansgalaxy.backpacks.events.AppendLoadedModels;
import com.beansgalaxy.backpacks.events.NetworkPackages;
import com.beansgalaxy.backpacks.events.TooltipImageEvent;
import com.beansgalaxy.backpacks.util.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

public class FabricClient implements ClientModInitializer {

      @Override
      public void onInitializeClient() {
            CommonClient.init();
            NetworkPackages.registerClient();
            PreparableModelLoadingPlugin.register(AppendLoadedModels.LOADER, new AppendLoadedModels());

            TooltipComponentCallback.EVENT.register(new TooltipImageEvent());
            ColorProviderRegistry.ITEM.register(CommonClient.LEATHER_BACKPACK_ITEM_COLOR, ModItems.LEATHER_BACKPACK.get());
            ColorProviderRegistry.ITEM.register(CommonClient.BUNDLE_ITEM_COLOR, ModItems.BUNDLE.get());
            ColorProviderRegistry.ITEM.register(CommonClient.BULK_POUCH_ITEM_COLOR, ModItems.BULK_POUCH.get());
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("no_gui"), CommonClient.NO_GUI_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("utilities"), CommonClient.UTILITIES_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("fullness"), CommonClient.FULLNESS_ITEM_PREDICATE);
            ItemProperties.registerGeneric(ResourceLocation.withDefaultNamespace("eating"), CommonClient.EATING_TRAIT_ITEM_PREDICATE);
            ItemProperties.register(ModItems.ENDER_POUCH.get(), ResourceLocation.withDefaultNamespace("searching"), CommonClient.ENDER_SEARCHING_PREDICATE);

            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.ACTION_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.MENUS_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.INSTANT_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.SHORTHAND_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.SECONDARY_KEY);
            KeyBindingHelper.registerKeyBinding(KeyPress.INSTANCE.UTILITY_KEY);

            EntityModelLayerRegistry.registerModelLayer(BackpackRender.BACKPACK_MODEL, BackpackModel::getTexturedModelData);
            EntityModelLayerRegistry.registerModelLayer(BackpackRender.PACK_CAPE_MODEL, BackpackCapeModel::createBodyLayer);
            EntityRendererRegistry.register(CommonClass.BACKPACK_ENTITY.get(), EntityRender::new);
            EntityRendererRegistry.register(CommonClass.LEGACY_ENDER_ENTITY.get(), EntityRender::new);
            EntityRendererRegistry.register(CommonClass.LEGACY_WINGED_ENTITY.get(), EntityRender::new);

            CommonClass.CLIENT_CONFIG.read();
      }

}
