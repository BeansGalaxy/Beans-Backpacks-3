package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.DisplayComponent;
import com.beansgalaxy.backpacks.components.FilterComponent;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.components.reference.ReferenceRegistry;
import com.beansgalaxy.backpacks.data.config.TraitConfig;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TraitLoader {
      private final ResourceManager resourceManager;
      private final LayeredRegistryAccess<RegistryLayer> registryAccess;
      HashMap<ResourceLocation, UnbakedTraits> unbakedMap = new HashMap<>();

      public TraitLoader(ResourceManager manager, LayeredRegistryAccess<RegistryLayer> access) {
            resourceManager = manager;
            registryAccess = access;
      }

      public void run() {
            ReferenceRegistry.REFERENCES.clear();
            resourceManager.listResources("trait_ids", in -> in.getPath().endsWith(".json"))
                        .forEach(((resourceLocation, resource) -> {
                              try {
                                    RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess.compositeAccess());
                                    ResourceLocation location = resourceLocation.withPath(path -> path.replaceFirst(".json", "").replaceFirst("trait_ids/", ""));
                                    JsonObject parse = GsonHelper.parse(resource.openAsReader());
                                    UnbakedTraits traits = readJson(parse, registryOps, location);
                                    unbakedMap.put(location, traits);

                              } catch (IOException e) {
                                    throw new RuntimeException("error while parsing trait_ids", e);
                              }
                        }));

            TraitConfig traitConfig = new TraitConfig();
            traitConfig.read();
            traitConfig.traits.forEach((string, parse) -> {
                  RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess.compositeAccess());
                  ResourceLocation location = ResourceLocation.parse(string);
                  UnbakedTraits traits = readJson(parse, registryOps, location);
                  unbakedMap.put(location, traits);
            });

            for (ResourceLocation location : unbakedMap.keySet())
                  bakeEntry(location);
      }

      private void bakeEntry(ResourceLocation location) {
            ResourceLocation thisLocation = location;
            List<ResourceLocation> chain = new ArrayList<>();

            do {
                  if (chain.contains(thisLocation))
                        break;

                  if (!unbakedMap.containsKey(thisLocation))
                        break;

                  chain.addFirst(thisLocation);
                  thisLocation = unbakedMap.get(thisLocation).parent();
            } while (thisLocation != null);

            Pair<TraitComponentKind<?>, JsonObject> unbakedTrait = null;
            ItemAttributeModifiers attributes = ItemAttributeModifiers.EMPTY;
            byte utilities = 0;
            DisplayComponent display = null;
            FilterComponent filter = FilterComponent.EMPTY;

            for (ResourceLocation locations : chain) {
                  UnbakedTraits temp = unbakedMap.get(locations);
                  Pair<TraitComponentKind<?>, JsonObject> trait = temp.traits();
                  if (trait != null) {
                        if (unbakedTrait != null) {
                              TraitComponentKind<?> kind = unbakedTrait.getFirst();
                              if (kind == trait.getFirst()) {
                                    JsonObject jsonObject = unbakedTrait.getSecond();
                                    trait.getSecond().asMap().forEach(jsonObject::add);
                              }
                              else unbakedTrait = trait;

                        }
                        else unbakedTrait = trait;
                  }
                  if (temp.attributes != null)
                        attributes = temp.attributes;
                  if (temp.utilities != -1)
                        utilities = temp.utilities;
                  if (temp.display != null)
                        display = temp.display;
                  if (temp.filter != null)
                        filter = temp.filter;
            }

            GenericTraits bakedTrait;

            if (unbakedTrait != null) {
                  TraitComponentKind<?> kind = unbakedTrait.getFirst();
                  JsonObject jsonObject = unbakedTrait.getSecond();

                  RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess.compositeAccess());
                  DataResult<? extends GenericTraits> result = kind.codec().parse(registryOps, jsonObject);
                  if (result.isError()) {
                        String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + kind + "\"; ";
                        String error = result.error().get().message();
                        Constants.LOG.warn("{}{}", message, error);
                        return;
                  }

                  bakedTrait = result.getOrThrow();
            }
            else bakedTrait = NonTrait.INSTANCE;

            ReferenceRegistry.put(location, new ReferenceRegistry(bakedTrait, attributes, utilities, display, filter));
      }

      private UnbakedTraits readJson(JsonObject parse, RegistryOps<JsonElement> registryOps, ResourceLocation location) {
            Iterator<String> iterator = parse.keySet().iterator();

            byte utilities = -1;
            FilterComponent filter = null;
            DisplayComponent display = null;
            ItemAttributeModifiers attributes = null;
            Pair<TraitComponentKind<?>, JsonObject> traits = null;
            ResourceLocation parent = null;

            while (iterator.hasNext()) {
                  String type = iterator.next();
                  JsonElement json = parse.get(type);
                  switch (type) {
                        case "parent" -> {
                              if (parent != null)
                                    continue;

                              DataResult<ResourceLocation> result = ResourceLocation.CODEC.parse(registryOps, json);
                              if (result.isError()) {
                                    String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                    String error = result.error().get().message();
                                    Constants.LOG.warn("{}{}", message, error);
                                    continue;
                              }

                              parent = result.getOrThrow();
                        }
                        case "modifiers" -> {
                              if (attributes != null)
                                    continue;

                              DataResult<ItemAttributeModifiers> result = ItemAttributeModifiers.CODEC.parse(registryOps, json);
                              if (result.isError()) {
                                    String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                    String error = result.error().get().message();
                                    Constants.LOG.warn("{}{}", message, error);
                                    continue;
                              }

                              attributes = result.getOrThrow();
                        }
                        case UtilityComponent.NAME -> {
                              if (utilities != -1)
                                    continue;

                              DataResult<Byte> result = UtilityComponent.SIZE_CODEC.parse(registryOps, json);
                              if (result.isError()) {
                                    String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                    String error = result.error().get().message();
                                    Constants.LOG.warn("{}{}", message, error);
                                    continue;
                              }

                              utilities = result.getOrThrow();
                        }
                        case DisplayComponent.NAME -> {
                              if (display != null)
                                    continue;

                              DataResult<DisplayComponent> result = DisplayComponent.CODEC.parse(registryOps, json);
                              if (result.isError()) {
                                    String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                    String error = result.error().get().message();
                                    Constants.LOG.warn("{}{}", message, error);
                                    continue;
                              }

                              display = result.getOrThrow();
                        }
                        case FilterComponent.NAME -> {
                              if (filter != null)
                                    continue;
                              
                              DataResult<FilterComponent> result = FilterComponent.CODEC.parse(registryOps, json);
                              if (result.isError()) {
                                    String message = "Failure while parsing trait_id \"" + location + "\"; Error while decoding \"" + type + "\"; ";
                                    String error = result.error().get().message();
                                    Constants.LOG.warn("{}{}", message, error);
                                    continue;
                              }
                              
                              filter = result.getOrThrow();
                        }
                        case NonTrait.NAME -> {
                        }
                        case null -> {
                        }
                        default -> {
                              TraitComponentKind<? extends GenericTraits> kind = TraitComponentKind.get(type);
                              if (kind != null && json.isJsonObject()) {
                                    traits = Pair.of(kind, json.getAsJsonObject());
                              }
                        }
                  }
            }

            return new UnbakedTraits(parent, traits, attributes, display, filter, utilities);
      }

      public record UnbakedTraits(ResourceLocation parent,
                                  Pair<TraitComponentKind<?>, JsonObject> traits,
                                  ItemAttributeModifiers attributes,
                                  DisplayComponent display,
                                  FilterComponent filter,
                                  byte utilities
      ) {
      }
      
}
