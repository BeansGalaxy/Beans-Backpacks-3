package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.abstract_traits.IProjectileTrait;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(BowItem.class)
public abstract class BowMixin extends ProjectileWeaponItem {
      public BowMixin(Properties pProperties) {
            super(pProperties);
      }

      @Shadow public abstract Predicate<ItemStack> getAllSupportedProjectiles();

      @Shadow public abstract int getUseDuration(ItemStack pStack, LivingEntity pEntity);

      @Inject(method = "releaseUsing", cancellable = true, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Player;getProjectile(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
      private void useBackpackQuiverArrow(ItemStack bowStack, Level level, LivingEntity pEntityLiving, int pTimeLeft, CallbackInfo ci) {
            Player player = (Player) pEntityLiving;
            Predicate<ItemStack> predicate = getAllSupportedProjectiles();
            IProjectileTrait.runIfEquipped(player, (proTrait, slot, quiver, holder) -> {
                  MutableBundleLike<?> mutable = proTrait.mutable(holder);
                  List<ItemStack> stacks = mutable.getItemStacks();
                  if (stacks.isEmpty())
                        return false;

                  int slotSafe = mutable.getSelectedSlotSafe(player);
                  ItemStack stack = stacks.get(slotSafe);
                  if (predicate.test(stack)) {
                        int i = this.getUseDuration(bowStack, player) - pTimeLeft;
                        float f = BowItem.getPowerForTime(i);
                        if (!(f + 0.0 < 0.1)) {
                              List<ItemStack> list = draw(bowStack, stack, player);
                              if (level instanceof ServerLevel serverLevel) {
                                    if (!list.isEmpty()) {
                                          this.shoot(serverLevel, player, player.getUsedItemHand(), bowStack, list, f * 3.0F, 1.0F, f == 1.0F, null);
                                    }
                              }

                              level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + f * 0.5F);
                              player.awardStat(Stats.ITEM_USED.get(this));
                              mutable.push();

                              int selectedSlotSafe = mutable.getSelectedSlotSafe(player);
                              List<ItemStack> finalStacks = holder.get(ITraitData.ITEM_STACKS);
                              int size = finalStacks == null ? 0 : finalStacks.size();
                              mutable.limitSelectedSlot(selectedSlotSafe, size);
                              ci.cancel();

                              if (holder instanceof EnderTraits enderTraits)
                                    enderTraits.broadcastChanges();
                              else if (player instanceof ServerPlayer serverPlayer) {
                                    List<Pair<EquipmentSlot, ItemStack>> pSlots = List.of(Pair.of(slot, quiver));
                                    ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(serverPlayer.getId(), pSlots);
                                    serverPlayer.serverLevel().getChunkSource().broadcastAndSend(serverPlayer, packet);
                              }
                        }

                        return true;
                  }

                  return false;
            });
      }
}
