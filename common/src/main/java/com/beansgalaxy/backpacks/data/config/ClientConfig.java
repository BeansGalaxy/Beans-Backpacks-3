package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.options.ToolBeltHUD;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import com.beansgalaxy.backpacks.data.config.options.ShorthandHUD;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.List;

public class ClientConfig implements IConfig {
      public EnumConfigVariant<ShorthandHUD> shorthand_hud_location;
      public HSetConfigVariant<Item> elytra_model_equipment;
      public EnumConfigVariant<ToolBeltHUD> tool_belt_hud_visibility;
      public BoolConfigVariant disable_equipable_render;
      public BoolConfigVariant disable_shorthand_render;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  shorthand_hud_location = new EnumConfigVariant<>("shorthand_hud_location", ShorthandHUD.NEAR_CENTER, ShorthandHUD.values()),
                  tool_belt_hud_visibility = new EnumConfigVariant<>("tool_belt_hud_visibility", ToolBeltHUD.CONTEXT, ToolBeltHUD.values()),
                  elytra_model_equipment = HSetConfigVariant.Builder.create(Constants::itemShortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                                                                 .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in)))
                                                                 .defauString("minecraft:elytra")
                                                                 .comment("effects the position of the backpack on the player's back while these items are equipped in the chestplate slot")
                                                                 .build("elytra_model_equipment"),
                  disable_equipable_render = new BoolConfigVariant("disable_backpack_render", false, "Disables backpacks and \"beansbackpacks:equipable\" rendering on the player"),
                  disable_shorthand_render = new BoolConfigVariant("disable_shorthand_render", false, "Disables shorthand item rendering on the player's back")
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
