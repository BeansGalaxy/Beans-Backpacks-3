package com.beansgalaxy.backpacks.util;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public class Int2ValueMap<V> extends Int2ObjectFunctions.UnmodifiableFunction<V> implements Int2ObjectMap<V>, java.io.Serializable {
      private V value;

      public Int2ValueMap(V value) {
            super(i -> value);
            this.value = value;
      }

      @Override
      public ObjectSet<Entry<V>> int2ObjectEntrySet() {
            return ObjectSets.emptySet();
      }

      @Override
      public boolean isEmpty() {
            return true;
      }

      @Override
      public boolean containsValue(Object value) {
            return Objects.equals(this.value, value);
      }

      @Override
      public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {
            throw new UnsupportedOperationException();
      }

      @Override
      public V defaultReturnValue() {
            return value;
      }

      @Override
      public int size() {
            return 0;
      }

      @Override
      public void defaultReturnValue(V defRetValue) {
            value = defRetValue;
      }

      @Override
      public IntSet keySet() {
            return IntSets.emptySet();
      }

      @Override
      public ObjectCollection<V> values() {
            return ObjectSets.singleton(value);
      }

      @Override
      public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Int2ValueMap<?> map)) return false;
            if (!super.equals(o)) return false;
            return Objects.equals(value, map.value);
      }

      @Override
      public int hashCode() {
            return Objects.hash(super.hashCode(), value);
      }
}
