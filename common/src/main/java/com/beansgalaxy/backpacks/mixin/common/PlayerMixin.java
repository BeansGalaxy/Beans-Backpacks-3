package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.PlayerAccessor;
import com.beansgalaxy.backpacks.util.SelectionBySlot;
import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.components.SlotSelection;
import com.beansgalaxy.backpacks.data.ServerSave;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.quiver.QuiverTraits;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements ViewableAccessor, PlayerAccessor {
      protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
            super(pEntityType, pLevel);
      }

      @Shadow public abstract Inventory getInventory();

      @Shadow @Final private Inventory inventory;
      
      @Shadow public abstract void remove(RemovalReason reason);

      @Shadow public abstract void travel(Vec3 travelVector);

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

            @Override protected ComponentHolder holder() {
                  return ComponentHolder.of(toStack());
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
            
      };

      @Override public ViewableBackpack getViewable() {
            return viewable;
      }

      @Inject(method = "getProjectile", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true, at = @At(value = "INVOKE", shift = At.Shift.BEFORE,
                  target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getHeldProjectile(Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Predicate;)Lnet/minecraft/world/item/ItemStack;"))
      private void getBackpackProjectile(ItemStack pShootable, CallbackInfoReturnable<ItemStack> cir, Predicate<ItemStack> predicate) {
            QuiverTraits.runIfPresent(instance, (proTrait, slot, quiver, holder) -> {
                  List<ItemStack> stacks = holder.get(ITraitData.ITEM_STACKS);
                  if (stacks == null || stacks.isEmpty())
                        return false;

                  int selectedSlot = proTrait.getSelectedSlot(holder, instance);
                  ItemStack stack = stacks.get(selectedSlot);
                  if (predicate.test(stack)) {
                        cir.setReturnValue(stack);
                        return true;
                  }

                  return false;
            });
      }

       @Inject(method = "interactOn", cancellable = true, at = @At("HEAD"))
      private void backpackInteractOn(Entity pEntityToInteractOn, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
             if (pEntityToInteractOn instanceof Player player)
                   CommonClass.interactEquippedBackpack(player, instance, cir);
       }

      @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
      private void backpackAddSaveData(ValueOutput output, CallbackInfo ci) {
            ValueOutput selections = output.child("slot_selection");
            Inventory inventory = getInventory();
            saveSelectedSlots("items", inventory.getNonEquipmentItems(), instance, selections);
            
            for (EquipmentSlot slot : new EquipmentSlot[] {
                  EquipmentSlot.HEAD,
                  EquipmentSlot.CHEST,
                  EquipmentSlot.LEGS,
                  EquipmentSlot.FEET,
                  EquipmentSlot.OFFHAND,
                  EquipmentSlot.BODY
            }) {
                  ItemStack item = instance.getItemBySlot(slot);
                  SlotSelection slotSelection = item.get(ITraitData.SLOT_SELECTION);
                  if (slotSelection == null)
                        continue;
                  
                  int selectedSlot = slotSelection.get(instance);
                  if (selectedSlot == 0)
                        continue;
                  
                  selections.putInt(slot.name(), selectedSlot);
            }
      }
      
      private void saveSelectedSlots(String name, NonNullList<ItemStack> items, Player player, ValueOutput output) {
            int size = items.size();
            ArrayList<SelectionBySlot> selectionBySlots = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                  ItemStack item = items.get(i);
                  SlotSelection slotSelection = item.get(ITraitData.SLOT_SELECTION);
                  if (slotSelection == null)
                        continue;
                  
                  int selectedSlot = slotSelection.get(instance);
                  if (selectedSlot == 0)
                        continue;
                  
                  selectionBySlots.add(new SelectionBySlot(i, selectedSlot));
            }
            
            output.store(name, SelectionBySlot.LIST_CODEC, selectionBySlots);
      }

      @Unique
      private static int getMaxSelection(ItemStack item) {
            List<ItemStack> stacks = item.get(ITraitData.ITEM_STACKS);
            if (stacks == null || stacks.isEmpty())
                  return 0;

            return stacks.size() - 1;
      }


      @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
      private void backpackReadSaveData(ValueInput input, CallbackInfo ci) {
            Optional<ValueInput> slot_selection = input.child("slot_selection");
            if (slot_selection.isPresent()) {
                  ValueInput selections = slot_selection.get();
                  readSlotSelection("items", inventory.getNonEquipmentItems(), instance, selections);
                  
                  for (EquipmentSlot equipmentSlot : new EquipmentSlot[] {
                        EquipmentSlot.HEAD,
                        EquipmentSlot.CHEST,
                        EquipmentSlot.LEGS,
                        EquipmentSlot.FEET,
                        EquipmentSlot.OFFHAND,
                        EquipmentSlot.BODY
                  }) {
                        Optional<Integer> selection = input.getInt(equipmentSlot.name());
                        if (selection.isEmpty())
                              continue;
                        
                        ItemStack stack = instance.getItemBySlot(equipmentSlot);
                        int max = getMaxSelection(stack);
                        if (max == 0)
                              continue;
                        
                        SlotSelection slotSelection1 = stack.getOrDefault(ITraitData.SLOT_SELECTION, new SlotSelection());
                        
                        slotSelection1.set(instance, Math.min(selection.get(), max));
                        stack.set(ITraitData.SLOT_SELECTION, slotSelection1);
                  }
            }
            
            legacyCollectBackItem(input);
      }
      
      private void readSlotSelection(String name, NonNullList<ItemStack> items, Player player, ValueInput input) {
            Optional<List<SelectionBySlot>> optional = input.read(name, SelectionBySlot.LIST_CODEC);
            if (optional.isEmpty())
                  return;
            
            List<SelectionBySlot> slots = optional.get();
            for (SelectionBySlot slot : slots) {
                  int i = slot.slot();
                  ItemStack stack = items.get(i);
                  int max = getMaxSelection(stack);
                  if (max == 0)
                        continue;
                  
                  SlotSelection slotSelection1 = stack.getOrDefault(ITraitData.SLOT_SELECTION, new SlotSelection());
                  
                  int selection = slot.selection();
                  slotSelection1.set(player, Math.min(selection, max));
                  stack.set(ITraitData.SLOT_SELECTION, slotSelection1);
            }
      }
      
      @Deprecated(since = "1.21.1")
      private void legacyCollectBackItem(ValueInput input) {
            Optional<ValueInput> optional = input.child(Constants.MOD_ID);
            if (optional.isEmpty())
                  return;
            
            ValueInput backpacks = optional.get();
            backpacks.read("back", ItemStack.OPTIONAL_CODEC).ifPresent(stack -> {
                  setItemSlot(EquipmentSlot.BODY, stack);
            });
      }

      @Inject(method = "defineSynchedData", at = @At("TAIL"))
      private void backpackSyncedData(SynchedEntityData.Builder pBuilder, CallbackInfo ci) {
            pBuilder.define(IS_OPEN, false);
      }

      @Inject(method = "dropEquipment", at = @At(value = "HEAD"))
      private void backpackDropEquipment(CallbackInfo ci) {
            if (level() instanceof ServerLevel level) {
                  if (!ServerSave.CONFIG.keepBackpack(level))
                        BackpackEntity.drop(instance);
                  
                  if (level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                        for (EquipmentSlot slot: new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                              ItemStack backpack = instance.getItemBySlot(slot);
                              BackpackTraits traits = BackpackTraits.get(backpack);
                              if (traits == null)
                                    continue;
                              
                              List<ItemStack> stacks = backpack.remove(ITraitData.ITEM_STACKS);
                              if (stacks == null)
                                    continue;
                              
                              for (ItemStack stack : stacks)
                                    instance.drop(stack, true, false);
                        }
                  }
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
      
}
