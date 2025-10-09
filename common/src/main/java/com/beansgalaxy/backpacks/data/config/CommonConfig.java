package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.options.BackpackOnDeath;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

import java.util.*;

public class CommonConfig implements IConfig {
      @Deprecated(since = "0.8-beta") public HSetConfigVariant<Item> tool_belt_additions;
      public EnumConfigVariant<BackpackOnDeath> keep_back_on_death;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  tool_belt_additions = HSetConfigVariant.Builder.create(Constants::shortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in))).comment("!!DOES NOTING!! SOON TO BE REMOVED!")
                              .build("tool_belt_additions"),
                  keep_back_on_death = new EnumConfigVariant<>("keep_back_on_death", BackpackOnDeath.KeepInventory, BackpackOnDeath.values(), "On death, will the player keep/drop their backpack") {
                        @Override @Deprecated(since = "0.11-beta")
                        public void decode(JsonObject jsonObject) {
                              if (!jsonObject.has(name))
                                    return;
                              
                              JsonElement jsonElement = jsonObject.get(name);
                              
                              try
                              {
                                    boolean keepBackpack = jsonElement.getAsBoolean();
                                    value = keepBackpack ? BackpackOnDeath.Always: BackpackOnDeath.KeepInventory;
                              }
                              catch (UnsupportedOperationException e)
                              {
                                    String string = GsonHelper.getAsString(jsonObject, name);
                                    for (BackpackOnDeath value : BackpackOnDeath.values()) {
                                          if (value.name().equals(string))
                                                this.value = value;
                                    }
                              }
                        }
                  },
      };

      @Override
      public String getPath() {
            return "common";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }
      
      public boolean keepBackpack(Level level) {
            return switch (keep_back_on_death.get()) {
                  case Always -> true;
                  case Never -> false;
                  default -> level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);
            };
      }
}
