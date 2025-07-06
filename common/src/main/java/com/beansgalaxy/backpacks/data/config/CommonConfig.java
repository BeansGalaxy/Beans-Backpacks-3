package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.*;

public class CommonConfig implements IConfig {
      @Deprecated(since = "0.8-beta") public HSetConfigVariant<Item> tool_belt_additions;
      public BoolConfigVariant keep_back_on_death;
      public BoolConfigVariant do_nbt_stacking;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  tool_belt_additions = HSetConfigVariant.Builder.create(Constants::shortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in))).comment("!!DOES NOTING!! SOON TO BE REMOVED!")
                              .build("tool_belt_additions"),
                  keep_back_on_death = new BoolConfigVariant("keep_back_on_death", false, "On death, the player will drop their equipment in the Back Slot"),
                  do_nbt_stacking = new BoolConfigVariant("do_nbt_stacking", false, "Matching items which do not stack due to differing nbt now can stack")
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
