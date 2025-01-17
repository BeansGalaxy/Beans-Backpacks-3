package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.events.InteractBlockCallback;
import com.beansgalaxy.backpacks.events.NetworkPackages;
import com.beansgalaxy.backpacks.events.ServerStartEvent;
import com.beansgalaxy.backpacks.events.SyncDataEvent;
import com.beansgalaxy.backpacks.trait.battery.BatteryTraits;
import com.beansgalaxy.backpacks.trait.bucket.BucketTraits;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModItems;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import team.reborn.energy.api.EnergyStorage;

import java.util.Optional;

public class FabricMain implements ModInitializer {

    public static final DataComponentType<FluidVariant>
                DATA_FLUID = Traits.register("data_fluid", FluidVariant.CODEC, FluidVariant.PACKET_CODEC);

    @Override
    public void onInitialize() {
        CommonClass.init();
        NetworkPackages.registerCommon();
        EntityDataSerializers.registerSerializer(BackpackEntity.PLACEABLE.serializer());
        InteractBlockCallback.EVENT.register(new InteractBlockCallback());
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerSave.CONFIG.read();
        });

        EnergyStorage.ITEM.registerFallback((stack, ctx) -> {
            BatteryTraits batteryTraits = (BatteryTraits) stack.get(Traits.BATTERY);
            if (batteryTraits != null)
                return batteryTraits.mutable(PatchedComponentHolder.of(stack)).getStorage();

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null) {
                Optional<GenericTraits> trait = reference.getTrait();
                if (trait.isPresent() && trait.get() instanceof BatteryTraits battery)
                    return battery.mutable(PatchedComponentHolder.of(stack)).getStorage();
            }

            Optional<EnderTraits> optional = EnderTraits.get(stack);
            if (optional.isEmpty())
                return null;

            EnderTraits enderTraits = optional.get();
            Optional<GenericTraits> optionalTrait = enderTraits.getTrait();
            if (optionalTrait.isPresent() && optionalTrait.get() instanceof BatteryTraits battery)
                return battery.mutable(enderTraits).getStorage();

            return null;
        });

        FluidStorage.ITEM.registerFallback((stack, ctx) -> {
            BucketTraits bucketTraits = (BucketTraits) stack.get(Traits.BUCKET);
            if (bucketTraits != null)
                return bucketTraits.mutable(PatchedComponentHolder.of(stack));

            ReferenceTrait reference = stack.get(Traits.REFERENCE);
            if (reference != null) {
                Optional<GenericTraits> trait = reference.getTrait();
                if (trait.isPresent() && trait.get() instanceof BucketTraits bucket)
                    return bucket.mutable(PatchedComponentHolder.of(stack));
            }

            Optional<EnderTraits> optional = EnderTraits.get(stack);
            if (optional.isEmpty())
                return null;

            EnderTraits enderTraits = optional.get();
            Optional<GenericTraits> optionalTrait = enderTraits.getTrait();
            if (optionalTrait.isPresent() && optionalTrait.get() instanceof BucketTraits bucket)
                return bucket.mutable(enderTraits);

            return null;
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(new SyncDataEvent());
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerStartEvent());
    }

    public static final CreativeModeTab BACKPACK_TAB = ModItems.CREATIVE_TAB.apply(FabricItemGroup.builder()).build();

    public static final CreativeModeTab CREATIVE_TAB =
                Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                            ResourceLocation.parse(Constants.MOD_ID + ":backpacks"), BACKPACK_TAB);


}
