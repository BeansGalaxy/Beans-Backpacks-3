package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.container.UtilitySlot;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.container.BackSlot;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.SmoothRandomFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngle;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Optional;

public class CommonClient {

      public static void init() {

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

      public static final ResourceLocation BACK_SLOT = Constants.defaultLocation("textures/gui/slots/back_slot");
      public static final ResourceLocation UTIL_SLOT = Constants.defaultLocation("textures/gui/slots/util_slot");

      public static void renderSlots(GuiGraphics graphics, int leftPos, int topPos, int imageWidth, int imageHeight, LocalPlayer player) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ResourceLocation.withDefaultNamespace("container/slot/back_slot"), leftPos + BackSlot.getX() - 1, topPos + BackSlot.getY() - 1, 10, 0, 0, 18, 18, 18, 18);
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            byte utilities = UtilityComponent.getSize(backpack);
            if (utilities > 0) for (byte i = 0; i < utilities; i++)
                  graphics.blit(RenderPipelines.GUI_TEXTURED, UTIL_SLOT, leftPos + UtilitySlot.getX(i) - 1, topPos + UtilitySlot.getY(i) - 1, 10, 0, 0, 18, 18, 18, 18);
      }


// ===================================================================================================================== SHORTHAND CLIENT

      public static void renderCompassClockHUD(Minecraft minecraft, GuiGraphics gui, Player player) {
            if (player == null || player.isSpectator() || minecraft.options.hideGui)
                  return;

            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            UtilityComponent utilities = backpack.get(ITraitData.UTILITIES);
            if (utilities == null)
                  return;

            ClientLevel level = minecraft.level;
            boolean second = false;
            RegistryAccess access = minecraft.level.registryAccess();
            
            Iterator<ItemStack> iterator = utilities.iterator();
            while (iterator.hasNext()) {
                  ItemStack stack = iterator.next();

                  int x = 8 + (second ? 24 : 0);
                  if (UtilityComponent.Type.LODESTONE.test(stack, access)) {
//                        RenderSystem.enableBlend();
                        float direction = new CompassAngle(true, CompassAngleState.CompassTarget.LODESTONE).get(stack, level, player, 0);
                        int compassFrame = modCompassDirection(direction);
                        ResourceLocation background = Constants.defaultLocation("compass_background" + compassFrame);
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, background, x, 0, 32, 32);
                        gui.blitSprite(RenderType.glint().pipeline(), background, x, 0, 32, 32);
                        gui.nextStratum();
                        
                        Optional<GlobalPos> target = stack.get(DataComponents.LODESTONE_TRACKER).target();
                        drawYDifference("compass_lodestone", gui, player, target, x, 0, second);
//                        RenderSystem.disableBlend();
                        
                        second = true;
                        continue;
                  }


                  if (UtilityComponent.Type.RECOVERY.test(stack, access)) {
//                        RenderSystem.enableBlend();
                        float direction = new CompassAngle(true, CompassAngleState.CompassTarget.RECOVERY).get(stack, level, player, 0);
                        int compassFrame = modCompassDirection(direction);
                        ResourceLocation clockLocation = Constants.defaultLocation("recovery_background" + compassFrame);
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, clockLocation, x, 0, 32, 32);

                        drawYDifference("recovery_default", gui, player, player.getLastDeathLocation(), x, 0, second);
                        second = true;
//                        RenderSystem.disableBlend();
                        continue;
                  }

                  if (UtilityComponent.Type.COMPASS.test(stack, access)) {
//                        RenderSystem.enableBlend();
                        float direction = new CompassAngle(true, CompassAngleState.CompassTarget.SPAWN).get(stack, level, player, 0);
                        int compassFrame = modCompassDirection(direction);
                        ResourceLocation background = Constants.defaultLocation("compass_background" + compassFrame);
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, background, x, 0, 32, 32);

                        int rotation = getPlayerRotation(player);
                        ResourceLocation overlay = Constants.defaultLocation("compass_default" + rotation);
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, overlay, x, 0, 10, 32, 32);
                        second = true;
