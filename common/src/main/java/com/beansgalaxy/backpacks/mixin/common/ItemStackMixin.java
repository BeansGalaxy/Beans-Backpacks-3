package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.components.ender.EnderItem;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.function.TriConsumer;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
      @Shadow public abstract Item getItem();

      @Unique public final ItemStack instance = (ItemStack) (Object) this;

      @Inject(method = "isStackable", at = @At("HEAD"), cancellable = true)
      private void backpackIsStackable(CallbackInfoReturnable<Boolean> cir) {
            Traits.runIfPresent(instance, traits -> {
                  if (!traits.isStackable(ComponentHolder.of(instance)))
                        cir.setReturnValue(false);
            });
      }
      
      @Inject(method="addDetailsToTooltip", at=@At("HEAD"))
      private void addDetailsToToolTip(Item.TooltipContext context, TooltipDisplay tooltipDisplay, Player player, TooltipFlag tooltipFlag, Consumer<Component> adder, CallbackInfo ci) {
            EnderItem.getEnderTrait(instance).ifPresentOrElse(enderTraits -> {
                  GenericTraits trait = enderTraits.getTrait(CommonClient.getLevel());
                  Component displayName = enderTraits.getDisplayName();
                  
                  adder.accept(Component.translatable("ender.beansbackpacks.bound_player", displayName).withStyle(ChatFormatting.GOLD));
                  trait.client().appendTooltipLines(trait, adder);
                  if (tooltipFlag.isAdvanced())
                        adder.accept(Component.translatable(
                              "tooltip.beansbackpacks.advanced.reference",
                              Component.literal(enderTraits.trait().toString())
                        ).withStyle(ChatFormatting.DARK_GRAY));
            }, () -> Traits.runIfPresent(instance, traits -> {
                  traits.client().appendTooltipLines(traits, adder);
            }));
      }

      @Inject(method = "addAttributeTooltips", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V"))
      private void backpackAttributeLines(Consumer<Component> tooltipAdder, TooltipDisplay tooltipDisplay, Player player, CallbackInfo ci, @Local EquipmentSlotGroup slotGroup, @Local MutableBoolean mutableboolean) {
            CommonClass.addAttributesToTooltip(tooltipAdder, slotGroup, mutableboolean, instance);
      }
      
      @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"))
      private void appendReferenceAttributesForEachSlot(EquipmentSlot pEquipmentSLot, BiConsumer<Holder<Attribute>, AttributeModifier> pAction, CallbackInfo ci) {
            ReferenceTrait.ifAttributesPresent(instance, modifiers -> {
                  if (!modifiers.modifiers().isEmpty()) {
                        modifiers.forEach(pEquipmentSLot, pAction);
                  }
            });
      }

      @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Lorg/apache/commons/lang3/function/TriConsumer;)V", at = @At(value = "HEAD"))
      private void appendReferenceAttributesForEachGroup(EquipmentSlotGroup slot, TriConsumer<Holder<Attribute>, AttributeModifier, ItemAttributeModifiers.Display> action, CallbackInfo ci) {
            ReferenceTrait.ifAttributesPresent(instance, modifiers -> {
                  if (!modifiers.modifiers().isEmpty()) {
                        modifiers.forEach(slot, action);
                  }
            });
      }
      
      @Inject(method="onUseTick", at=@At(value = "JUMP", opcode = 198, ordinal = 0))
      private void onUseTick(Level level, LivingEntity livingEntity, int remainingUseDuration, CallbackInfo ci, @Local LocalRef<Consumable> consumable) {
            if (consumable.get() != null)
                  return;
            
            LunchBoxTraits.selectionIsPresent(instance, livingEntity, itemStack -> {
                  Consumable newConsume = itemStack.get(DataComponents.CONSUMABLE);
                  if (newConsume == null)
                        return;
                  
                  consumable.set(newConsume);
            });
      }
      
      @Deprecated(since = "0.11-beta")
      @Inject(method="applyComponentsAndValidate", at=@At("HEAD"))
      private void applyComponentsAndValidate(DataComponentPatch components, CallbackInfo ci) {
            ItemContainerContents contents = instance.get(ITraitData.CHEST);
            if (contents == null)
                  return;
            
            Stream<ItemStack> stream = contents.nonEmptyStream();
            
            List<ItemStack> stacks = instance.get(ITraitData.ITEM_STACKS);
            if (stacks != null)
                  stream = Stream.concat(stream, stacks.stream());
             
            instance.set(ITraitData.ITEM_STACKS, stream.toList());
      }
}
