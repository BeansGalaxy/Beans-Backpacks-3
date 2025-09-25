package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.abstract_traits.IProjectileTrait;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
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
                  IProjectileTrait.runIfEquipped(player, (proTrait, slot, quiver, holder) -> {
                        MutableBundleLike<?> mutable = proTrait.mutable(holder);
                        List<ItemStack> stacks = mutable.getItemStacks();
                        if (stacks.isEmpty())
                              return false;

                        int selectedSlotSafe = mutable.getSelectedSlotSafe(player);
                        ItemStack stack = stacks.get(selectedSlotSafe);
                        if (!predicate.test(stack))
                              return false;

                        List<ItemStack> list = draw(pCrossbowStack, stack, player);
                        if (list.isEmpty())
                              return false;

                        pCrossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(list));

                        mutable.push();
                        cir.setReturnValue(true);
                        List<ItemStack> finalStacks = holder.get(ITraitData.ITEM_STACKS);
                        int size = finalStacks == null ? 0 : finalStacks.size();
                        mutable.limitSelectedSlot(selectedSlotSafe, size);

                        if (holder instanceof EnderTraits enderTraits)
                              enderTraits.broadcastChanges();
                        else if (player instanceof ServerPlayer serverPlayer) {
                              List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(slot, quiver));
                              ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                              serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                        }

                        return true;
                  });
            }
      }
}
