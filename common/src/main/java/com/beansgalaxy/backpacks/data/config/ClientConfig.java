package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.options.Orientation;
import com.beansgalaxy.backpacks.data.config.options.ShorthandControl;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import com.beansgalaxy.backpacks.data.config.options.ShorthandHUD;
import com.google.gson.JsonElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.List;

public class ClientConfig implements IConfig {
      public EnumConfigVariant<ShorthandHUD> shorthand_hud_location;
      public HSetConfigVariant<Item> elytra_model_equipment;
      public BoolConfigVariant disable_equipable_render;
//      public BoolConfigVariant disable_shorthand_render;
      public ListConfigVariant<Integer> back_slot_pos;
      public EnumConfigVariant<Orientation> back_and_utility_direction;
      public EnumConfigVariant<ShorthandControl> shorthand_control;
      public BoolConfigVariant shorthand_breaks_tool;
//      public ListConfigVariant<Integer> shorthand_slot_pos;
//      public EnumConfigVariant<Orientation> shorthand_slots_direction;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  shorthand_hud_location = new EnumConfigVariant<>("shorthand_hud_location", ShorthandHUD.NEAR_CENTER, ShorthandHUD.values()),
                  elytra_model_equipment = HSetConfigVariant.Builder.create(Constants::shortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                                                                 .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in))).defauString("minecraft:elytra")
                                                                 .comment("effects the position of the backpack on the player's back while these items are equipped in the chestplate slot")
                                                                 .build("elytra_model_equipment"),
                  disable_equipable_render = new BoolConfigVariant("disable_backpack_render", false, "Disables backpacks and \"beansbackpacks:equipable\" rendering on the player"),
//                  disable_shorthand_render = new BoolConfigVariant("disable_shorthand_render", false, "Disables shorthand item rendering on the player's back"),
                  back_slot_pos = ListConfigVariant.create(String::valueOf, JsonElement::getAsInt)
                              .defau(77, 44).valid(in -> in.size() == 2).build("back_slot_pos"),
                  back_and_utility_direction = new EnumConfigVariant<>("back_and_utility_direction", Orientation.UP, Orientation.values()),
//                  shorthand_slot_pos = ListConfigVariant.create(String::valueOf, JsonElement::getAsInt)
//                              .defau(77, 44).valid(in -> in.size() == 2).build("shorthand_slot_pos"),
//                  shorthand_slots_direction = new EnumConfigVariant<>("shorthand_slots_direction", Orientation.LEFT, Orientation.values())
                  shorthand_control = new EnumConfigVariant<>("shorthand_control", ShorthandControl.HARD, ShorthandControl.values()),
                  shorthand_breaks_tool = new BoolConfigVariant("shorthand_breaks_tool", false, "Will the Shorthand continue to use a tool until it breaks"),
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
