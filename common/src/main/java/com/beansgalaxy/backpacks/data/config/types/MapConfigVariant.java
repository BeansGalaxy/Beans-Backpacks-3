package com.beansgalaxy.backpacks.data.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MapConfigVariant<KEY, ENTRY> extends ConfigVariant<HashMap<String, ENTRY>> {
      private final Function<KEY, String> keyEncode;
      private final Function<String, KEY> keyDecode;
      private final Function<ENTRY, JsonElement> entryEncode;
      private final Function<JsonElement, ENTRY> entryDecode;
      public final BiPredicate<String, ENTRY> validate;
      public final UnaryOperator<ENTRY> clamp;
      private final HashMap<String, ENTRY> example;

      protected MapConfigVariant(String name, HashMap<String, ENTRY> defau, String comment,
                                 Function<KEY, String> keyEncode, Function<String, KEY> keyDecode,
                                 Function<ENTRY, JsonElement> entryEncode, Function<JsonElement, ENTRY> entryDecode,
                                 BiPredicate<String, ENTRY> validate, UnaryOperator<ENTRY> clamp, HashMap<String, ENTRY> example)
      {
            super(name, defau, comment);
            this.value = new HashMap<>(defau);
            this.keyEncode = keyEncode;
            this.keyDecode = keyDecode;
            this.entryEncode = entryEncode;
            this.entryDecode = entryDecode;
            this.validate = validate;
            this.clamp = clamp;
            this.example = example;
      }

      public static <K, E> Builder<K, E> create(Function<K, String> keyEncode, Function<String, K> keyDecode, Function<E, JsonElement> entryEncode, Function<JsonElement, E> entryDecode) {
            return new Builder<>(keyEncode, keyDecode, entryEncode, entryDecode);
      }

      public static <E> Builder<String, E> create(Function<E, JsonElement> entryEncode, Function<JsonElement, E> entryDecode) {
            Function<String, String> transcode = in -> in;
            return new Builder<>(transcode, transcode, entryEncode, entryDecode);
      }


      public boolean contains(KEY key) {
            return value.containsKey(keyEncode.apply(key));
      }

      public ENTRY get (KEY key) {
            return value.get(keyEncode.apply(key));
      }

      public void put(String key, ENTRY entry) {
            value.put(key, entry);
      }

      @Override
      public String comment(int whiteSpace) {
            return "";
      }

      @Override
      public String encode() {
            String formattedName = toString();
            StringBuilder sb = new StringBuilder().append(formattedName);
            sb.append("{");
            if (!comment.isBlank())
                  sb.append(" ".repeat(Math.max(0, 34 - formattedName.length())))
                              .append("// ").append(comment);

            writeEntries(sb);
            writeExamples(sb);
            sb.append("\n  }");
            return sb.toString();
      }

      private void writeEntries(StringBuilder sb) {
            Iterator<String> iterator = value.keySet().iterator();
            while (iterator.hasNext()) {
                  String key = iterator.next();
                  ENTRY entry = value.get(key);
                  if (!validate.test(key, entry)) continue;

                  sb.append("\n    \"");
                  sb.append(key).append("\": ").append(entryEncode.apply(entry));
                  if (iterator.hasNext())
                        sb.append(",");
            }
      }

      private void writeExamples(StringBuilder sb) {
            HashMap<String, ENTRY> map = new HashMap<>(example);
            for (String key : value.keySet())
                  map.remove(key);

            Iterator<String> iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                  String key = iterator.next();
                  ENTRY entry = map.get(key);
                  sb.append("\n    //\"");
                  sb.append(key).append("\": ").append(entryEncode.apply(entry));
                  if (iterator.hasNext())
                        sb.append(",");
            }
      }

      @Override
      public void decode(JsonObject jsonObject) {
            if (!jsonObject.has(name)) return;

            value.clear();
            JsonObject jsonValue = jsonObject.getAsJsonObject(name);
            for (String key : jsonValue.keySet()) {
                  JsonElement entry = jsonValue.get(key);
                  ENTRY appliedEntry = entryDecode.apply(entry);
                  put(key, appliedEntry);
            }
      }

      public static class Builder<K, E> {
            private final HashMap<String, E> defau = new HashMap<>();
            private final HashMap<String, E> example = new HashMap<>();
            private final Function<K, String> keyEncode;
            private final Function<String, K> keyDecode;
            private final Function<E, JsonElement> entryEncode;
            private final Function<JsonElement, E> entryDecode;
            private BiPredicate<String, E> validator = (k, e) -> true;
            private UnaryOperator<E> clamp = e -> e;
            private String comment = "";

            private Builder(Function<K, String> keyEncode, Function<String, K> keyDecode, Function<E, JsonElement> entryEncode, Function<JsonElement, E> entryDecode) {
                  this.keyEncode = keyEncode;
                  this.keyDecode = keyDecode;
                  this.entryEncode = entryEncode;
                  this.entryDecode = entryDecode;
            }

            public Builder<K, E> defau(String[] keys, E[] entries) {
                  int size = Math.min(keys.length, entries.length);
                  for (int i = 0; i < size; i++)
                        defau.put(keys[i], entries[i]);
                  return this;
            }

            public Builder<K, E> example(String[] keys, E[] entries) {
                  int size = Math.min(keys.length, entries.length);
                  for (int i = 0; i < size; i++)
                        example.put(keys[i], entries[i]);
                  return this;
            }

            public Builder<K, E> validate(BiPredicate<String, E> validate) {
                  this.validator = validate;
                  return this;
            }

            public Builder<K, E> clamp(UnaryOperator<E> clamp) {
                  this.clamp = clamp;
                  return this;
            }

            public Builder<K, E> comment(String comment) {
                  this.comment = comment;
                  return this;
            }

            public MapConfigVariant<K, E> build(String name) {
                  return new MapConfigVariant<>(name, defau, comment, keyEncode, keyDecode, entryEncode, entryDecode, validator, clamp, example);
            }

      }
}
