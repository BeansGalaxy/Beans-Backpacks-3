package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.*;

public class CommonConfig implements IConfig {
      public IntConfigVariant tool_belt_size;
      public IntConfigVariant shorthand_size;
      public HSetConfigVariant<Item> tool_belt_additions;
      public HSetConfigVariant<Item> shorthand_additions;
      public BoolConfigVariant tool_belt_break_items;
      public BoolConfigVariant keep_back_on_death;
      public BoolConfigVariant keep_tool_belt_on_death;
      public BoolConfigVariant keep_shorthand_on_death;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  tool_belt_size = new IntConfigVariant("tool_belt_size", 2, 0, 8),
                  shorthand_size = new IntConfigVariant("shorthand_size", 1, 0, 8),
                  tool_belt_additions = HSetConfigVariant.Builder.create(Constants::itemShortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in)))
                              .build("tool_belt_additions"),
                  shorthand_additions = HSetConfigVariant.Builder.create(Constants::itemShortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in)))
                              .build("shorthand_additions"),
                  tool_belt_break_items = new BoolConfigVariant("tool_belt_break_items", false, "Will the Tool Belt continue to use a tool until it breaks"),
                  keep_back_on_death = new BoolConfigVariant("keep_back_on_death", false, "On death, the player will drop their equipment in the Back Slot"),
                  keep_tool_belt_on_death = new BoolConfigVariant("keep_tool_belt_on_death", false, "On death, the player will drop their equipment in the Tool Belt"),
                  keep_shorthand_on_death = new BoolConfigVariant("keep_shorthand_on_death", false, "On death, the player will drop their equipment in the Shorthand")
      };

      @Override
      public String getPath() {
            return "common";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }

}
