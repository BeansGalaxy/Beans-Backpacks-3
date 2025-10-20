package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.data.TraitLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class DataResourcesMixin {

      @Inject(method = "loadResources", at = @At("HEAD"))
      private static void catchDataPacks(ResourceManager manager,
                                         LayeredRegistryAccess<RegistryLayer> access,
                                         FeatureFlagSet flagSet,
                                         Commands.CommandSelection commands,
                                         int $$4, Executor $$5, Executor $$6,
                                         CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir)
      {
            new TraitLoader(manager, access).run();
      }
}
