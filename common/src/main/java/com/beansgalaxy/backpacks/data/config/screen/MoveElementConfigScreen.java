package com.beansgalaxy.backpacks.data.config.screen;

import com.beansgalaxy.backpacks.data.config.options.Orientation;
import com.beansgalaxy.backpacks.data.config.types.EnumConfigVariant;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiConsumer;

public class MoveElementConfigScreen extends Screen {
      private final ResourceLocation background;
      private final Screen lastScreen;
      private final int bgU;
      private final int bgV;
      private final int bgWidth;
      private final int bgHeight;
      private final TriConsumer<Integer, Integer, Orientation> onSave;
      private final int elementW;
      private final int elementH;
      private int elementX;
      private int elementY;
      private int topPos;
      private int leftPos;
      private final int childSlots;
      private Orientation orientation;

      public MoveElementConfigScreen(Screen lastScreen, ResourceLocation background, TriConsumer<Integer, Integer, Orientation> onSave, int elementX, int elementY, int elementW, int elementH, int bgWidth, int bgHeight, int bgU, int bgV, int childSlots, Orientation orientation) {
            super(Component.empty());
            this.lastScreen = lastScreen;
            this.background = background;
            this.bgU = bgU;
            this.bgV = bgV;
            this.onSave = onSave;
            this.elementW = elementW;
            this.elementH = elementH;
            this.bgWidth = bgWidth;
            this.bgHeight = bgHeight;
            this.elementX = elementX;
            this.elementY = elementY;
            this.childSlots = childSlots;
            this.orientation = orientation;
      }

      @Override
      public boolean mouseDragged(double x1, double y1, int i, double x2, double y2) {
            Optional<GuiEventListener> childAt = getChildAt(x1, y1);

            if (childAt.isEmpty() && i == 0) {
                  elementX = (int) (x1 - leftPos) - elementW / 2;
                  elementY = (int) (y1 - topPos) - elementH / 2;
            }
            return super.mouseDragged(x1, y1, i, x2, y2);
      }

      @Override
      protected void init() {
            super.init();

            this.leftPos = (int) (bgWidth / -2.0 + width / 2.0);
            this.topPos = (int) (bgHeight / -4.0 + height / 4.0);

            int center = width / 2;
            Button save = Button.builder(Component.translatable("screen.beansbackpacks.move_element.save_and_close"), in -> {
                  onSave.accept(elementX, elementY, orientation);
                  onClose();
            }).bounds(center + 5, height - 26, 80, 20).build();

            Button exit = Button.builder(Component.translatable("screen.beansbackpacks.move_element.cancel_and_exit"), in -> {
                  onClose();
            }).bounds(center - 85, height - 26, 80, 20).build();

            Button rotate = Button.builder(Component.translatable("screen.beansbackpacks.move_element.rotate"), in -> {
                  Orientation lastValue = Orientation.RIGHT;
                  for (Orientation value : Orientation.values()) {
                        if (value == orientation) {
                              orientation = lastValue;
                              return;
                        }
                        else lastValue = value;
                  }
            }).bounds(center + 95, height - 26, 20, 20).build();

            addRenderableWidget(save);
            addRenderableWidget(exit);
            addRenderableWidget(rotate);
      }

      @Override
      public void onClose() {
            minecraft.setScreen(lastScreen);
      }

      @Override
      public void render(GuiGraphics gui, int x, int y, float delta) {
            super.renderBackground(gui, x, y, delta);
            int eleX = leftPos + elementX;
            int eleY = topPos + elementY;
            gui.fill(eleX, eleY, elementW + eleX, elementH + eleY, 300, 0xFFEE3333);
            boolean positive = orientation.isPositive();
            for (int i = 0; i < childSlots; i++) {
                  int chiY;
                  int chiX;
                  if (orientation.isVertical()) {
                        int mod = (elementH + 2) * (i + 1);
                        chiY = positive ? eleY + mod : eleY - mod;
                        chiX = eleX;
                  }
                  else {
                        chiY = eleY;
                        int mod = (elementW + 2) * (i + 1);
                        chiX = positive ? eleX + mod : eleX - mod;
                  }

                  gui.fill(chiX, chiY, elementW + chiX, elementH + chiY, 300, 0xFFDDCC33);
            }
            gui.blit(background, leftPos, topPos, bgU, bgV, bgWidth, bgHeight);
            super.render(gui, x, y, delta);
      }

      public static class Builder {
            private TriConsumer<Integer, Integer, Orientation> onSave = (x, y, o) -> {};
            private ResourceLocation background = null;
            private int elementX = 0;
            private int elementY = 0;
            private int elementW = 1;
            private int elementH = 1;
            private int bgW = 0;
            private int bgH = 0;
            private int bgU = 0;
            private int bgV = 0;
            private int childSlots = 0;
            private Orientation orientation;

            public static Builder create() {
                  return new Builder();
            }

            public Builder elementPos(int x, int y) {
                  elementX = x;
                  elementY = y;
                  return this;
            }

            public Builder elementSize(int width, int height) {
                  elementW = width;
                  elementH = height;
                  return this;
            }

            public Builder backgroundSize(int width, int height) {
                  bgW = width;
                  bgH = height;
                  return this;
            }

            public Builder backgroundUV(int x, int y) {
                  bgU = x;
                  bgV = y;
                  return this;
            }

            public Builder childSlots(int size) {
                  childSlots = size;
                  return this;
            }

            public Builder background(ResourceLocation background) {
                  this.background = background;
                  return this;
            }

            public Builder onSave(TriConsumer<Integer, Integer, Orientation> onClose) {
                  this.onSave = onClose;
                  return this;
            }

            public MoveElementConfigScreen build(Screen lastScreen) {
                  return new MoveElementConfigScreen(lastScreen, background, onSave, elementX, elementY, elementW, elementH, bgW, bgH, bgU, bgV, childSlots, orientation);
            }

            public Builder orientation(Orientation orientation) {
                  this.orientation = orientation;
                  return this;
            }
      }
}
