package com.beansgalaxy.backpacks.data;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.data.config.CommonConfig;
import com.beansgalaxy.backpacks.util.data_fixers.LegacyEnder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ServerSave extends SavedData {
      public static final CommonConfig CONFIG = new CommonConfig();
      public final EnderStorage enderStorage = new EnderStorage();

      @Override
      public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
            enderStorage.save(tag);
            return tag;
      }

      private static ServerSave load(CompoundTag tag, HolderLookup.Provider provider) {
            ServerSave save = new ServerSave();
            recoverLegacyEnderItems(tag, save);
            save.enderStorage.load(tag);


            return save;
      }

      private static void recoverLegacyEnderItems(CompoundTag tag, ServerSave save) {
            LegacyEnder legacyEnder = new LegacyEnder();
            legacyEnder.fromNbt(tag);
            legacyEnder.MAP.forEach(save.enderStorage::setLegacyEnder);
            tag.remove("EnderData");
            tag.remove("Config");
            tag.remove("LockedAdvancement");
      }

      public static ServerSave getSave(MinecraftServer server, boolean updateSave) {
            ServerLevel level = server.getLevel(Level.OVERWORLD);
            DimensionDataStorage dataStorage = level.getDataStorage();
            Factory<ServerSave> factory = new Factory<>(ServerSave::new, ServerSave::load, DataFixTypes.LEVEL);
            ServerSave save = dataStorage.computeIfAbsent(factory, Constants.MOD_ID);

            if (updateSave)
                  save.setDirty();

            return save;
      }
}