//                        RenderSystem.disableBlend();
                        continue;
                  }
                  
                  if (UtilityComponent.Type.CLOCK.test(stack, access)) {
//                        RenderSystem.enableBlend();
                        float day = level.getTimeOfDay(1f);
                        int clockFrame = Mth.floor(day * 92);
                        ResourceLocation clockLocation = Constants.defaultLocation("clock" + clockFrame);
                        gui.blitSprite(RenderPipelines.GUI_TEXTURED, clockLocation, x, 0, 32, 32);
                        second = true;
//                        RenderSystem.disableBlend();
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
      
      private static final SmoothRandomFloat firstFloat = new SmoothRandomFloat();
      private static final SmoothRandomFloat secondFloat = new SmoothRandomFloat();

      private static void drawYDifference(String prefix, GuiGraphics gui, Player player, Optional<GlobalPos> target, int pX1, int pY1, boolean second) {
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

                  ResourceLocation overlay = Constants.defaultLocation(prefix + yFrame);
                  gui.blitSprite(RenderPipelines.GUI_TEXTURED, overlay, pX1, pY1, 32, 32);
            }
            else {
                  RandomSource random = player.getRandom();
                  int fps = Minecraft.getInstance().getFps() + 2;
                  SmoothRandomFloat aFloat = second ? secondFloat : firstFloat;
                  float direction = aFloat.getDirection(random, fps);
                  int yFrame = Mth.floor(direction * 10) + 1;
                  ResourceLocation overlay = Constants.defaultLocation(prefix + yFrame);
                  gui.blitSprite(RenderPipelines.GUI_TEXTURED, overlay, pX1, pY1, 32, 32);
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
            BackpackTraits traits = BackpackTraits.get(backpack);
            return traits != null;
      }

      public static boolean scrollTraits(Player player, ItemStack stack, Level level, int containerId, int scrolled, Slot hoveredSlot) {
            ISlotSelectorTrait trait = ISlotSelectorTrait.get(stack);
            if (trait != null) {
                  ComponentHolder holder = ComponentHolder.of(stack);
                  return trait.mouseScrolled(player, holder, hoveredSlot.index, containerId, scrolled);
            }

            Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
            if (optionalEnder.isPresent()) {
                  EnderTraits enderTraits = optionalEnder.get();
                  Optional<GenericTraits> optional = enderTraits.getTrait();
                  if (optional.isPresent()) {
                        GenericTraits traits = optional.get();
                        if (traits instanceof ISlotSelectorTrait storageTraits) {
                              return storageTraits.mouseScrolled(player, enderTraits, hoveredSlot.index, containerId, scrolled);
                        }
                  }
            }
            return false;
      }

      public static void renderItemDecorations(GuiGraphics gui, Font font, ItemStack pStack, int x, int y) {
            if (!pStack.isEmpty()) {
                  gui.nextStratum();
                  
                  int count = pStack.getCount();
                  if (count != 1) {
                        String string = String.valueOf(count);
                        if (count > 999) {
                              float k = count / 1000f;
                              String s = (int) k + "k";
                              gui.drawString(font, s, x + 9 - font.width(s), y + 1, 0xFFFFFFFF, true);
                        }
                        else if (count > 99) {
                              char[] chars = string.toCharArray();
                              for (int i1 = 0; i1 < chars.length; i1++) {
                                    char c = chars[i1];
                                    String s = CommonClass.getTinyNumberFromDigitChar(c);
                                    chars[i1] = s.toCharArray()[0];
                              }

                              String s = String.valueOf(chars);
                              gui.drawString(font, s, x + 10 - font.width(s), y - 1, 0xFFFFFFFF, true);
                        }
                        else {
                              gui.drawString(font, string, x + 9 - font.width(string), y + 1, 0xFFFFFFFF, true);
                        }
                  }
                  else if (pStack.isBarVisible()) {
                        int barColor = pStack.getBarColor();
                        int barX = x - 6;
                        int barY = y + 5;
                        gui.fill(barX, barY, barX + 13, barY + 2, 0xFF000000);
                        gui.fill(barX, barY, barX + pStack.getBarWidth(), barY + 1, barColor | -16777216);
                  }
                  
                  Optional<GenericTraits> traitsOptional = Traits.get(pStack);
                  if (traitsOptional.isPresent()) {
                        GenericTraits traits = traitsOptional.get();
                        traits.client().renderItemDecorations(traits, ComponentHolder.of(pStack), gui, font, pStack, x - 8, y - 8);
                  }
                  
            }
      }

      public static void renderItem(Minecraft minecraft, GuiGraphics gui, ItemStack stack, int x, int y) {
            gui.nextStratum();
            gui.renderItem(stack, x - 8, y - 8, 0);
      }
      
      public static void handleSentItemComponentPatch(int slot, DataComponentPatch patch) {
            if (slot < 0)
                  return;

            Minecraft minecraft = Minecraft.getInstance();
            Inventory inventory = minecraft.player.getInventory();
            ItemStack stack = inventory.getItem(slot);
            stack.applyComponents(patch);
      }
      
      private static final ResourceLocation R_CLICK_ICON = Constants.defaultLocation("r_click_icon");
      
      public static void renderInfoTooltip(GuiGraphics gui, int mouseX, int mouseY, ComponentHolder holder) {
            int x = mouseX - 20;
            int y = mouseY - 8;
            TooltipRenderUtil.renderTooltipBackground(gui, x, y, 11, 13, holder.get(DataComponents.TOOLTIP_STYLE));
            gui.blitSprite(RenderPipelines.GUI_TEXTURED, R_CLICK_ICON, 16, 16, 0, 0, x - 2, y - 2, 16, 16);
            
      }
}
