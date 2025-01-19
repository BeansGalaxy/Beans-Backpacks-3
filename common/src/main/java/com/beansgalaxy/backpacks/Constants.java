package com.beansgalaxy.backpacks;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
      public static final String MOD_ID = "beansbackpacks";
      public static final String MOD_NAME = "Beans' Backpacks";
      public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);
      public static final int DEFAULT_LEATHER_COLOR = 0xFF8A4821;

      public static boolean isEmpty(String string) {
              return string == null || string.isEmpty() || string.isBlank();
      }

      public static boolean isEmpty(Component component) {
              return component == null || component.getContents().toString().equals("empty");
      }

      public static Component getName(ItemStack stack) {
              MutableComponent name = Component.empty().append(stack.getHoverName());
              if (stack.has(DataComponents.CUSTOM_NAME)) {
                  name.withStyle(ChatFormatting.ITALIC);
              }

              if (!stack.isEmpty()) {
                  name.withStyle(stack.getRarity().color())
                              .withStyle($$0x -> $$0x.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(stack))));
              }

              return name;
      }

      public static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Block block, ClipContext.Fluid fluid) {
            Vec3 $$3 = player.getEyePosition();
            Vec3 $$4 = $$3.add(player.calculateViewVector(player.getXRot(), player.getYRot()).scale(player.blockInteractionRange()));
            return level.clip(new ClipContext($$3, $$4, block, fluid, player));
      }

      public static String shortString(Item item) {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            return shortString(key);
      }

      public static String shortString(ResourceLocation key) {
            if (key.getNamespace().equals("minecraft"))
                  return key.getPath();
            else
                  return key.getNamespace() + ":" + key.getPath();
      }
}
