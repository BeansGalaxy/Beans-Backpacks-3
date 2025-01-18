package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.components.StackableComponent;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.container.UtilitySlot;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.data.config.options.ShorthandHUD;
import com.beansgalaxy.backpacks.network.serverbound.SyncSelectedSlot;
import com.beansgalaxy.backpacks.container.BackSlot;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.SmoothRandomFloat;
import com.beansgalaxy.backpacks.util.Tint;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CommonClient {

      public static void init() {

      }

      public static final ItemStack NO_GUI_STAND_IN = new ItemStack(Items.AIR);
      public static final ClampedItemPropertyFunction NO_GUI_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            if (itemStack == NO_GUI_STAND_IN && clientLevel == null && livingEntity == null && i == 0)
                  return 1;

            return 0;
      };


      public static final ItemStack UTILITY_DISPLAY_STAND_IN = new ItemStack(Items.AIR);
      public static final ClampedItemPropertyFunction UTILITIES_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            if (itemStack == UTILITY_DISPLAY_STAND_IN && clientLevel == null && livingEntity == null && i == 0)
                  return 1;

            return 0;
      };

      public static final ClampedItemPropertyFunction EATING_TRAIT_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) ->
                  livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && LunchBoxTraits.get(itemStack).isPresent()
                              ? 1.0F : 0.0F;

      public static final ClampedItemPropertyFunction FULLNESS_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            Optional<GenericTraits> optional = Traits.get(itemStack);
            GenericTraits traits;
            PatchedComponentHolder holder;
            if (optional.isPresent()) {
                  traits = optional.get();
                  holder = PatchedComponentHolder.of(itemStack);
            }
            else {
                  EnderTraits enderTraits = itemStack.get(Traits.ENDER);
                  if (enderTraits == null)
                        return 0f;

                  traits = enderTraits.getTrait(clientLevel);
                  holder = enderTraits;
            }

            if (traits.isFull(holder))
                  return 1f;

            Fraction fullness = traits.fullness(holder);
            if (traits.isEmpty(holder) || fullness.equals(Fraction.ZERO))
                  return 0f;

            float v = fullness.floatValue();
            return v * 0.89f + 0.1f;
      };

      public static final ClampedItemPropertyFunction ENDER_SEARCHING_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            EnderTraits enderTraits = itemStack.get(Traits.ENDER);
            if (enderTraits == null || !enderTraits.isLoaded())
                  return 1;
            return 0;
      };

      public static final ItemColor LEATHER_BACKPACK_ITEM_COLOR = (itemStack, layer) -> switch (layer) {
            case 0, 2 -> componentTint(itemStack, Constants.DEFAULT_LEATHER_COLOR);
            case 4 -> componentHighlight(itemStack, Constants.DEFAULT_LEATHER_COLOR);
            default -> 0xFFFFFFFF;
      };

      public static final ItemColor BUNDLE_ITEM_COLOR = (itemStack, layer) -> layer != 1 ?
                  componentTint(itemStack, 0xFFcd7b46) : 0xFFFFFFFF;

      public static final ItemColor BULK_POUCH_ITEM_COLOR = (itemStack, layer) -> layer != 1 ?
                  componentTint(itemStack, 0xFFcd7b46) : 0xFFFFFFFF;

      private static int componentTint(ItemStack itemStack, int rgbBase) {
            DyedItemColor itemColor = itemStack.get(DataComponents.DYED_COLOR);
            if (itemColor != null) {
                  int rgbTint = itemColor.rgb();
                  return smartAverageTint(rgbTint, rgbBase).rgb();
            }
            return rgbBase;
      }

      private static int componentHighlight(ItemStack itemStack, int rgbBase) {
            DyedItemColor itemColor = itemStack.get(DataComponents.DYED_COLOR);
            if (itemColor != null) {
                  rgbBase = itemColor.rgb();
            }
            Tint tint = new Tint(rgbBase);
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.setLum((Math.cbrt(lum + 0.2) + lum) / 2).rotate(5).setSat(Math.sqrt((hsl.getSat() + brightness) / 2));
            return hsl.rgb();
      }

      public static Tint.HSL smartAverageTint(int rgbTint, int rgbBase) {
            Tint tint = new Tint(rgbTint, true);
            Tint base = new Tint(rgbBase);
            tint.modRGB(
                        r -> (r + r + base.getRed()) / 3,
                        g -> (g + g + base.getGreen()) / 3,
                        b -> (b + b + base.getBlue()) / 3
            );
            Tint.HSL tintHsl = tint.HSL();
            tintHsl.modLum(l -> (Math.sqrt(l) + l + l) / 3);
            return tintHsl;
      }

      public static void playSound(SoundEvent soundEvent, float volume, float pitch) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, pitch, volume));
      }

      public static EnderStorage getEnderStorage() {
            MinecraftAccessor instance = (MinecraftAccessor) Minecraft.getInstance();
            return instance.beans_Backpacks_2$getEnder();
      }

      public static Level getLevel() {
            return Minecraft.getInstance().level;
      }

