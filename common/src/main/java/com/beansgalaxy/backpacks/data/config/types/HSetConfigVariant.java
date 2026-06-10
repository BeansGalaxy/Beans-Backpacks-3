package com.beansgalaxy.backpacks.data.config.types;

import com.beansgalaxy.backpacks.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.GsonHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class HSetConfigVariant<ENTRY> extends ConfigVariant<HashSet<ENTRY>> {
      private final Codec<ENTRY> codec;
      private final Predicate<String> isValid;
      private final HashSet<JsonElement> rejects;
      
      private HSetConfigVariant(String name, HashSet<ENTRY> defau, HashSet<JsonElement> rejects, Predicate<String> isValid, Codec<ENTRY> codec, String comment) {
            super(name, defau, comment);
            this.value = new HashSet<>(defau);
            this.codec = codec;
            this.isValid = isValid;
            this.rejects = rejects;
      }

      @Override
      public String encode() {
            JsonArray array = new JsonArray();
            
            for (ENTRY entry : value) {
                  DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, entry);
                  if (result.isError()) {
                        result.ifError(error -> {
                              Constants.LOG.warn(error.toString());
                        });
                        continue;
                  }
                  
                  array.add(result.getOrThrow());
            }
            
            for (JsonElement entry : rejects) {
                  array.add(entry);
            }

            return '"' + name + "\": " + array;
      }

      @Override
      public void decode(JsonObject jsonObject) {
            if (!jsonObject.has(name))
                  return;
            
            JsonElement element = jsonObject.get(name);
            if (!element.isJsonArray()) {
                  Constants.LOG.error("error while decoding \"" + name + "\"; Not a JSON Array:" + element);
                  return;
            }
            
            value.clear();
            rejects.clear();
            
            JsonArray jsonArray = element.getAsJsonArray();
            for (JsonElement jsonElement : jsonArray) {
                  DataResult<ENTRY> parse = codec.parse(JsonOps.INSTANCE, jsonElement);
                  if (parse.isError()) {
                        rejects.add(jsonElement);
                        continue;
                  }
                  
                  ENTRY entry = parse.getOrThrow();
                  value.add(entry);
            }
      }

      private static <E> void decode(String encoded, Predicate<String> isValid, Function<String, E> decode, HashSet<E> value, HashSet<String> rejects) {
            String[] split = encoded.replace(" ", "").split(",");
            for (String entry : split) {
                  if (Constants.isEmpty(entry))
                        continue;
                  if (isValid.test(entry)) {
                        E apply = decode.apply(entry);
                        value.add(apply);
                  }
                  else rejects.add(entry);
            }
      }

      public static class Builder<E> {
            private final Codec<E> codec;
            private final HashSet<E> defau = new HashSet<>();
            private String defauString = "[]";
            private Predicate<String> isValid = in -> true;
            private String comment = "";

            private Builder(Codec<E> codec) {
                  this.codec = codec;
            }
            
            public static <E> Builder<E> create(Codec<E> codec) {
                  return new Builder<>(codec);
            }

            public Builder<E> comment(String comment) {
                  this.comment = comment;
                  return this;
            }

            public Builder<E> defau(E... defau) {
                  this.defau.addAll(Arrays.asList(defau));
                  return this;
            }

            public Builder<E> defauString(String defau) {
                  this.defauString = defau;
                  return this;
            }

            public Builder<E> defauString(Supplier<String> defau) {
                  this.defauString = defau.get();
                  return this;
            }

            /**
             * Used while indexing through the list written in the json file. Passes each entry of the list through before converting
             * it to ENTRY to check if it is valid to be converted.
             * @param validate If false, the list's entry will; be saved back to config, not be converted, and not be used.
             */
            public Builder<E> isValid(Predicate<String> validate) {
                  this.isValid = validate;
                  return this;
            }

            public HSetConfigVariant<E> build(String name) {
                  HashSet<JsonElement> rejects = new HashSet<>();
                  
                  for (JsonElement jsonElement : GsonHelper.parseArray(defauString)) {
                        DataResult<E> result = codec.parse(JsonOps.COMPRESSED, jsonElement);
                        result.ifSuccess(defau::add).ifError(error -> {
                              Constants.LOG.warn(error.message());
                              rejects.add(jsonElement);
                        });
                  }
                  
                  return new HSetConfigVariant<>(name, defau, rejects, isValid, codec, comment);
            }
      }
}
