package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.PlayerAccessor;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements ViewableAccessor, PlayerAccessor {
      protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot1);

      @Shadow public abstract void setItemSlot(EquipmentSlot pSlot, ItemStack pStack);

      @Shadow public abstract Inventory getInventory();

      @Shadow @Final private Inventory inventory;
      @Unique public final Player instance = (Player) (Object) this;

      @Unique private static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
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

            @Override protected PatchedComponentHolder holder() {
                  return PatchedComponentHolder.of(toStack());
            }

            @Override public ItemStack toStack() {
                  return instance.getItemBySlot(EquipmentSlot.BODY);
            }

            Vec3 openedPos = null;
            float openedYaw = 0;

            @Override public void onOpen(Player player) {
                  openedPos = instance.position();
                  openedYaw = instance.yHeadRot;
                  super.onOpen(player);
            }

            @Override public boolean shouldClose() {
                  if (instance.isRemoved())
                        return true;

                  ItemStack stack = viewable.toStack();
                  if (stack.isEmpty())
                        return true;

                  if (Traits.get(stack).isEmpty())
                        return true;

                  if (openedPos == null)
                        return false;

                  if (instance.distanceToSqr(openedPos) > 0.5)
                        return true;

                  double yaw = Math.abs(instance.yHeadRot - openedYaw) % 360 - 180;
                  boolean yawMatches = Math.abs(yaw) > 90;
                  return !yawMatches;
            }

            @Override public float fallDistance() {
                  return PlayerMixin.this.fallDistance;
            }
      };

      @Override public ViewableBackpack beans_Backpacks_3$getViewable() {
            return viewable;
      }

      @Inject(method = "getItemBySlot", at = @At("HEAD"), cancellable = true)
      private void getBackSlotItem(EquipmentSlot equipmentSlot, CallbackInfoReturnable<ItemStack> cir) {
            if (equipmentSlot == EquipmentSlot.BODY) {
                  BackData access = (BackData) instance.getInventory();
                  cir.setReturnValue(access.beans_Backpacks_3$getBody().getFirst());
            }
      }

      @Inject(method = "setItemSlot", cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.AFTER,
                  target = "Lnet/minecraft/world/entity/player/Player;verifyEquippedItem(Lnet/minecraft/world/item/ItemStack;)V"))
      private void setBackSlotItem(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
            if (EquipmentSlot.BODY.equals(pSlot)) {
                  BackData access = (BackData) instance.getInventory();
                  instance.onEquipItem(EquipmentSlot.BODY, access.beans_Backpacks_3$getBody().set(0, pStack), pStack);
                  ci.cancel();
            }
      }

      @Inject(method = "getProjectile", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                  target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getHeldProjectile(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;"))
      private void getBackpackProjectile(ItemStack pShootable, CallbackInfoReturnable<ItemStack> cir, Predicate<ItemStack> predicate) {
            QuiverTraits.runIfQuiverEquipped(instance, (traits, slot, quiver, holder) -> {
                  List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty())
                        return false;

                  int selectedSlot = traits.getSelectedSlotSafe(holder, instance);
                  ItemStack stack = stacks.get(selectedSlot);
                  if (predicate.test(stack)) {
                        cir.setReturnValue(stack);
                        return true;
                  }

                  return false;
            });
      }

      @Inject(method = "createAttributes", at = @At(value = "RETURN"))
      private static void addShortAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
            AttributeSupplier.Builder returnValue = cir.getReturnValue();
            returnValue.add(CommonClass.SHORTHAND_ATTRIBUTE, Shorthand.SHORTHAND_DEFAU);
      }

       @Inject(method = "interactOn", cancellable = true, at = @At("HEAD"))
      private void backpackInteractOn(Entity pEntityToInteractOn, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
             if (pEntityToInteractOn instanceof Player player)
                   CommonClass.interactEquippedBackpack(player, instance, cir);
       }

      @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
      private void backpackAddSaveData(CompoundTag pCompound, CallbackInfo ci) {
            RegistryAccess access = instance.registryAccess();

            ItemStack backStack = getItemBySlot(EquipmentSlot.BODY);
            CompoundTag backpacks = new CompoundTag();

            RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
            DataResult<Tag> dataResult = ItemStack.OPTIONAL_CODEC.encodeStart(serializationContext, backStack);
            dataResult.ifSuccess(back -> backpacks.put("back", back));

            Shorthand shorthand = Shorthand.get(instance);
            shorthand.save(backpacks, access);

            Inventory inventory = getInventory();
            CompoundTag selectedSlots = new CompoundTag();
            saveSelectedSlots("items", inventory.items, instance, selectedSlots);
            saveSelectedSlots("armor", inventory.armor, instance, selectedSlots);
            ItemStack offhand = inventory.offhand.getFirst();
            saveSelectedSlots("offhand", offhand, instance, selectedSlots);
            ItemStack back = BackData.get(instance).beans_Backpacks_3$getBody().getFirst();
            saveSelectedSlots("back", back, instance, selectedSlots);
            backpacks.put("slot_selection", selectedSlots);

            pCompound.put(Constants.MOD_ID, backpacks);
      }

      @Unique
      private static void saveSelectedSlots(String name, List<ItemStack> items, Player instance, CompoundTag tag) {
            int size = items.size();
            CompoundTag slots = new CompoundTag();
            for (int i = 0; i < size; i++) {
                  ItemStack item = items.get(i);
                  saveSelectedSlots(String.valueOf(i), item, instance, slots);
            }
            
            tag.put(name, slots);
      }

      @Unique
      private static void saveSelectedSlots(String name, ItemStack item, Player instance, CompoundTag tag) {
            SlotSelection slotSelection = item.get(ITraitData.SLOT_SELECTION);
            if (slotSelection == null)
                  return;

            int selectedSlot = slotSelection.getSelectedSlot(instance);
            if (selectedSlot == 0)
                  return;

            tag.putInt(name, selectedSlot);
      }

      private static int getMaxSelection(ItemStack item) {
            List<ItemStack> stacks = item.get(ITraitData.ITEM_STACKS);
            if (stacks != null)
                  return stacks.size();

            return 0;
      }


      @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
      private void backpackReadSaveData(CompoundTag pCompound, CallbackInfo ci) {
            CompoundTag backpacks = pCompound.getCompound(Constants.MOD_ID);
            RegistryAccess access = instance.registryAccess();

            if (backpacks.contains("back")) {
                  Tag backSlot = backpacks.get("back");
                  RegistryOps<Tag> serializationContext = access.createSerializationContext(NbtOps.INSTANCE);
                  DataResult<ItemStack> back = ItemStack.OPTIONAL_CODEC.parse(serializationContext, backSlot);
                  back.ifSuccess(stack -> setItemSlot(EquipmentSlot.BODY, stack));
            }

            Shorthand shorthand = Shorthand.get(instance);
            shorthand.load(backpacks, access);

            CompoundTag slotSelection = backpacks.getCompound("slot_selection");
            readSlotSelection("items", inventory.items, instance, slotSelection);
            readSlotSelection("armor", inventory.armor, instance, slotSelection);
            ItemStack offhand = inventory.offhand.getFirst();
            readSlotSelection("offhand", offhand, instance, slotSelection);
            ItemStack back = BackData.get(instance).beans_Backpacks_3$getBody().getFirst();
            readSlotSelection("back", back, instance, slotSelection);
      }

      @Unique
      private static void readSlotSelection(String name, List<ItemStack> items, Player instance, CompoundTag slotSelection) {
            CompoundTag tag = slotSelection.getCompound(name);
            for (String key : tag.getAllKeys()) {
                  int slot = Integer.parseInt(key);
                  ItemStack stack = items.get(slot);
                  readSlotSelection(key, stack, instance, tag);
            }
      }

      @Unique
      private static void readSlotSelection(String name, ItemStack item, Player instance, CompoundTag slotSelection) {
            int selection = slotSelection.getInt(name);
            if (selection == 0)
                  return;

            SlotSelection slotSelection1 = item.getOrDefault(ITraitData.SLOT_SELECTION, new SlotSelection());
            int max = getMaxSelection(item);

            slotSelection1.setSelectedSlot(instance, Math.min(selection, max));
            item.set(ITraitData.SLOT_SELECTION, slotSelection1);
      }

      @Inject(method = "defineSynchedData", at = @At("TAIL"))
      private void backpackSyncedData(SynchedEntityData.Builder pBuilder, CallbackInfo ci) {
            pBuilder.define(IS_OPEN, false);
      }

      @Inject(method = "getWeaponItem", cancellable = true, at = @At("HEAD"))
      private void backpackSyncedData(CallbackInfoReturnable<ItemStack> cir) {
            Shorthand shorthand = Shorthand.get(instance);
            if (shorthand.active) {
                  ItemStack stack = shorthand.getItem(shorthand.selection);
                  cir.setReturnValue(stack);
            }
      }

      @Inject(method = "dropEquipment", at = @At(value = "INVOKE",
                  target = "Lnet/minecraft/world/entity/player/Player;destroyVanishingCursedItems()V"))
      private void backpackDropEquipment(CallbackInfo ci) {
            if (!ServerSave.CONFIG.keep_back_on_death.get()) {
                  ItemStack backpack = instance.getItemBySlot(EquipmentSlot.BODY);
                  PlaceableComponent.get(backpack).ifPresent(placeable -> {
                        BackpackEntity.create(backpack, placeable, Traits.get(backpack), instance.level(), instance.position().add(0, 1, 0), instance.yBodyRot + 180, Direction.UP, instance);
                  });
            }
      }

      @Unique private boolean utilitiesScope = false;

      @Override
      public boolean isUtilityScoped() {
            return utilitiesScope;
      }

      @Override
      public void setUtilityScoped(boolean isScoped) {
            utilitiesScope = isScoped;
      }

      @Inject(method = "isScoping", cancellable = true, at = @At("TAIL"))
      private void backpacks_isScoping(CallbackInfoReturnable<Boolean> cir) {
            if (utilitiesScope)
                  cir.setReturnValue(true);
      }

      @Override
      public void setItemUsed(ItemStack itemstack) {
            if (!itemstack.isEmpty() && !this.isUsingItem()) {
                  this.useItem = itemstack;
                  this.useItemRemaining = itemstack.getUseDuration(this);
                  if (!this.level().isClientSide) {
                        this.setLivingEntityFlag(1, true);
                        this.setLivingEntityFlag(2, false);
                        this.gameEvent(GameEvent.ITEM_INTERACT_START);
                  }
            }
      }
}