// ===================================================================================================================== SHORTHAND CLIENT

      public static float getHandHeight(float mainHandHeight) {
            return 1f - mainHandHeight;
      }

      public static final ResourceLocation BACK_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/back_slot.png");
      public static final ResourceLocation UTIL_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/util_slot.png");
      private static final ResourceLocation SHORTHAND_START = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/start.png");
      private static final ResourceLocation SHORTHAND_STOP = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/stop.png");
      private static final ResourceLocation SHORTHAND_END = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/end.png");
      private static final ResourceLocation SHORTHAND_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/shorthand.png");

      public static void renderShorthandSlots(GuiGraphics graphics, int leftPos, int topPos, int imageWidth, int imageHeight, LocalPlayer player) {
            graphics.blit(BACK_SLOT, leftPos + BackSlot.getX() - 1, topPos + BackSlot.getY() - 1, 10, 0, 0, 18, 18, 18, 18);
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            byte utilities = UtilityComponent.getSize(backpack);
            if (utilities > 0) for (byte i = 0; i < utilities; i++)
                        graphics.blit(UTIL_SLOT, leftPos + UtilitySlot.getX(i) - 1, topPos + UtilitySlot.getY(i) - 1, 10, 0, 0, 18, 18, 18, 18);

            Shorthand shorthand = Shorthand.get(player);
            int hX = leftPos + imageWidth;
            int hY = topPos + imageHeight - 10;
            int totalSize = shorthand.getContainerSize();
            if (totalSize == 0)
                  return;

            graphics.blit(SHORTHAND_START, hX - 32, hY, 10, 0, 0, 32, 32, 32, 32);
            int i = 0;
            while (i < totalSize) {
                  graphics.blit(SHORTHAND_SLOT, hX - 32 - (i * 18), hY, 10, 0, 0, 32, 32, 32, 32);
                  i++;
            }
            if (totalSize == 9)
                  graphics.blit(SHORTHAND_END, leftPos, hY, 10, 0, 0, 32, 32, 32, 32);
            else
                  graphics.blit(SHORTHAND_STOP, hX - 14 - (i * 18), hY, 10, 0, 0, 32, 32, 32, 32);
      }

      private static final ResourceLocation SHORTHAND_SINGLE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_single");
      private static final ResourceLocation SHORTHAND_LEFT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_left");
      private static final ResourceLocation SHORTHAND_CENTER = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_center");
      private static final ResourceLocation SHORTHAND_RIGHT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_right");

      private static final ResourceLocation SHORTHAND_DORMANT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_dormant");
      private static final ResourceLocation SHORTHAND_SELECT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_selection");

      public static void renderShorthandHUD(Minecraft minecraft, GuiGraphics gui, DeltaTracker tickCounter, Player player) {
            if (player == null || player.isSpectator() || minecraft.options.hideGui)
                  return;

            renderCompassClockHUD(minecraft, gui, player);

            Shorthand shorthand = Shorthand.get(player);

            int size = 0;
            int containerSize = shorthand.getContainerSize();
            for (int i = 0; i < containerSize; i++) {
                  if (!shorthand.getItem(i).isEmpty())
                        size++;
            }

            if (size == 0)
                  return;

            int height = gui.guiHeight();
            int width = gui.guiWidth();
            int y = height - 1 - 18;

            HumanoidArm mainArm = player.getMainArm();
            ShorthandHUD hud = CommonClass.CLIENT_CONFIG.shorthand_hud_location.get();
            boolean hudIsFarCorner = ShorthandHUD.FAR_CORNER.equals(hud);
            int x = getShorthandHudX(mainArm, width, size, hudIsFarCorner);

            if (shorthand.active) {
                  int heldSlot = shorthand.getHeldSlot();
                  int left = width / 2 - 90;
                  int heldX = heldSlot * 20;
                  gui.blitSprite(SHORTHAND_DORMANT, left + heldX - 12, y - 4, 44, 24);
            }

            if (size == 1) {
                  int i = 0;
                  ItemStack weapon;
                  do {
                        weapon = shorthand.getItem(i);
                        i++;

                        if (i > containerSize)
                              return;
                  }
                  while (weapon.isEmpty());

                  RenderSystem.enableBlend();
                  gui.blitSprite(SHORTHAND_SINGLE, x - 6, y - 4, 44, 24);
                  RenderSystem.disableBlend();

                  gui.renderItem(weapon, x + 17, y, player.getId());
                  gui.renderItemDecorations(minecraft.font, weapon, x + 17, y);

                  if (shorthand.active)
                        gui.blitSprite(SHORTHAND_SELECT, x + 3, y - 4, 44, 24);
            }
            else {
                  boolean leftHanded = HumanoidArm.LEFT.equals(mainArm);
                  int offset = (hudIsFarCorner && leftHanded) || (!hudIsFarCorner && !leftHanded) ? size * 20 - 3 : 17;

                  int j = 0;
                  for (int i = 0; i < containerSize; i++) {
                        ItemStack stack = shorthand.getItem(i);
                        if (stack.isEmpty())
                              continue;

                        int spriteX = j * -20 + x + offset;
                        ResourceLocation sprite = j == 0 ? SHORTHAND_RIGHT
                                         : j == size - 1 ? SHORTHAND_LEFT
                                                         : SHORTHAND_CENTER;

                        RenderSystem.enableBlend();
                        gui.blitSprite(sprite, spriteX - 4, y - 4, 24, 24);
                        RenderSystem.disableBlend();

                        gui.renderItem(stack, spriteX, y, minecraft.player.getId());
                        gui.renderItemDecorations(minecraft.font, stack, spriteX, y);

                        if (i == shorthand.selection) {
                              ResourceLocation overlay = shorthand.active ? SHORTHAND_SELECT : SHORTHAND_DORMANT;
                              gui.blitSprite(overlay, spriteX - 14, y - 4, 10, 44, 24);
                        }

                        j++;
                  }
            }
      }

      private static final CompassItemPropertyFunction COMPASS_FUNCTION = new CompassItemPropertyFunction((clientLevel, itemStack, entity) -> {
            LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);
            return lodestoneTracker != null
                   ? lodestoneTracker.target().orElse(null)
                   : CompassItem.getSpawnPosition(clientLevel);
      });

      private static final CompassItemPropertyFunction RECOVERY_FUNCTION = new CompassItemPropertyFunction((clientLevel, itemStack, entity) -> {
            if (entity instanceof Player player) {
                  return player.getLastDeathLocation().orElse(null);
            } else {
                  return null;
            }
      });

      private static void renderCompassClockHUD(Minecraft minecraft, GuiGraphics gui, Player player) {
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            UtilityComponent utilities = backpack.get(ITraitData.UTILITIES);
            if (utilities == null)
                  return;

            ClientLevel level = minecraft.level;
            boolean second = false;
            Iterator<ItemStack> iterator = utilities.iterator();
            while (iterator.hasNext()) {
                  ItemStack stack = iterator.next();

                  int x = 8 + (second ? 24 : 0);
                  if (UtilityComponent.Type.LODESTONE.test(stack)) {
                        RenderSystem.enableBlend();
                        float direction = COMPASS_FUNCTION.unclampedCall(stack, level, player, 0);
                        int compassFrame = modCompassDirection(direction);
                        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "compass_background" + compassFrame);
                        gui.blitSprite(background, x, 0, 32, 32);

                        int pZ1 = 10;
                        Optional<GlobalPos> target = stack.get(DataComponents.LODESTONE_TRACKER).target();
                        drawYDifference("compass_lodestone", gui, player, target, x, 0, pZ1, second);
                        RenderSystem.disableBlend();

                        drawGlint(gui, x, pZ1);
                        second = true;
                        continue;
                  }


                  if (UtilityComponent.Type.RECOVERY.test(stack)) {
                        RenderSystem.enableBlend();
                        float direction = RECOVERY_FUNCTION.unclampedCall(stack, level, player, 0);
                        int compassFrame = modCompassDirection(direction);
                        ResourceLocation clockLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "recovery_background" + compassFrame);
                        gui.blitSprite(clockLocation, x, 0, 32, 32);

                        drawYDifference("recovery_default", gui, player, player.getLastDeathLocation(), x, 0, 0, second);
                        second = true;
                        RenderSystem.disableBlend();
                        continue;
                  }

                  if (UtilityComponent.Type.COMPASS.test(stack)) {
                        RenderSystem.enableBlend();
                        float direction = COMPASS_FUNCTION.unclampedCall(stack, level, player, 0);
                        int compassFrame = modCompassDirection(direction);
                        ResourceLocation background = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "compass_background" + compassFrame);
                        gui.blitSprite(background, x, 0, 32, 32);

                        int rotation = getPlayerRotation(player);
                        ResourceLocation overlay = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "compass_default" + rotation);
                        gui.blitSprite(overlay, x, 0, 10, 32, 32);
                        second = true;
                        RenderSystem.disableBlend();
                        continue;
                  }

                  if (UtilityComponent.Type.CLOCK.test(stack)) {
                        RenderSystem.enableBlend();
                        float day = level.getTimeOfDay(1f);
                        int clockFrame = Mth.floor(day * 92);
                        ResourceLocation clockLocation = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "clock" + clockFrame);
                        gui.blitSprite(clockLocation, x, 0, 32, 32);
                        second = true;
                        RenderSystem.disableBlend();
                  }
            }

      }

      private static int getPlayerRotation(Player player) {
            float o = player.getYRot() + 45;
            while (o > 360)
                  o -= 360;

            while (o < 0)
                  o += 360;

            double rot = (o % 90) / 4.5;
            int v = (int) rot % 20;

            int sub = switch (v) {
                  case 17, 18 -> 4;
                  case 0, 19 -> 1;
                  case 1, 2 -> 2;
                  default -> 3;
            };

            float o1 = (o + 4.5f) % 360;
            int quad = (int) (o1 / 90);

            int rotation = quad * 4 + sub;
            return rotation;
      }

      private static void drawGlint(GuiGraphics gui, int x, int pZ1) {
            PoseStack pose = gui.pose();
            pose.pushPose();
            pose.translate(x, 0, 0);

            float scale = 1/32f;
            Matrix4f matrix4f = pose.last().pose();
            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferbuilder.addVertex(matrix4f, 0, 0, pZ1).setUv(0, 0);
            bufferbuilder.addVertex(matrix4f, 0, 32, pZ1).setUv(0, scale);
            bufferbuilder.addVertex(matrix4f, 32, 32, pZ1).setUv(scale, scale);
            bufferbuilder.addVertex(matrix4f, 32, 0, pZ1).setUv(scale, 0);
            MeshData data = bufferbuilder.buildOrThrow();
            RenderType.glint().draw(data);
            pose.popPose();
      }

      private static final SmoothRandomFloat firstFloat = new SmoothRandomFloat();
      private static final SmoothRandomFloat secondFloat = new SmoothRandomFloat();

      private static void drawYDifference(String prefix, GuiGraphics gui, Player player, Optional<GlobalPos> target, int pX1, int pY1, int pZ1, boolean second) {
            if (target.isPresent() && target.get().dimension().equals(player.level().dimension())) {
                  GlobalPos pos = target.get();
                  double v = pos.pos().getY() - player.getY();
                  double abs = Math.abs(v);
                  int yFrame;
                  if (abs < 8) {
                        yFrame = 0;
                  }
                  else if (abs < 16)
                        yFrame = 1;
                  else if (abs < 32)
                        yFrame = 2;
                  else if (abs < 64)
                        yFrame = 3;
                  else if (abs < 128)
                        yFrame = 4;
                  else
                        yFrame = 5;

                  if (v < 0)
                        yFrame *= -1;
                  yFrame += 6;

                  ResourceLocation overlay = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, prefix + yFrame);
                  gui.blitSprite(overlay, pX1, pY1, pZ1, 32, 32);
            }
            else {
                  RandomSource random = player.getRandom();
                  int fps = Minecraft.getInstance().getFps() + 2;
                  SmoothRandomFloat aFloat = second ? secondFloat : firstFloat;
                  float direction = aFloat.getDirection(random, fps);
                  int yFrame = Mth.floor(direction * 10) + 1;
                  ResourceLocation overlay = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, prefix + yFrame);
                  gui.blitSprite(overlay, pX1, pY1, pZ1, 32, 32);
            }
      }

      private static int modCompassDirection(float d) {
            float f = (d + 0.5f) % 1;

            double r;
            if (f > 0.5)
                  r = 1 - (-(Math.cos(Math.PI * (1 - f)) - 1) / 2);
            else if (f < 0.5)
                  r = -(Math.cos(Math.PI * f) - 1) / 2;
            else
                  r = 0.5;

            return Mth.floor(r * 58) + 1;
      }

      private static int getShorthandHudX(HumanoidArm mainArm, int width, int weaponsSize, boolean hudIsFarCorner) {
            if (HumanoidArm.LEFT.equals(mainArm)) {
                  if (hudIsFarCorner)
                        return -12;
                  else
                        return width / 2 - 134;
            }
            else {
                  if (hudIsFarCorner)
                        return width - 38;
                  else
                        return width / 2 + 84;
            }
      }

      public static void handleSendWeaponSlot(int player, int selectedSlot, ItemStack stack) {
            Minecraft minecraft = Minecraft.getInstance();
            Entity entity = minecraft.level.getEntity(player);
            if (entity instanceof Player otherPlayer) {
                  Shorthand shorthand = Shorthand.get(otherPlayer);
                  shorthand.selection = selectedSlot;
                  shorthand.setItem(selectedSlot, stack);
            }
      }

      public static void modifyBackpackKeyDisplay(Component name, Button changeButton) {
            KeyPress keyPress = KeyPress.INSTANCE;
            if (name.equals(Component.translatable(KeyPress.ACTION_KEY_IDENTIFIER))) {
                  if (keyPress.INSTANT_KEY.isUnbound()) {
                        MutableComponent translatable = Component.translatable("key.sprint");
                        changeButton.setTooltip(Tooltip.create(Component.translatable(KeyPress.ACTION_KEY_DESC, translatable.plainCopy())));
                        if (keyPress.ACTION_KEY.isUnbound())
                              changeButton.setMessage(translatable.withStyle(ChatFormatting.ITALIC));
                  } else {
                        MutableComponent action = Component.translatable(KeyPress.ACTION_KEY_IDENTIFIER);
                        MutableComponent instant = Component.translatable(KeyPress.INSTANT_KEY_IDENTIFIER);
                        changeButton.setTooltip(Tooltip.create(Component.translatable(KeyPress.ACTION_KEY_DISABLED_DESC, action, instant)));
                        MutableComponent disabledMessage = Component.translatable(KeyPress.ACTION_KEY_DISABLED);
                        changeButton.setMessage(disabledMessage.withStyle(ChatFormatting.ITALIC, ChatFormatting.RED));
                  }
                  return;
            }
            if (name.equals(Component.translatable(KeyPress.MENUS_KEY_IDENTIFIER))) {
                  MutableComponent translatable = Component.translatable(KeyPress.ACTION_KEY_IDENTIFIER);
                  changeButton.setTooltip(Tooltip.create(Component.translatable(KeyPress.MENUS_KEY_DESC, translatable)));
                  if (keyPress.MENUS_KEY.isUnbound()) {
                        String boundKey = KeyPress.getMenusKeyBind().getName();
                        changeButton.setMessage(Component.translatable(boundKey).withStyle(ChatFormatting.ITALIC));
                  }
                  return;
            }
            if (name.equals(Component.translatable(KeyPress.INSTANT_KEY_IDENTIFIER))) {
                  MutableComponent translatable = Component.translatable(KeyPress.ACTION_KEY_IDENTIFIER);
                  changeButton.setTooltip(Tooltip.create(Component.translatable(KeyPress.INSTANT_KEY_DESC, translatable)));
                  return;
            }
      }


      public static void handleKeyBinds(LocalPlayer player, @Nullable HitResult hitResult) {
            KeyPress keyPress = KeyPress.INSTANCE;
            while (keyPress.INSTANT_KEY.consumeClick()) {
                  if (hitResult instanceof BlockHitResult blockHitResult) {
                        if (KeyPress.placeBackpack(player, blockHitResult))
                              continue;
                        if (keyPress.pickUpThru(player))
                              continue;
                  }

                  if (hitResult instanceof EntityHitResult entityHitResult) {
                        Entity hit = entityHitResult.getEntity();
                        KeyPress.tryEquip(player, hit);
                  }
            }

            keyPress.handleUtility(player, hitResult);
      }

      public static Boolean cancelCapeRender(Player player) {
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            return EquipableComponent.get(backpack).map(equipable -> {
                  ResourceLocation texture = equipable.backpackTexture();
                  return texture != null;
            }).orElse(false);
      }

      public static boolean scrollTraits(ItemStack stack, ClientLevel level, int containerId, int scrolled, Slot hoveredSlot) {
            StackableComponent component = stack.get(ITraitData.STACKABLE);
            if (component != null) {
                  LocalPlayer player = Minecraft.getInstance().player;
                  int startSlot = component.selection.getSelectedSlot(player);

                  ItemStack carried = player.containerMenu.getCarried();
                  if (!carried.isEmpty())
                        return false;


                  int selectedSlot = startSlot - scrolled;
                  int size = component.stacks().size();
                  int i = size == 0 || selectedSlot < 0 ? 0
                          : Math.min(selectedSlot, size - 1);

                  if (startSlot == i)
                        return true;

                  component.selection.setSelectedSlot(player, i);
                  SyncSelectedSlot.send(containerId, hoveredSlot.index, i);

                  return true;
            }

            Optional<ItemStorageTraits> optionalStorage = ItemStorageTraits.get(stack);
            if (optionalStorage.isPresent()) {
                  ItemStorageTraits traits = optionalStorage.get();
                  PatchedComponentHolder holder = PatchedComponentHolder.of(stack);
                  return traits.client().mouseScrolled(traits, holder, level, hoveredSlot, containerId, scrolled);
            }

            Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
            if (optionalEnder.isPresent()) {
                  EnderTraits enderTraits = optionalEnder.get();
                  Optional<GenericTraits> optional = enderTraits.getTrait();
                  if (optional.isPresent()) {
                        GenericTraits traits = optional.get();
                        if (traits instanceof ItemStorageTraits storageTraits) {
                              return traits.client().mouseScrolled(storageTraits, enderTraits, level, hoveredSlot, containerId, scrolled);
                        }
                  }
            }
            return false;
      }

      public static void addStackableLines(int selectedSlot, StackableComponent component, List<Component> lines) {
            MutableComponent empty = Component.empty();
            MutableComponent pre = Component.literal("|".repeat(selectedSlot)).withStyle(ChatFormatting.GRAY);
            empty.append(pre).append(Component.literal("|").withStyle(ChatFormatting.GOLD));
            int postCount = component.stacks().size() - 1 - selectedSlot;
            MutableComponent post = Component.literal("|".repeat(postCount)).withStyle(ChatFormatting.GRAY);
            empty.append(post);
            lines.add(empty);
      }
}
