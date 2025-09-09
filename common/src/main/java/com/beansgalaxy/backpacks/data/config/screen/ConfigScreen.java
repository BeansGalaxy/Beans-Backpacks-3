package com.beansgalaxy.backpacks.data.config.screen;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.data.config.*;
import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConfigScreen extends Screen {
      private final Map<IConfig, Function<ConfigScreen, ConfigRows>> pageConstructor;
      private final Screen lastScreen;
      private final IConfig[] pages;
      private ConfigRows[] rows;
      private ConfigRows currentPage;

      public ConfigScreen(Screen lastScreen, Map<IConfig, Function<ConfigScreen, ConfigRows>> pageConstructor) {
            super(Component.empty());
            this.pageConstructor = pageConstructor;
            this.lastScreen = lastScreen;
            this.pages = pageConstructor.keySet().toArray(new IConfig[0]);
            for (IConfig page : this.pages) {
                  page.read();
            }
      }

      public static void buildPageMap(Minecraft minecraft, ImmutableMap.Builder<IConfig, Function<ConfigScreen, ConfigRows>> map) {
            CommonConfig common = new CommonConfig();
            map.put(common, configScreen ->
                        new CommonConfigRows(configScreen, minecraft, common));

            ClientConfig client = CommonClass.CLIENT_CONFIG;
            map.put(client, configScreen ->
                        new ClientConfigRows(configScreen, minecraft, client));

            TraitConfig traits = new TraitConfig();
            map.put(traits, configScreen ->
                        new TraitConfigRows(configScreen, minecraft, traits));
      }

      @Override
      public List<? extends GuiEventListener> children() {
            List<? extends GuiEventListener> children = super.children().stream().filter(in -> !(in != currentPage && in instanceof ConfigRows)).toList();
            return children;
      }

      @Override
      protected void init() {
            super.init();
            ArrayList<ConfigRows> rows = new ArrayList<>();
            ArrayList<PageTab> tabs = new ArrayList<>();
            int totalWidth = -20;
            for (IConfig page : pages) {
                  ConfigRows row = pageConstructor.get(page).apply(this);
                  rows.add(row);

                  String path = row.config.getPath();
                  MutableComponent title = Component.translatableWithFallback("screen.beansbackpacks.config.title-" + path, path);
                  int titleWidth = minecraft.font.width(title);
                  totalWidth += 20 + titleWidth;
                  tabs.add(new PageTab(row, title, titleWidth));
            }

            this.rows = rows.toArray(ConfigRows[]::new);
            ServerSave.CONFIG.read(false);
            currentPage = this.rows[0];
            addWidgets();

            int x = (this.width - totalWidth) / 2;
            for (PageTab tab : tabs) {
                  addWidget(tab.row);

                  this.addRenderableWidget(new PlainTextButton(x, 20, tab.width, 20, tab.title,
                              in -> currentPage = tab.row, minecraft.font)
                  {
                        @Override public boolean isFocused() {
                              return currentPage == tab.row;
                        }
                  });

                  x += tab.width + 20;
            }
      }

      private record PageTab(ConfigRows row, Component title, int width) {}

      private void addWidgets() {
            int center = this.width / 2;
            this.addRenderableWidget(Button.builder(Component.translatable("screen.beansbackpacks.config.main.reset_all"), ($$0) -> {
                  currentPage.resetToDefault();
            }).bounds(center - 165, this.height - 26, 70, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("screen.beansbackpacks.config.main.undo_all"), ($$0) -> {
                  for (IConfig page : pages)
                        page.read(false);
            }).bounds(center - 80, this.height - 26, 70, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("screen.beansbackpacks.config.main.save_and_exit"), ($$0) -> {
                  for (ConfigRows row : rows) {
                        row.onSave();
                        row.config.write();
                  }

                  this.minecraft.setScreen(this.lastScreen);
            }).bounds(center + 5, this.height - 26, 160, 20).build());
      }

      @Override
      public void onClose() {
            this.minecraft.setScreen(this.lastScreen);
      }

      @Override
      public void render(GuiGraphics gui, int x, int y, float delta) {
            super.render(gui, x, y, delta);
            MutableComponent title = Component.literal(Constants.MOD_NAME).withStyle(ChatFormatting.BOLD);
            gui.drawCenteredString(font, title, minecraft.getWindow().getGuiScaledWidth() / 2, 6, 0xFFCCDDFF);
            if (currentPage != null)
                  currentPage.render(gui, x, y, delta);
      }



}
