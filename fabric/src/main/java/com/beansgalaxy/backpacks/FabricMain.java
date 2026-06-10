package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.events.NetworkPackages;
import com.beansgalaxy.backpacks.events.ServerStartEvent;
import com.beansgalaxy.backpacks.events.SyncDataEvent;
import com.beansgalaxy.backpacks.items.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;

public class FabricMain implements ModInitializer {

    @Override
    public void onInitialize() {
        CommonClass.init();
        ModRegistry.register();
        NetworkPackages.registerCommon();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerSave.CONFIG.read();
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(new SyncDataEvent());
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerStartEvent());
    }

    public static final CreativeModeTab BACKPACK_TAB = ModItems.CREATIVE_TAB.apply(FabricItemGroup.builder()).build();

    public static final CreativeModeTab CREATIVE_TAB =
                Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                            Constants.defaultLocation("backpacks"), BACKPACK_TAB);


}
