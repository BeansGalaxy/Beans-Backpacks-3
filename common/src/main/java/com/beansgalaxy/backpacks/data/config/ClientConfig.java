package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.options.Orientation;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.List;

public class ClientConfig implements IConfig {
      public HSetConfigVariant<Item> elytra_model_equipment;
      public BoolConfigVariant disable_equipable_render;
      public ListConfigVariant<Integer> back_slot_pos;
      public EnumConfigVariant<Orientation> back_and_utility_direction;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  elytra_model_equipment = HSetConfigVariant.Builder.create(Constants::shortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                                                                 .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in))).defauString("minecraft:elytra")
                                                                 .comment("effects the position of the backpack on the player's back while these items are equipped in the chestplate slot")
                                                                 .build("elytra_model_equipment"),
                  disable_equipable_render = new BoolConfigVariant("disable_backpack_render", false, "Disables backpacks and \"beansbackpacks:equipable\" rendering on the player"),
                  back_slot_pos = ListConfigVariant.create(String::valueOf, JsonElement::getAsInt)
                              .defau(77, 44).valid(in -> in.size() == 2).build("back_slot_pos"),
                  back_and_utility_direction = new EnumConfigVariant<>("back_and_utility_direction", Orientation.UP, Orientation.values()),
      };

      @Override
      public String getPath() {
            return "client";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }

}
