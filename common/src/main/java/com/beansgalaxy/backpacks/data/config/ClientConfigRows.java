package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.data.config.screen.ConfigRows;
import com.beansgalaxy.backpacks.data.config.screen.ConfigScreen;
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
                        new MoveBackSlotConfigRow(config.back_slot_pos, config.back_and_utility_direction),
                        new ConfigLabel(Component.translatable("config.beansbackpacks.client.player-render")),
                        new ItemListConfigRow(config.elytra_model_equipment),
                        new BoolConfigRow(config.disable_equipable_render)
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
