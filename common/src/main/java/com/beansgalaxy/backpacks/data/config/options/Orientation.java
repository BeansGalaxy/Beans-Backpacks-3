package com.beansgalaxy.backpacks.data.config.options;

public enum Orientation {
      Up,
      Left,
      Down,
      Right,
      ;

      public boolean isVertical() {
            return this == Up || this == Down;
      }

      public boolean isPositive() {
            return this == Down || this == Right;
      }
}
