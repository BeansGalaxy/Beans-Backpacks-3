package com.beansgalaxy.backpacks.data.config.options;

public enum Orientation {
      UP,
      LEFT,
      DOWN,
      RIGHT,
      ;

      public boolean isVertical() {
            return this == UP || this == DOWN;
      }

      public boolean isPositive() {
            return this == DOWN || this == RIGHT;
      }
}
