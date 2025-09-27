package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity implements ViewableAccessor {

      @Unique public final ArmorStand instance = (ArmorStand) (Object) this;

      protected ArmorStandMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);

      @Shadow protected abstract EquipmentSlot getClickedSlot(Vec3 pVector);

      @Shadow protected abstract boolean swapItem(Player pPlayer, EquipmentSlot pSlot, ItemStack pStack, InteractionHand pHand);

      @Shadow public abstract void tick();

      @Shadow public abstract boolean isMarker();

      @Inject(method = "interactAt", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At("HEAD"))
      private void backpackInteractAt(Player pPlayer, Vec3 pVec, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
            if (isMarker())
                  return;

            if (!pPlayer.isSpectator() && BackData.get(pPlayer).isActionKeyDown()) {
                  InteractionResult result = CommonClass.swapBackWith(instance, pPlayer);
                  if (result.consumesAction())
                        cir.setReturnValue(result);
            }
            else if (!getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
                  interactWithBackpack(instance, pPlayer, pVec, cir);
            }
      }

      @Inject(method = "interactAt", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/decoration/ArmorStand;getEquipmentSlotForItem(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/EquipmentSlot;"))
      private void backpackInteractAt(Player pPlayer, Vec3 pVec, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir, ItemStack itemstack) {
            Optional<EquipableComponent> optional = EquipableComponent.get(itemstack);
            if (optional.isEmpty())
                  return;

            EquipableComponent equipable = optional.get();

            EquipmentSlot equipmentSlot = null;
            for (EquipmentSlot slot : equipable.values()) {
                  if (EquipmentSlot.BODY.equals(slot))
                        continue;

                  equipmentSlot = slot;
                  ItemStack armor = getItemBySlot(slot);
                  if (armor.isEmpty())
                        break;
            }

            if (equipmentSlot == null) {
                  EquipmentSlot clickedSlot = getClickedSlot(pVec);
                  if (equipable.slots().test(clickedSlot) && swapItem(pPlayer, clickedSlot, itemstack, pHand))
                        cir.setReturnValue(InteractionResult.SUCCESS);
                  return;
            }

            if (swapItem(pPlayer, equipmentSlot, itemstack, pHand))
                  cir.setReturnValue(InteractionResult.SUCCESS);
      }

      @Unique private void interactWithBackpack(LivingEntity owner, Player viewer, Vec3 pVec, CallbackInfoReturnable<InteractionResult> cir) {
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

      private ItemStack backItem = ItemStack.EMPTY;

      @Inject(method = "getItemBySlot", cancellable = true, at = @At("HEAD"))
      private void backpackGetItemBySlot(EquipmentSlot pSlot, CallbackInfoReturnable<ItemStack> cir) {
            if (EquipmentSlot.BODY.equals(pSlot))
                  cir.setReturnValue(backItem);
      }

      @Inject(method = "setItemSlot", cancellable = true, at = @At("HEAD"))
      private void backpackGetItemBySlot(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
            if (EquipmentSlot.BODY.equals(pSlot)) {
                  this.verifyEquippedItem(pStack);
                  onEquipItem(pSlot, backItem, pStack);
                  backItem = pStack;
            }
      }

      @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
      private void backpackAddSaveData(CompoundTag pCompound, CallbackInfo ci) {
            Tag backTag = backItem.saveOptional(registryAccess());
            pCompound.put("BackItem", backTag);
      }

      @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
      private void backpackReadSaveData(CompoundTag pCompound, CallbackInfo ci) {
            if (pCompound.contains("BackItem", 10))
                  backItem = ItemStack.parseOptional(registryAccess(), pCompound.getCompound("BackItem"));
      }

      @Inject(method = "brokenByAnything", at = @At("TAIL"))
      private void backpackBrokenByAnything(ServerLevel pLevel, DamageSource pDamageSource, CallbackInfo ci) {
            Block.popResource(this.level(), this.blockPosition().above(), backItem);
            backItem = ItemStack.EMPTY;
      }

// ===================================================================================================================== Viewable

      @Unique private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(ArmorStand.class, EntityDataSerializers.BOOLEAN);
      private final ViewableBackpack viewable = new ViewableBackpack() {
            @Override public void setOpen(boolean isOpen) {
                  instance.getEntityData().set(IS_OPEN, isOpen);
            }

            @Override public boolean isOpen() {
                  return instance.getEntityData().get(IS_OPEN);
            }

            @Override public void playSound(ModSound.Type type) {
                  Traits.get(toStack()).ifPresent(traits -> traits.sound().at(instance, type));
            }

            @Override public @NotNull Entity entity() {
                  return instance;
            }

            @Override protected ComponentHolder holder() {
                  return ComponentHolder.of(toStack());
            }

            @Override public ItemStack toStack() {
                  return instance.getItemBySlot(EquipmentSlot.BODY);
            }

            @Override public void onOpen(Player player) {
                  super.onOpen(player);
            }

            @Override public boolean shouldClose() {
                  if (instance.isRemoved())
                        return true;

                  ItemStack stack = viewable.toStack();
                  if (stack.isEmpty())
                        return true;

                  return Traits.get(stack).isEmpty();
            }

            @Override public float fallDistance() {
                  return ArmorStandMixin.this.fallDistance;
            }
      };

      @Override public ViewableBackpack beans_Backpacks_3$getViewable() {
            return viewable;
      }

      @Inject(method = "defineSynchedData", at = @At("TAIL"))
      private void backpackSyncedData(SynchedEntityData.Builder pBuilder, CallbackInfo ci) {
            pBuilder.define(IS_OPEN, false);
      }
}
