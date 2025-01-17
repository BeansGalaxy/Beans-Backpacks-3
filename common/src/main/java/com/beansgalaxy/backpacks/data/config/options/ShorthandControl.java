package com.beansgalaxy.backpacks.data.config.options;

public enum ShorthandControl {
      FIXED(false, true),
      HARD(true, true),
      MIXED(true, false),
      SOFT(false, false),
      ;

      private final boolean autoEquips, activateOnPress;

      ShorthandControl(boolean autoEquips, boolean activateOnPress) {
            this.autoEquips = autoEquips;
            this.activateOnPress = activateOnPress;
      }

      public boolean autoEquips() {
            return autoEquips;
      }

      public boolean pressKey() {
            return activateOnPress;
      }
}
