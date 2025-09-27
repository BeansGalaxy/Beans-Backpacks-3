package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.data.config.ClientConfig;
import com.beansgalaxy.backpacks.items.ModBlocks;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class CommonClass {
      public static final Supplier<EntityType<BackpackEntity>> BACKPACK_ENTITY =
                Services.PLATFORM.register("backpack",
                            EntityType.Builder.of(BackpackEntity::new, MobCategory.MISC)
                                        .sized(7/16f, 9/16f)
                                        .eyeHeight(0.5f)
                );
      public static final Supplier<EntityType<BackpackEntity>> LEGACY_ENDER_ENTITY =
                Services.PLATFORM.register("ender_backpack",
                            EntityType.Builder.of(BackpackEntity::new, MobCategory.MISC)
                                        .sized(7/16f, 9/16f)
                                        .eyeHeight(0.5f)
                );
      public static final Supplier<EntityType<BackpackEntity>> LEGACY_WINGED_ENTITY =
                Services.PLATFORM.register("winged_backpack",
                            EntityType.Builder.of(BackpackEntity::new, MobCategory.MISC)
                                        .sized(7/16f, 9/16f)
                                        .eyeHeight(0.5f)
                );

      public static final Supplier<Activity> CHESTER_ACTIVITY =
                Services.PLATFORM.registerActivity("chester");
      public static final Supplier<Activity> CHESTER_IDLE_ACTIVITY =
                Services.PLATFORM.registerActivity("chester_idle");
      public static final Supplier<MemoryModuleType<UUID>> BACKPACK_OWNER_MEMORY =
                  Services.PLATFORM.registerMemoryModule("backpack_owner", UUIDUtil.CODEC);

      public static final ClientConfig CLIENT_CONFIG = new ClientConfig();

      public static void init() {
            ModSound.Events.register();
            Traits.register();
            ITraitData.register();
            ModItems.register();
            ModBlocks.register();
    }

      public static InteractionResult swapBackWith(ArmorStand armorStand, Player player) {
            EquipmentSlot slot = EquipmentSlot.BODY;
            ItemStack backpack = player.getItemBySlot(slot);
            ItemStack standItem = armorStand.getItemBySlot(slot);
            if (backpack.isEmpty() && standItem.isEmpty())
                  return InteractionResult.FAIL;

            armorStand.onEquipItem(slot, standItem, backpack);
            player.onEquipItem(slot, backpack, standItem);
            armorStand.setItemSlot(slot, backpack);
            player.setItemSlot(slot, standItem);
            return InteractionResult.SUCCESS;
      }

      public static InteractionResult swapBackWith(Allay allay, Player player) {
            EquipmentSlot slot = EquipmentSlot.BODY;
            ItemStack backpack = player.getItemBySlot(slot);
            ItemStack standItem = allay.getItemBySlot(slot);

            if (backpack.isEmpty()) {
                  if (standItem.isEmpty())
                        return InteractionResult.FAIL;
                  else
                        allay.getBrain().eraseMemory(BACKPACK_OWNER_MEMORY.get());
            }
            else {
                  Optional<GenericTraits> traitsOptional = Traits.get(backpack);
                  if (traitsOptional.isEmpty())
                        return InteractionResult.FAIL;

                  ItemStack itemInHand = allay.getItemInHand(InteractionHand.MAIN_HAND);
                  if (!itemInHand.isEmpty() && !player.addItem(itemInHand))
                        return InteractionResult.FAIL;

                  Brain<Allay> brain = allay.getBrain();
                  brain.setMemory(BACKPACK_OWNER_MEMORY.get(), player.getUUID());
                  brain.setActiveActivityIfPossible(CommonClass.CHESTER_ACTIVITY.get());
            }

            allay.onEquipItem(slot, standItem, backpack);
            player.onEquipItem(slot, backpack, standItem);
            allay.setItemSlot(slot, backpack);
            player.setItemSlot(slot, standItem);
            return InteractionResult.SUCCESS;
      }

      public static void interactEquippedBackpack(LivingEntity owner, Player viewer, CallbackInfoReturnable<InteractionResult> cir) {
            ItemStack backpack = owner.getItemBySlot(EquipmentSlot.BODY);
            if (backpack.isEmpty())
                  return;

            // CHECKS ROTATION OF BOTH PLAYERS
            double yaw = Math.abs(viewer.yHeadRot - owner.yBodyRot) % 360 - 180;
            boolean yawMatches = Math.abs(yaw) > 90;
            if (!yawMatches)
                  return;

            Optional<GenericTraits> optional = Traits.get(backpack);
            if (optional.isEmpty())
                  return;

            // OFFSETS OTHER PLAYER'S POSITION
            double angleRadians = Math.toRadians(owner.yBodyRot);
            double offset = -0.3;
            double x = owner.getX();
            double z = owner.getZ();
            double offsetX = Math.cos(angleRadians) * offset;
            double offsetZ = Math.sin(angleRadians) * offset;
            double newX = x - offsetZ;
            double newY = owner.getEyeY() - .45;
            double newZ = z + offsetX;

            // CHECKS IF PLAYER IS LOOKING
            Vec3 vec3d = viewer.getViewVector(1.0f).normalize();
            Vec3 vec3d2 = new Vec3(newX - viewer.getX(), newY - viewer.getEyeY(), newZ - viewer.getZ());
            double d = -vec3d2.length() + 5.65;
            double e = vec3d.dot(vec3d2.normalize());
            double maxRadius = 0.05;
            double radius = (d * d * d * d) / 625;
            boolean looking = e > 1.0 - radius * maxRadius && viewer.hasLineOfSight(owner);
            if (!looking)
                  return;

            GenericTraits traits = optional.get();
            traits.onPlayerInteract(owner, viewer, backpack, cir);
      }

      public static @NotNull String getTinyNumberFromDigitChar(char c) {
            return switch (c) {
                  case '1' -> "₁";
                  case '2' -> "₂";
                  case '3' -> "₃";
                  case '4' -> "₄";
                  case '5' -> "₅";
                  case '6' -> "₆";
                  case '7' -> "₇";
                  case '8' -> "₈";
                  case '9' -> "₉";
                  default -> "₀";
            };
      }
}