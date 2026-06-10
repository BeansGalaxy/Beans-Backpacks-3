package com.beansgalaxy.backpacks.network.clientbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.data.config.CommonConfig;
import com.beansgalaxy.backpacks.data.config.types.ConfigLine;
import com.beansgalaxy.backpacks.network.Network2C;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Iterator;

public class ConfigureConfig implements Packet2C {
      final String encodedConfig;

      private ConfigureConfig(String encodedConfig) {
            this.encodedConfig = encodedConfig;
      }

      public ConfigureConfig(FriendlyByteBuf buf) {
            this(buf.readUtf());
      }

      public static void send(ServerPlayer player) {
            CommonConfig commonConfig = new CommonConfig();
            commonConfig.read(false);
            Iterator<ConfigLine> iterator = commonConfig.getLines().iterator();
            StringBuilder sb = new StringBuilder().append('{');
            while (iterator.hasNext()) {
                  ConfigLine line = iterator.next();
                  if (!line.punctuate()) continue;

                  String encode = line.encode();
                  sb.append(encode);

                  if (iterator.hasNext())
                        sb.append(',');
            }
            sb.append('}');
            String msg = sb.toString();

            new ConfigureConfig(msg).send2C(player);
      }

      @Override
      public Network2C getNetwork() {
            return Network2C.CONFIG_COMMON_2C;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeUtf(encodedConfig);
      }

      @Override
      public void handle() {
            String jsonContent = encodedConfig.replaceAll("/\\*.*?\\*/", "").replaceAll("//.*", "");
            ServerSave.CONFIG.parse(jsonContent);
      }

      public static final Type<ConfigureConfig> ID = new Type<>(Constants.defaultLocation("config_common_c"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
