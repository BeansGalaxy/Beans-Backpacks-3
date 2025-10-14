package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.container.UtilitySlot;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.container.BackSlot;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.abstract_traits.ISlotSelectorTrait;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.SmoothRandomFloat;
import com.beansgalaxy.backpacks.util.Tint;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
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
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.Optional;

public class CommonClient {

      public static void init() {

      }

      public static final ItemStack UTILITY_DISPLAY_STAND_IN = new ItemStack(Items.AIR);
      public static final ClampedItemPropertyFunction UTILITIES_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            if (itemStack == UTILITY_DISPLAY_STAND_IN && clientLevel == null && livingEntity == null && i == 0)
                  return 1;

            return 0;
      };

      public static final ClampedItemPropertyFunction EATING_TRAIT_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) ->
                  livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack && LunchBoxTraits.get(itemStack) != null
                              ? 1.0F : 0.0F;

      public static final ClampedItemPropertyFunction FULLNESS_ITEM_PREDICATE = (itemStack, clientLevel, livingEntity, i) -> {
            Optional<GenericTraits> optional = Traits.get(itemStack);
            GenericTraits traits;
            ComponentHolder holder;
            if (optional.isPresent()) {
                  traits = optional.get();
                  holder = ComponentHolder.of(itemStack);
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
            case 0 -> componentTint(itemStack, Constants.DEFAULT_LEATHER_COLOR);
            case 4, 2 -> componentHighlight(itemStack, Constants.DEFAULT_LEATHER_COLOR);
            default -> 0xFFFFFFFF;
      };

      public static final ItemColor BUNDLE_ITEM_COLOR = (itemStack, layer) -> layer != 1 ?
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

            int rgb = itemColor == null ? rgbBase : itemColor.rgb();

            Tint tint = new Tint(rgb);
            double brightness = tint.brightness();
            Tint.HSL hsl = tint.HSL();
            double lum = hsl.getLum();
            hsl.rotate(10);
            hsl.setLum((Math.pow(brightness, 4) + lum + 2.3 + (tint.getBlue() / 160.0)) / 5);
            double sat = hsl.getSat();
            hsl.setSat((1 - brightness + sat) / 2);
            return hsl.rgb();
      }

      public static Tint.HSL smartAverageTint(int rgbTint, int rgbBase) {
            Tint tint = new Tint(rgbTint, true);
            tint.setAlpha(1f);
            Tint.HSL tintHsl = tint.HSL();
            tintHsl.modLum(l -> (Math.sqrt(l) + l) / 2);
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

      public static final ResourceLocation BACK_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/back_slot.png");
      public static final ResourceLocation UTIL_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/util_slot.png");

      public static void renderSlots(GuiGraphics graphics, int leftPos, int topPos, int imageWidth, int imageHeight, LocalPlayer player) {
            graphics.blit(BACK_SLOT, leftPos + BackSlot.getX() - 1, topPos + BackSlot.getY() - 1, 10, 0, 0, 18, 18, 18, 18);
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            byte utilities = UtilityComponent.getSize(backpack);
            if (utilities > 0) for (byte i = 0; i < utilities; i++)
                  graphics.blit(UTIL_SLOT, leftPos + UtilitySlot.getX(i) - 1, topPos + UtilitySlot.getY(i) - 1, 10, 0, 0, 18, 18, 18, 18);
      }


// ===================================================================================================================== SHORTHAND CLIENT

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

      public static void renderCompassClockHUD(Minecraft minecraft, GuiGraphics gui, Player player) {
            if (player == null || player.isSpectator() || minecraft.options.hideGui)
                  return;

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
            BackpackTraits traits = BackpackTraits.get(backpack);
            if (traits == null) {
                  return false;
            }

            return true;
      }

      public static boolean scrollTraits(Player player, ItemStack stack, Level level, int containerId, int scrolled, Slot hoveredSlot) {
            ISlotSelectorTrait trait = ISlotSelectorTrait.get(stack);
            if (trait != null) {
                  ComponentHolder holder = ComponentHolder.of(stack);
                  return trait.mouseScrolled(player, holder, level, hoveredSlot, containerId, scrolled);
            }

            Optional<EnderTraits> optionalEnder = EnderTraits.get(stack);
            if (optionalEnder.isPresent()) {
                  EnderTraits enderTraits = optionalEnder.get();
                  Optional<GenericTraits> optional = enderTraits.getTrait();
                  if (optional.isPresent()) {
                        GenericTraits traits = optional.get();
                        if (traits instanceof ISlotSelectorTrait storageTraits) {
                              return storageTraits.mouseScrolled(player, enderTraits, level, hoveredSlot, containerId, scrolled);
                        }
                  }
            }
            return false;
      }

      public static void renderItemDecorations(GuiGraphics gui, Font font, ItemStack $$1, int x, int y, int z) {
            if (!$$1.isEmpty()) {
                  PoseStack pose = gui.pose();
                  pose.pushPose();
                  pose.translate(0.0F, 0.0F, z + 10);
                  int count = $$1.getCount();
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
                  else if ($$1.isBarVisible()) {
                        int barColor = $$1.getBarColor();
                        int barX = x - 6;
                        int barY = y + 5;
                        gui.fill(barX, barY, barX + 13, barY + 2, 0xFF000000);
                        gui.fill(barX, barY, barX + $$1.getBarWidth(), barY + 1, barColor | -16777216);
                  }
                  pose.popPose();
            }
      }

      public static void renderItem(Minecraft minecraft, GuiGraphics gui, ItemStack stack, int x, int y, int z, boolean drawShadows) {
            PoseStack pose = gui.pose();
            pose.pushPose();
            BakedModel model = minecraft.getItemRenderer().getModel(stack, minecraft.level, minecraft.player, 0);
            pose.translate(x, y, z);

            renderModel(minecraft, gui, stack, drawShadows, pose, model);

            pose.popPose();
      }

      public static void renderModel(Minecraft minecraft, GuiGraphics gui, ItemStack stack, boolean drawShadows, PoseStack pose, BakedModel model) {
            try {
                  pose.mulPose((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
                  pose.scale(16.0F, 16.0F, 16.0F);
                  boolean $$8 = !model.usesBlockLight();
                  if ($$8) {
                        Lighting.setupForFlatItems();
                  }

                  minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, gui.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
                  if (drawShadows && !model.isGui3d()) {
                        pose.translate(1/16f, -1/16f, -1/16f);
                        minecraft.getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pose, gui.bufferSource(), 0, OverlayTexture.NO_OVERLAY, model);
                  }

                  gui.flush();
                  if ($$8) {
                        Lighting.setupFor3DItems();
                  }
            } catch (Throwable var12) {
                  CrashReport $$10 = CrashReport.forThrowable(var12, "Rendering item");
                  CrashReportCategory $$11 = $$10.addCategory("Item being rendered");
                  $$11.setDetail("Item Type", () -> String.valueOf(stack.getItem()));
                  $$11.setDetail("Item Components", () -> String.valueOf(stack.getComponents()));
                  $$11.setDetail("Item Foil", () -> String.valueOf(stack.hasFoil()));
                  throw new ReportedException($$10);
            }
      }

      public static void renderHoveredItem(Minecraft minecraft, GuiGraphics instance, ItemStack stack, int x, int y, int seed, Operation<Void> original, String modelName) {
            ISlotSelectorTrait trait = ISlotSelectorTrait.get(stack);
            if (trait != null) {
                  ItemStack food = trait.getHoverItem(ComponentHolder.of(stack), minecraft.player);
                  if (food != null) {
                        original.call(instance, food, x, y, seed);
                  }
                  else {
                        ModelResourceLocation location = Services.PLATFORM.getModelVariant(
                                    ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpack/" + modelName)
                        );

                        BakedModel model = minecraft.getItemRenderer().getItemModelShaper().getModelManager().getModel(location);
                        PoseStack pose = instance.pose();
                        pose.pushPose();
                        pose.translate(x + 8, y + 8, 0);
                        renderModel(minecraft, instance, stack, false, pose, model);
                        pose.popPose();
                  }
            }
      }

      public static void handleSentItemComponentPatch(int slot, DataComponentPatch patch) {
            if (slot < 0)
                  return;

            Minecraft minecraft = Minecraft.getInstance();
            Inventory inventory = minecraft.player.getInventory();
            ItemStack stack = inventory.getItem(slot);
            stack.applyComponents(patch);
      }
      
      public static void testingHitbox(Minecraft instance, HitResult result, LocalPlayer player) {
            if (result == null) {
                  return;
            }
            
            if (HitResult.Type.MISS.equals(result.getType()))
                  return;
            
            BlockHitResult blockHitResult;
            if (result instanceof BlockHitResult bhr)
                  blockHitResult = bhr;
            else
                  return;
            
            Level level = player.level();
            
            BlockPos blockPos = blockHitResult.getBlockPos();
            
            Direction direction;
            Vec3 cursor;
            float rotation = player.getYRot();
            
            if (level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()) {
                  BlockHitResult hitResult = Constants.getPlayerPOVHitResult(level, player, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
                  direction = HitResult.Type.MISS.equals(hitResult.getType()) ? Direction.UP : hitResult.getDirection();
                  cursor = hitResult.getLocation();
            }
            else {
                  direction = blockHitResult.getDirection();
                  cursor = blockHitResult.getLocation();
            }
            
            double d = (4 / 32.0);
            double w = (8 / 32.0);
            double h = 9 / 16.0;
            float yRot;
            Vec3 pos;
            
            switch (direction) {
                  case NORTH, SOUTH -> { // Z
                        double y = snapY(player, cursor);
                        double z = snapXZ(cursor.x);
                        
                        pos = new Vec3(
                              z,
                              y - 5/16f,
                              cursor.z
                        );
                        
                        yRot = direction.toYRot();
                  }
                  case EAST, WEST -> { // X
                        double y = snapY(player, cursor);
                        double z = snapXZ(cursor.z);
                        
                        pos = new Vec3(
                              cursor.x,
                              y - 5/16.0,
                              z
                        );
                        
                        yRot = direction.toYRot();
                  }
                  default -> {
                        if (direction == Direction.DOWN) {
                              cursor = cursor.add(0, -h, 0);
                              direction = Direction.UP;
                        }
                        
                        pos = new Vec3(
                              Mth.lerp(0.85, player.getX(), cursor.x),
                              cursor.y,
                              Mth.lerp(0.85, player.getZ(), cursor.z)
                        );
                        
                        yRot = rotation + 180;
                  }
            }
            
            Vector3f step = direction.step().mul(2/16f, 0, 2/16f);
            Vec3 stepped_pos = pos.add(step.x, step.y, step.z);
            AABB aabb;
            
            switch (direction) {
                  case NORTH, SOUTH -> {
                        aabb = new AABB(
                              stepped_pos.x - w, stepped_pos.y, stepped_pos.z + d,
                              stepped_pos.x + w, stepped_pos.y + h, stepped_pos.z - d
                        );
                  }
                  case EAST, WEST -> {
                        aabb = new AABB(
                              stepped_pos.x - d, stepped_pos.y, stepped_pos.z - w,
                              stepped_pos.x + d, stepped_pos.y + h, stepped_pos.z + w
                        );
                  }
                  default -> {
                        double width = (7 / 32.0);
                        aabb = new AABB(stepped_pos.add(width, h, width), stepped_pos.add(-width, 0, -width));
                  }
            }
            
            Vector3f inset = direction.step().mul(-1/16f, -1/16f, -1/16f);
            AABB inset_aabb = aabb.move(inset);

//                  showBoxCorners(new Vector3f(1f, 0f, 0f), 0.2f, level, inset_aabb);
            
            CollidingVertexMap map = new CollidingVertexMap(inset_aabb, direction, level, cursor);
            map.pushClippedPoints();
            map.pushHangingPoints();
            
            Vector3f offset = inset.mul(-1);
            AABB box = map.box.move(offset);
            
            showBoxCorners(new Vector3f(0f, 0f, 1f), 0.2f, level, box);
            
            if (!map.areClippedPointsStable() || !level.noBlockCollision(null, box)) {
                  map.box = box;
                  map.stabilizeHangingPoints();
                  box = map.box;
                  
                  if (!level.noBlockCollision(player, box))
                        return;
            }
            
            showBoxCorners(new Vector3f(1f, 1f, 1f), 0.4f, level, box);
      }
      
      private static void showBoxCorners(Vector3f color, float scale, Level level, AABB box) {
            DustParticleOptions green = new DustParticleOptions(color, scale);
            level.addAlwaysVisibleParticle(green, box.minX, box.minY, box.minZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.minX, box.maxY, box.minZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.maxX, box.minY, box.minZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.maxX, box.maxY, box.minZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.minX, box.minY, box.maxZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.minX, box.maxY, box.maxZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.maxX, box.minY, box.maxZ, 0, 0, 0);
            level.addAlwaysVisibleParticle(green, box.maxX, box.maxY, box.maxZ, 0, 0, 0);
      }
      
      private static double snapXZ(double clickLocation) {
            int iX = clickLocation < 0
                  ? -1
                  : 1;
            int block = (int) clickLocation;
            double vX = Math.abs(clickLocation - block);
            
            double z;
            if (vX < 0.09)
                  z = block;
            else if (vX > 0.91)
                  z = block + iX;
            else if (vX < 0.35)
                  z = block + (iX * 0.25);
            else if (vX > 0.65)
                  z = block + (iX * 0.75);
            else
                  z = block + (iX * 0.5);
            
            return z;
      }
      
      private static double snapY(LocalPlayer player, Vec3 clickLocation) {
            double i = clickLocation.y;
            i -= 1.0/16;
            double scale = 8;
            double scaled = i * scale;
            double v = i - player.getEyeY();
            
            double y;
            if (v > 0) {
                  y = (int) scaled / scale;
            } else {
                  y = Mth.ceil(scaled) / scale;
            }
            return y;
      }
      
      private static class CollidingVertexMap {
            final AABB starting_box;
            final Level level;
            final Vec3 cursor;
            Direction direction;
            AABB box;
            
            Vec3[] clipped = new Vec3[4];
            Vec3[] hanging = new Vec3[4];
            
            int pointer = 0;
            
            CollidingVertexMap(AABB box, Direction direction, Level level, Vec3 cursor) {
                  this.direction = direction;
                  this.level = level;
                  this.box = box;
                  this.starting_box = box;
                  this.cursor = cursor;
                  
                  updateCords();
            }
            
            private void updateCords() {
                  Vec3[] hang;
                  Vec3[] clip;
                  
                  switch (direction) {
                        case NORTH -> {
                              // -Z
                              hang = new Vec3[] {
                                    new Vec3(box.minX, box.maxY, box.minZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.maxX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.minZ)  // BOT LEFT
                              };
                              clip = new Vec3[] {
                                    new Vec3(box.minX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.maxZ), // TOP RIGHT
                                    new Vec3(box.maxX, box.minY, box.maxZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.maxZ)  // BOT LEFT
                              };
                        }
                        case SOUTH -> {
                              // +Z
                              clip = new Vec3[] {
                                    new Vec3(box.minX, box.maxY, box.minZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.maxX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.minZ)  // BOT LEFT
                              };
                              hang = new Vec3[] {
                                    new Vec3(box.minX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.maxZ), // TOP RIGHT
                                    new Vec3(box.maxX, box.minY, box.maxZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.maxZ)  // BOT LEFT
                              };
                        }
                        case EAST -> {
                              // +X
                              hang = new Vec3[] {
                                    new Vec3(box.maxX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.maxX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.maxX, box.minY, box.maxZ)  // BOT LEFT
                              };
                              clip = new Vec3[] {
                                    new Vec3(box.minX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.minX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.minX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.maxZ)  // BOT LEFT
                              };
                        }
                        case WEST -> {
                              // -X
                              clip = new Vec3[] {
                                    new Vec3(box.maxX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.maxX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.maxX, box.minY, box.maxZ)  // BOT LEFT
                              };
                              hang = new Vec3[] {
                                    new Vec3(box.minX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.minX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.minX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.maxZ)  // BOT LEFT
                              };
                        }
                        case UP, DOWN -> {
                              clip = new Vec3[] {
                                    new Vec3(box.maxX, box.minY, box.maxZ), // TOP LEFT
                                    new Vec3(box.maxX, box.minY, box.minZ), // TOP RIGHT
                                    new Vec3(box.minX, box.minY, box.minZ), // BOT RIGHT
                                    new Vec3(box.minX, box.minY, box.maxZ)  // BOT LEFT
                              };
                              hang = new Vec3[] {
                                    new Vec3(box.maxX, box.maxY, box.maxZ), // TOP LEFT
                                    new Vec3(box.maxX, box.maxY, box.minZ), // TOP RIGHT
                                    new Vec3(box.minX, box.maxY, box.minZ), // BOT RIGHT
                                    new Vec3(box.minX, box.maxY, box.maxZ)  // BOT LEFT
                              };
                        }
                        default -> {
                              return;
                        }
                  }
                  
                  clipped = clip;
                  hanging = hang;
            }
            
            boolean areClippedPointsStable() {
                  for (pointer = 0; pointer < 4; pointer++) {
                        Vec3 v = clipped[pointer];
                        
                        Vec3 offs = new Vec3(0.1, 0.1, 0.1);
                        Iterable<VoxelShape> blockCollisions = level.getBlockCollisions(null, new AABB(v.add(offs), v.subtract(offs)));
                        
                        boolean noCollision = true;
                        for (VoxelShape collision : blockCollisions) {
                              
                              for (AABB ab : collision.toAabbs()) {
                                    boolean minX = v.x >= ab.minX;
                                    boolean maxX = v.x <= ab.maxX;
                                    boolean minY = v.y >= ab.minY;
                                    boolean maxY = v.y <= ab.maxY;
                                    boolean minZ = v.z >= ab.minZ;
                                    boolean maxZ = v.z <= ab.maxZ;
                                    boolean contains
                                           = minX
                                          && maxX
                                          && minY
                                          && maxY
                                          && minZ
                                          && maxZ
                                          ;
                                    
                                    if (contains) {
                                          noCollision = false;
                                          break;
                                    }
                              }
                        }
                        
                        if (noCollision)
                              return false;
                  }
                  
                  return true;
            }
            
            void stabilizeHangingPoints() {
                  Vec3 size = new Vec3(7 / 32.0, 9 / 32.0, 7 / 32.0);
                  AABB aabb = new AABB(cursor.add(size), cursor.subtract(size));
                  
                  Vec3 center = aabb.getCenter();
                  Vec3 px = new Vec3(aabb.maxX, center.y, center.z);
                  Vec3 nx = new Vec3(aabb.minX, center.y, center.z);
                  Vec3 pz = new Vec3(center.x, center.y, aabb.maxZ);
                  Vec3 nz = new Vec3(center.x, center.y, aabb.minZ);
                  
                  Vec3[] face;
                  Vec3[] pair;
                  switch (direction) {
                        case NORTH -> {
                              face = new Vec3[]{nz};
                              pair = new Vec3[]{pz};
                        }
                        case SOUTH -> {
                              face = new Vec3[]{pz};
                              pair = new Vec3[]{nz};
                        }
                        case EAST -> {
                              face = new Vec3[]{px};
                              pair = new Vec3[]{nx};
                        }
                        case WEST -> {
                              face = new Vec3[]{nx};
                              pair = new Vec3[]{px};
                        }
                        default -> {
                              face = new Vec3[]{nx, nz, px, pz};
                              pair = new Vec3[]{px, pz, nx, nz};
                        }
                  }
                  
                  int index = -1;
                  for (int i = 0; i < face.length; i++) {
                        Vec3 vFace = face[i];
                        
                        BlockHitResult lineOfSightToCursor = level.clip(new ClipContext(vFace, cursor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        boolean noLineOfSight = HitResult.Type.BLOCK.equals(lineOfSightToCursor.getType());
                        
                        if (noLineOfSight)
                              continue;
                        
                        if (!doesCollide(vFace)) {
                              DustParticleOptions yellow = new DustParticleOptions(new Vector3f(1f, 1f, 0f), 0.2f);
                              level.addAlwaysVisibleParticle(yellow, vFace.x, vFace.y, vFace.z, 0, 0, 0);
                              index = i;
                              break;
                        }
                  }
                  
                  if (index != -1) {
                        int i = index;
                        do {
                              Vec3 vFace = face[i];
                              Vec3 vPair = pair[i];
                              
                              if (doesCollide(vFace))
                                    continue;
                              
                              BlockHitResult lineOfSightToCursor = level.clip(new ClipContext(vFace, cursor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              boolean hasLinOfSight = !HitResult.Type.BLOCK.equals(lineOfSightToCursor.getType());
                              
                              if (!hasLinOfSight)
                                    continue;
                              
                              BlockHitResult clip = level.clip(new ClipContext(vFace, vPair, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              if (!HitResult.Type.BLOCK.equals(clip.getType()))
                                    continue;
                              
                              Vec3 location = clip.getLocation();
                              Vec3 offset = location.subtract(vPair);
                              
                              if (!(Math.abs(offset.x) + Math.abs(offset.y) + Math.abs(offset.z) > 0.001))
                                    continue;
                              
                              for (int j = 0; j < face.length; j++) {
                                    face[j] = face[j].add(offset);
                                    pair[j] = pair[j].add(offset);
                              }
                              
                              aabb = aabb.move(offset);
                              
                        } while ((i = (i + 1) % face.length) != index);
                  }
                  
                  // =================================================================================================== TODO ALIGN VERTICALLY
                  
                  AABB start = aabb.move(0, 1, 0);
                  AABB zone = start.expandTowards(0, -1, 0);
                  Iterable<VoxelShape> iterable = level.getCollisions(null, zone);
                  double yOff = 1 + Shapes.collide(Direction.Axis.Y, start, iterable, -1);
                  aabb = aabb.move(0, yOff, 0);
                  
                  showBoxCorners(new Vector3f(1f, 0f, 0f), 0.2f, level, aabb);
                  this.box = aabb;
                  updateCords();
            }
            
            void move(Vec3 offset) {
                  box = box.move(offset);
                  updateCords();
            }
            
            void pushClippedPoints() {
                  for (pointer = 0; pointer < 4; pointer++) {
                        
                        Vec3 tl = clipped[pointer];
                        Vec3 tr = clipped[(pointer + 1) % 4];
                        Vec3 bl = clipped[(pointer + 3) % 4];
                        
                        boolean doesCollide = doesCollide(tl);
                        
                        if (doesCollide)
                              continue;
                        
                        BlockHitResult clip2 = level.clip(new ClipContext(tl, tr, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        BlockHitResult clip0 = level.clip(new ClipContext(tl, bl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        
                        Vec3 offset;
                        boolean clip2Missed = HitResult.Type.MISS.equals(clip2.getType());
                        boolean clip0Missed = HitResult.Type.MISS.equals(clip0.getType());
                        if (clip2Missed && clip0Missed)
                              continue;
                        
                        if (!clip2Missed && !clip0Missed) {
                              Vec3 zeroed2 = clip2.getLocation().subtract(tl);
                              Vec3 zeroed0 = clip0.getLocation().subtract(tl);
                              Vec3 zeroed = zeroed2.add(zeroed0);
                              
                              double absZ = Math.abs(zeroed.z);
                              double absX = Math.abs(zeroed.x);
                              
                              if (direction.getAxis().isVertical()) {
                                    if (absX < absZ) {
                                          offset = new Vec3(zeroed.x, 0, 0);
                                    }
                                    else offset = new Vec3(0, 0, zeroed.z);
                              }
                              else {
                                    if (Math.max(absZ, absX) < Math.abs(zeroed.y)) {
                                          offset = new Vec3(zeroed.x, 0, zeroed.z);
                                    }
                                    else offset = new Vec3(0, zeroed.y, 0);
                              }
                        }
                        else {
                              BlockHitResult clip = clip2Missed ? clip0 : clip2;
                              offset = clip.getLocation().subtract(tl);
                        }
                        
                        if (Math.abs(offset.x) + Math.abs(offset.y) + Math.abs(offset.z) < 0.001)
                              continue;
                        
                        move(offset);
                  }
            }
            
            private boolean doesCollide(Vec3 v) {
                  Vec3 offs = new Vec3(0.1, 0.1, 0.1);
                  Iterable<VoxelShape> blockCollisions = level.getBlockCollisions(null, new AABB(v.add(offs), v.subtract(offs)));
                  for (VoxelShape collision : blockCollisions) {
                        for (AABB ab : collision.toAabbs()) {
                              boolean contains
                                     = v.x >= ab.minX
                                    && v.x <= ab.maxX
                                    && v.y >= ab.minY
                                    && v.y <= ab.maxY
                                    && v.z >= ab.minZ
                                    && v.z <= ab.maxZ
                              ;
                              
                              if (contains) return true;
                        }
                  }
                  
                  return false;
            }
            
            void pushHangingPoints() {
                  int success = 0;
                  
                  Vec3[] list = {
                        null, null, null, null
                  };
                  
                  int count = 0;
                  for (pointer = 0; pointer < 4; pointer++) {
                        Vec3 br = hanging[(pointer + 2) % 4]; // OPPOSITE
                        if (doesCollide(br)) {
                              continue;
                        }
                        else {
                              BlockHitResult clip2 = level.clip(new ClipContext(br, cursor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              if (!clip2.getType().equals(HitResult.Type.MISS))
                                    continue;
                        }
                        
                        list[pointer] = br;
                        count++;
                  }
                  
                  if (count == 3) {
                        // HANDLE SNAPS TO SHARP-OUTER CORNERS
                        for (pointer = 0; pointer < 4; pointer++) {
                              Vec3 br = list[pointer]; // OPPOSITE
                              if (br == null)
                                    continue;
                              
                              Vec3 tl = hanging[pointer];
                              Vec3 tr = hanging[(pointer + 1) % 4];
                              Vec3 bl = hanging[(pointer + 3) % 4];
                              BlockHitResult clipR = level.clip(new ClipContext(tr, tl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              Vec3 contactR = clipR.getLocation();
                              
                              BlockHitResult clipL = level.clip(new ClipContext(bl, tl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              Vec3 contactL = clipL.getLocation();
                              
                              Vec3 offsR = tl.subtract(contactR).multiply(-1, -1, -1);
                              Vec3 offsL = tl.subtract(contactL).multiply(-1, -1, -1);
                              
                              Vec3 zeroed = offsR.add(offsL);
                              
                              
                              Vec3 offs;
                              double absZ = Math.abs(zeroed.z);
                              double absX = Math.abs(zeroed.x);
                              
                              if (direction.getAxis().isVertical()) {
                                    if (absX < absZ) {
                                          offs = new Vec3(zeroed.x, 0, 0);
                                    }
                                    else offs = new Vec3(0, 0, zeroed.z);
                              }
                              else {
                                    if (Math.max(absZ, absX) < Math.abs(zeroed.y)) {
                                          offs = new Vec3(zeroed.x, 0, zeroed.z);
                                    }
                                    else offs = new Vec3(0, zeroed.y, 0);
                              }
                              
                              move(offs);
                        }
                        
                  }
                  else {
                        for (pointer = 0; pointer < 4 && success < 2; pointer++) {
                              Vec3 br = list[pointer]; // OPPOSITE
                              if (br == null)
                                    continue;
                              
                              Vec3 tr = hanging[(pointer + 1) % 4];
                              Vec3 bl = hanging[(pointer + 3) % 4];
                              
                              int steps = 0;
                              Vec3 offset = Vec3.ZERO;
                              
                              if (doesCollide(tr)) {
                                    BlockHitResult clip2 = level.clip(new ClipContext(br, tr, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                                    Vec3 contact = clip2.getLocation();
                                    
                                    Vec3 offs = tr.subtract(contact).multiply(-1, -1, -1);
                                    offset = offset.add(offs);
                                    steps++;
                              }
                              
                              if (doesCollide(bl)) {
                                    BlockHitResult clip2 = level.clip(new ClipContext(br, bl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                                    Vec3 contact = clip2.getLocation();
                                    
                                    Vec3 offs = bl.subtract(contact).multiply(-1, -1, -1);
                                    offset = offset.add(offs);
                                    steps++;
                              }
                              
                              if (Vec3.ZERO == offset)
                                    continue;
                              
                              move(offset);
                              success += steps;
                        }
                  }
            }
      }
      
}
