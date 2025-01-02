package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.data.config.options.ToolBeltHUD;
import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
import com.beansgalaxy.backpacks.data.config.options.ShorthandHUD;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClientConfigRows extends ConfigRows {
      private final List<ConfigLabel> rows;

      public ClientConfigRows(ConfigScreen screen, Minecraft minecraft, ClientConfig config) {
            super(screen, minecraft, config);

            this.rows = getRows();
            for (ConfigLabel row : rows)
                  addEntry(row);
      }

      private List<ConfigLabel> getRows() {
            ClientConfig config = (ClientConfig) this.config;
            return List.of(
                        new EnumConfigRow<>(config.shorthand_hud_location, ShorthandHUD.values()),
                        new EnumConfigRow<>(config.tool_belt_hud_visibility, ToolBeltHUD.values()),
                        new ConfigLabel(Component.translatable("config.beansbackpacks.client.player-render")),
                        new ItemListConfigRow(config.elytra_model_equipment),
                        new BoolConfigRow(config.disable_equipable_render),
                        new BoolConfigRow(config.disable_shorthand_render)
            );
      }

      @Override
      public void resetToDefault() {
            for (ConfigLabel row : rows)
                  row.resetToDefault();
      }

      @Override public void onSave() {
            for (ConfigLabel row : rows)
                  row.onSave();
      }
}
