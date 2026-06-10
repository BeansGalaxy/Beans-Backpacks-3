package com.beansgalaxy.backpacks.access;

import com.beansgalaxy.backpacks.util.ViewableBackpack;

public interface ViewableAccessor {
      ViewableBackpack getViewable();
      
      default void setViewable(ViewableBackpack viewable) {
      
      }
}
