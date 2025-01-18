package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CommonConfigRows extends ConfigRows {
      private final List<ConfigLabel> rows;

      public CommonConfigRows(ConfigScreen screen, Minecraft minecraft, CommonConfig config) {
            super(screen, minecraft, config);

            this.rows = getRows();
            for (ConfigLabel row : rows) {
                  addEntry(row);
            }
      }

      private List<ConfigLabel> getRows() {
            CommonConfig config = (CommonConfig) this.config;

            return List.of(
                        new ConfigLabel(Component.translatable("config.beansbackpacks.common.shorthandToolBelt")),
                        new IntConfigRow(config.shorthand_size),
                        new ItemListConfigRow(config.shorthand_additions),
                        new ConfigLabel(Component.translatable("config.beansbackpacks.common.keepInventory")),
                        new BoolConfigRow(config.keep_back_on_death),
                        new BoolConfigRow(config.keep_shorthand_on_death),
                        new ConfigLabel(Component.translatable("config.beansbackpacks.common.misc")),
                        new BoolConfigRow(config.do_nbt_stacking)
            );
      }

      @Override public void resetToDefault() {
            for (ConfigLabel row : rows)
                  row.resetToDefault();
      }

      @Override public void onSave() {
            for (ConfigLabel row : rows) {
                  row.onSave();
            }
      }
}
