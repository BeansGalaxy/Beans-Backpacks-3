package com.beansgalaxy.backpacks.mixin;

import com.beansgalaxy.backpacks.CommonClass;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(AttributeUtil.class)
public class AttributeUtilMixin {
      @Inject(method="applyModifierTooltips", at=@At(value="INVOKE",
            target="Lcom/google/common/collect/Multimap;isEmpty()Z"))
      private static void applyModifierTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx, CallbackInfo ci,
            @Local Multimap<Holder<Attribute>, AttributeModifier> modifiers, @Local EquipmentSlotGroup group
      ) {
            if (modifiers.isEmpty()) {
                  CommonClass.addAttributesToTooltip(tooltip, group, new MutableBoolean(true), stack);
            }
      }
      
      @Inject(method="applyModifierTooltips", at=@At(value="INVOKE",
            target="Lnet/neoforged/neoforge/common/util/AttributeUtil;applyTextFor(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lcom/google/common/collect/Multimap;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V"))
      private static void applyModifierTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx, CallbackInfo ci,
            @Local EquipmentSlotGroup group
      ) {
            CommonClass.addAttributesToTooltip(tooltip, group, new MutableBoolean(false), stack);
      }
}
