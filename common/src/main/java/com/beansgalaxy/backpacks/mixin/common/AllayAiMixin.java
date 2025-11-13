package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.allay.AllayAi;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(AllayAi.class)
public class AllayAiMixin {

      @Inject(method = "makeBrain", at = @At("HEAD"))
      private static void backpackMakeBrain(Brain<Allay> pBrain, CallbackInfoReturnable<Brain<?>> cir) {
            OneShot<LivingEntity> itemPickup = BehaviorBuilder.create((brain) -> brain.group(
                  brain.registered(MemoryModuleType.LOOK_TARGET),
                  brain.registered(MemoryModuleType.WALK_TARGET),
                  brain.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                  brain.registered(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS),
                  brain.present(CommonClass.BACKPACK_OWNER_MEMORY.get())
            ).apply(brain, (look_target, walk_target, item, cooldown, owner) -> (level, allay, seed) -> {
                  ItemEntity itementity = brain.get(item);
                  if (!level.getWorldBorder().isWithinBounds(itementity.getBoundingBox()))
                        return false;
                  
                  if (brain.tryGet(cooldown).isPresent())
                        return false;
                  
                  if (!itementity.closerThan(allay, 16, 32))
                        return false;
                  
                  UUID uuid = brain.get(owner);
                  Player player = level.getPlayerByUUID(uuid);
                  if (player != null) {
                        Vector3f step;
                        float yaw = player.getXRot();
                        if (yaw < -60) // UP
                              step = new Vector3f(0);
                        else if (yaw > 60) // DOWN
                              step = new Vector3f(0, -4, 0);
                        else // HORIZONTAL
                              step = player.getDirection().step().mul(2);
                        
                        Vec3 newPos = player.position().add(step.x, step.y, step.z);
                        if (itementity.position().closerThan(newPos, 6))
                              return false;
                  }
                  
                  EntityTracker target = new EntityTracker(itementity, true);
                  WalkTarget walktarget = new WalkTarget(target, 2F, 0);
                  look_target.set(target);
                  walk_target.set(walktarget);
                  return true;
            }));
            
            BehaviorControl<LivingEntity> catchupToPlayer = StayCloseToTarget.create(
                        entity -> {
                              if (entity.level() instanceof ServerLevel serverLevel) {
                                    Player player = getPlayer(serverLevel, entity.getBrain());
                                    if (player != null) {
                                          return Optional.of(new EntityTracker(player, true));
                                    }
                              }
                              return Optional.empty();
                        },
                        entity -> !entity.getItemBySlot(EquipmentSlot.BODY).isEmpty() && !entity.getBrain().hasMemoryValue(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM),
                        8, 5, 1.65f);

            OneShot<Allay> wanderNearTarget = beans_Backpacks_3$createWanderNearTarget();
            pBrain.addActivityWithConditions(
                  CommonClass.CHESTER_ACTIVITY.get(),
                  ImmutableList.of(
                        Pair.of(0, itemPickup),
                        Pair.of(1, catchupToPlayer),
                        Pair.of(2, new RunOne<>(ImmutableList.of(
                              Pair.of(wanderNearTarget, 1),
                              Pair.of(SetEntityLookTarget.create(entity -> {
                                    EntityType<?> type = entity.getType();
                                    return type == EntityType.PLAYER || type == EntityType.ALLAY;
                              }, 8.0F), 2),
                              Pair.of(SetEntityLookTarget.create(entity -> {
                                    EntityType<?> type = entity.getType();
                                    return type == EntityType.GLOW_ITEM_FRAME || type == EntityType.ITEM_FRAME || type == CommonClass.BACKPACK_ENTITY.get();
                              }, 4.0F), 2),
                              Pair.of(new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F), 1),
                              Pair.of(new DoNothing(20, 60), 1)
                        )))
                  ),
                  ImmutableSet.of(Pair.of(CommonClass.BACKPACK_OWNER_MEMORY.get(), MemoryStatus.VALUE_PRESENT))
            );
      }

      @Inject(method = "updateActivity", cancellable = true, at = @At("HEAD"))
      private static void backpacks_updateActivity(Allay pAllay, CallbackInfo ci) {
            Brain<Allay> brain = pAllay.getBrain();
            if (brain.checkMemory(CommonClass.BACKPACK_OWNER_MEMORY.get(), MemoryStatus.VALUE_PRESENT)) {
                  brain.setActiveActivityIfPossible(CommonClass.CHESTER_ACTIVITY.get());
                  ci.cancel();
            }
      }

      @Unique
      private static OneShot<Allay> beans_Backpacks_3$createWanderNearTarget() {
            return BehaviorBuilder.create(in ->
                        in.group(
                                    in.registered(MemoryModuleType.LOOK_TARGET),
                                    in.registered(MemoryModuleType.WALK_TARGET)
                        ).apply(in, (look, walk) -> (serverLevel, allay, l) -> {
                              Brain<Allay> brain = allay.getBrain();
                              Optional<WalkTarget> memory = brain.getMemory(MemoryModuleType.WALK_TARGET);
                              if (memory.isPresent())
                                    return false;

                              Optional<PositionTracker> lookOptional = brain.getMemory(MemoryModuleType.LOOK_TARGET);
                              if (lookOptional.isPresent())
                                    return false;

                              Player player = getPlayer(serverLevel, brain);
                              if (player == null || !Objects.equals(player.level(), allay.level()))
                                    return false;

                              RandomSource random = allay.getRandom();
                              EntityTracker target = new EntityTracker(player, true);
                              BlockPos pos = target.currentBlockPosition();

                              int i = pos.getX();
                              int k = pos.getY();
                              int j = pos.getZ();
                              i += random.nextInt(8) - 4;
                              k += random.nextBoolean() ? 2 : 1;
                              j += random.nextInt(8) - 4;

                              BlockPos walkPos = new BlockPos(i, k, j);
                              if (!serverLevel.isEmptyBlock(walkPos)) {
                                    for (walkPos = walkPos.above(); serverLevel.isEmptyBlock(walkPos); walkPos = walkPos.above()) {
                                          if (walkPos.getY() > pos.getY() + 6)
                                                return false;
                                    }
                              }

                              Vec3 center = walkPos.getCenter();
                              Vec3 targetPos = target.currentPosition();
                              if (center.closerThan(targetPos, 3))
                                    return false;

                              double pDistance = 4;
                              double distanceToSqr = center.distanceToSqr(targetPos.x(), targetPos.y(), targetPos.z());
                              if (distanceToSqr > pDistance * pDistance)
                                    return false;
//
//                              if (player instanceof ServerPlayer serverPlayer1) {
//                                    serverPlayer1.connection.send(new ClientboundLevelParticlesPacket(
//                                                ParticleTypes.CLOUD,
//                                                true,
//                                                walkPos.getX(), walkPos.getY(), walkPos.getZ(),
//                                                0.1f, 0.1f, 0.1f,
//                                                0.01f, 10
//                                    ));
//                              }

                              look.set(target);
                              walk.set(new WalkTarget(walkPos, 1, 0));
                              return true;
                        })
            );
      }

      @Unique @Nullable
      private static Player getPlayer(ServerLevel serverLevel, Brain<? extends LivingEntity> brain) {
            Optional<UUID> optional = brain.getMemory(CommonClass.BACKPACK_OWNER_MEMORY.get());
            if (optional.isEmpty())
                  return null;

            UUID uuid = optional.get();
            return serverLevel.getPlayerByUUID(uuid);
      }

      @Unique
      private static BehaviorControl<PathfinderMob> fly(float pSpeedModifier) {
            return strollFlyOrSwim(pSpeedModifier, mob -> {
                  Vec3 vec3 = mob.getViewVector(0.0F);
                  return AirAndWaterRandomPos.getPos(mob, 5, 1, 2, vec3.x, vec3.z, 1.5707963705062866);
            });
      }

      @Unique
      private static OneShot<PathfinderMob> strollFlyOrSwim(float pSpeedModifier, Function<PathfinderMob, Vec3> pTarget) {
            return BehaviorBuilder.create((p_258620_) -> {
                  return p_258620_.group(p_258620_.absent(MemoryModuleType.WALK_TARGET)).apply(p_258620_, (p_258600_) -> {
                        return (p_258610_, p_258611_, p_258612_) -> {
                              Optional<Vec3> optional = Optional.ofNullable(pTarget.apply(p_258611_));
                              p_258600_.setOrErase(optional.map((p_258622_) -> {
                                    return new WalkTarget(p_258622_, pSpeedModifier, 0);
                              }));
                              return true;
                        };
                  });
            });
      }

}
