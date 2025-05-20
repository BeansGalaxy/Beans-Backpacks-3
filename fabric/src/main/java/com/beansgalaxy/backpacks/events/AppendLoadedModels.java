package com.beansgalaxy.backpacks.events;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AppendLoadedModels implements PreparableModelLoadingPlugin<Collection<ResourceLocation>> {

      @Override
      public void onInitializeModelLoader(Collection<ResourceLocation> data, ModelLoadingPlugin.Context pluginContext) {
            pluginContext.addModels(data);
      }

      public static final DataLoader<Collection<ResourceLocation>> LOADER = (resourceManager, executor) ->
            CompletableFuture.supplyAsync(() -> {
                  HashSet<ResourceLocation> modelIDs = new HashSet<>();
                  appendModels(resourceManager, "backpack", modelIDs);
                  appendModels(resourceManager, "utilities", modelIDs);

                  return modelIDs;
            }, executor
      );

      private static void appendModels(ResourceManager resourceManager, String path, HashSet<ResourceLocation> modelIDs) {
            Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("models/" + path, (p_251575_) -> {
                  String s = p_251575_.getPath();
                  return s.endsWith(".json");
            });

            for(ResourceLocation resourceLocation: resourceMap.keySet()) {
                  ResourceLocation location = resourceLocation.withPath(key -> key.replaceAll("models/", "").replaceAll(".json", ""));
                  modelIDs.add(location);
            }
      }

}
