package com.beansgalaxy.backpacks.data.config.screen;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.types.ConfigLine;
import com.beansgalaxy.backpacks.platform.Services;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public interface IConfig {

      String getPath();

      Collection<ConfigLine> getLines();

      default void parse(String encodedConfig) {
            // Parse the JSON content into a Person object
            JsonObject jsonObject = JsonParser.parseString(encodedConfig).getAsJsonObject();
            Collection<ConfigLine> lines = getLines();
            for (ConfigLine line : lines) {
                  line.decode(jsonObject);
            }
      }
      default void read() {
            read(true);
      }

      default void read(boolean andWrite) {
            try {
                  // Read the .json5 content from the file
                  Path path = Services.PLATFORM.getConfigPath();
                  
                  Path resolve = path.resolve(getPath() + ".json5");
                  if (!Files.exists(resolve))
                        resolve = getLegacyLocation();
                  

                  String json5Content = new String(Files.readAllBytes(resolve));

                  // Remove comments from the .json5 content
                  String jsonContent = json5Content.replaceAll("/\\*.*?\\*/", "").replaceAll("//.*", "");
                  parse(jsonContent);

            } catch (Throwable e) {
                  Constants.LOG.warn("No Config for " + Constants.MOD_ID + " : Created new config");
            }

            if (andWrite)
                  this.write();
      }

      @Deprecated(since="0.4-beta")
      private Path getLegacyLocation() {
            return Services.PLATFORM.getConfigDir().resolve(Constants.MOD_ID + '-' + getPath() + ".json5");
      }

      default void write() {
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");

            Iterator<ConfigLine> iterator = getLines().iterator();
            while (iterator.hasNext()) {
                  ConfigLine line = iterator.next();
                  String encode = line.encode();
                  if (line.punctuate())
                        sb.append("  ");
                  sb.append(encode);
                  int length = encode.length();
                  if (line.punctuate() && iterator.hasNext()) {
                        sb.append(',');
                        length += 1;
                  }

                  sb.append(line.comment(Math.max(0, 34 - length)));
                  sb.append('\n');
            }
            sb.append('}');

            try {
                  Path path = Services.PLATFORM.getConfigPath();
                  Path resolve = path.resolve(getPath() + ".json5");

                  if (!Files.exists(path))
                        Files.createDirectory(path);

                  String string = sb.toString();
                  Files.writeString(resolve, string);
            } catch (IOException e) {
                  e.printStackTrace();
            }
      }

      /* ============================= Item Whitelist Helpers ============================ */

}
