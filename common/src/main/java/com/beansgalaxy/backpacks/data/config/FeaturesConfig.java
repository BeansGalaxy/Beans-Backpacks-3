package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.ConfigLine;
import com.beansgalaxy.backpacks.data.config.types.ListConfigVariant;

import java.util.*;

public class FeaturesConfig implements IConfig {
      public ListConfigVariant<String> enabled_features;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  enabled_features = ListConfigVariant.stringList().build("enabled_features")
      };

      @Override
      public String getPath() {
            return "features";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }
}
