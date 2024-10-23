package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Predicate;

@Mixin(CrossbowItem.class)
public abstract class CrossbowMixin extends ProjectileWeaponItem {
      public CrossbowMixin(Properties pProperties) {
            super(pProperties);
      }

      @Shadow public abstract Predicate<ItemStack> getAllSupportedProjectiles();

      @Inject(method = "tryLoadProjectiles", cancellable = true, at = @At("HEAD"))
      private static void tryLoadQuiverProjectiles(LivingEntity pShooter, ItemStack pCrossbowStack, CallbackInfoReturnable<Boolean> cir) {
            if (pShooter instanceof Player player) {
                  ProjectileWeaponItem projectileWeaponItem = (ProjectileWeaponItem) pCrossbowStack.getItem();
                  Predicate<ItemStack> predicate = projectileWeaponItem.getAllSupportedProjectiles();
                  QuiverTraits.runIfQuiverEquipped(player, (traits, slot) -> {
                        QuiverTraits.Mutable mutable = traits.mutable();
                        List<ItemStack> stacks = mutable.getItemStacks();
                        if (stacks.isEmpty())
                              return false;

                        int selectedSlotSafe = traits.getSelectedSlotSafe(player);
                        ItemStack stack = stacks.get(selectedSlotSafe);
                        if (!predicate.test(stack))
                              return false;

                        List<ItemStack> list = draw(pCrossbowStack, stack, player);
                        if (list.isEmpty())
                              return false;

                        pCrossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));

                        ItemStack quiver = player.getItemBySlot(slot);
                        PatchedComponentHolder holder = PatchedComponentHolder.of(quiver);
                        traits.kind().freezeAndCancel(holder, mutable);
                        cir.setReturnValue(true);

                        if (player instanceof ServerPlayer serverPlayer) {
                              List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(slot, quiver));
                              ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                              serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                        }

                        return true;
                  });
            }
      }
}
