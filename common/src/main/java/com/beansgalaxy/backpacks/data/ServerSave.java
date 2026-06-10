package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.CommonConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ServerSave extends SavedData {
      public static final CommonConfig CONFIG = new CommonConfig();
      public final EnderStorage enderStorage;
      
      public ServerSave() {
            this(new EnderStorage());
      }
      
      public ServerSave(EnderStorage enderStorage) {
            this.enderStorage = enderStorage;
      }
      
      public static ServerSave getSave(MinecraftServer server, boolean updateSave) {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            DimensionDataStorage dataStorage = level.getDataStorage();
            SavedDataType<ServerSave> type = new SavedDataType<>(Constants.MOD_ID, ServerSave::new, ServerSave.CODEC, DataFixTypes.LEVEL);
            ServerSave save = dataStorage.computeIfAbsent(type);
            
            if (updateSave)
                  save.setDirty();

            return save;
      }
      
      public static final Codec<ServerSave> CODEC = RecordCodecBuilder.create(in -> in.group(
            EnderStorage.CODEC.fieldOf("ender_storage").forGetter(save -> save.enderStorage)
      ).apply(in, ServerSave::new));
}
