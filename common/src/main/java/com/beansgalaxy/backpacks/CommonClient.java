package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.access.MinecraftAccessor;
import com.beansgalaxy.backpacks.client.KeyPress;
import com.beansgalaxy.backpacks.components.StackableComponent;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.data.config.options.ShorthandHUD;
import com.beansgalaxy.backpacks.data.config.options.ToolBeltHUD;
import com.beansgalaxy.backpacks.network.serverbound.SyncSelectedSlot;
import com.beansgalaxy.backpacks.container.BackSlot;
import com.beansgalaxy.backpacks.container.ShortContainer;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.ItemStorageTraits;
import com.beansgalaxy.backpacks.traits.lunch_box.LunchBoxTraits;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

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

      private static final ResourceLocation BACK_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/back_slot.png");
      private static final ResourceLocation SHORTHAND_START = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/start.png");
      private static final ResourceLocation SHORTHAND_STOP = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/stop.png");
      private static final ResourceLocation SHORTHAND_END = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/end.png");
      private static final ResourceLocation SHORTHAND_SLOT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/shorthand.png");
      private static final ResourceLocation SHORTHAND_TOOL_BELT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/slots/shorthand/tool_belt.png");

      public static void renderShorthandSlots(GuiGraphics graphics, int leftPos, int topPos, int imageWidth, int imageHeight, LocalPlayer player) {
            graphics.blit(BACK_SLOT, leftPos + BackSlot.getX() - 1, topPos + BackSlot.getY() - 1, 10, 0, 0, 18, 18, 18, 18);

            Shorthand shorthand = Shorthand.get(player);
            int hX = leftPos + imageWidth;
            int hY = topPos + imageHeight - 10;
            int weaponsContainerSize = shorthand.weapons.getContainerSize();
            int toolsContainerSize = shorthand.tools.getContainerSize();
            int totalSize = weaponsContainerSize + toolsContainerSize;
            if (totalSize == 0)
                  return;

            graphics.blit(SHORTHAND_START, hX - 32, hY, 10, 0, 0, 32, 32, 32, 32);
            int i = 0;
            while (i < totalSize) {
                  ResourceLocation slot = i < weaponsContainerSize
                                    ? SHORTHAND_SLOT : SHORTHAND_TOOL_BELT;

                  graphics.blit(slot, hX - 32 - (i * 18), hY, 10, 0, 0, 32, 32, 32, 32);
                  i++;
            }
            if (totalSize == 9)
                  graphics.blit(SHORTHAND_END, leftPos, hY, 10, 0, 0, 32, 32, 32, 32);
            else
                  graphics.blit(SHORTHAND_STOP, hX - 14 - (i * 18), hY, 10, 0, 0, 32, 32, 32, 32);
      }

      private static final ResourceLocation SHORTHAND_SINGLE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_single");
      private static final ResourceLocation SHORTHAND_DOUBLE = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_double");
      private static final ResourceLocation SHORTHAND_MANY_0 = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_many_0");
      private static final ResourceLocation SHORTHAND_MANY_1 = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_many_1");
      private static final ResourceLocation SHORTHAND_SELECT = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID,"shorthand_selection");

      public static void renderShorthandHUD(Minecraft minecraft, GuiGraphics gui, DeltaTracker tickCounter, Player player) {
            if (player == null || player.isSpectator() || minecraft.options.hideGui)
                  return;

            PoseStack pose = gui.pose();
            Window window = minecraft.getWindow();
            int height = window.getGuiScaledHeight();
            int width = window.getGuiScaledWidth();
            int y = height - 1 - 18;

            Shorthand shorthand = Shorthand.get(player);
            ShortContainer weapons = shorthand.weapons;

            Inventory inventory = player.getInventory();
            int slot = inventory.selected - inventory.items.size();
            int toolsSize = shorthand.tools.getContainerSize();
            int selected = slot - toolsSize;

            HumanoidArm mainArm = player.getMainArm();
            ShorthandHUD hud = CommonClass.CLIENT_CONFIG.shorthand_hud_location.get();

            renderToolBelt(minecraft, gui, selected, slot, player, shorthand, mainArm, hud, width, y);

            int weaponsSize = weapons.getContainerSize();
            if (weaponsSize == 0)
                  return;

            int x = getShorthandHudX(mainArm, width, weaponsSize, ShorthandHUD.FAR_CORNER.equals(hud));

            RenderSystem.enableBlend();
            if (weaponsSize < 3) {
                  ItemStack weapon = weapons.getItem(0);

                  if (weaponsSize > 1) {
                        ItemStack utility = weapon;
                        weapon = weapons.getItem(1);

                        gui.blitSprite(SHORTHAND_DOUBLE, x - 7, y - 4, 44, 24);
//                        gui.blit(SHORTHAND_DOUBLE, x - 7, y - 4, 0, 0, 44, 24, 44, 24);

                        gui.renderItem(utility, x - 3, y, player.getId());
                        gui.renderItemDecorations(minecraft.font, utility, x - 3, y);

                        pose.pushPose();
                        pose.translate(0, 0, 200);
                        if (selected == 1)
                              gui.blitSprite(SHORTHAND_SELECT, x + 3, y - 4, 44, 24);
//                              gui.blit(SHORTHAND_SELECT, x + 3, y - 4, 0, 0, 44, 24, 44, 24);

                        if (selected == 0)
                              gui.blitSprite(SHORTHAND_SELECT, x - 17, y - 4, 44, 24);
//                              gui.blit(SHORTHAND_SELECT, x - 17, y - 4, 0, 0, 44, 24, 44, 24);

                        pose.popPose();

                  } else {
                        gui.blitSprite(SHORTHAND_SINGLE, x - 6, y - 4, 44, 24);
//                        gui.blit(SHORTHAND_SINGLE, x - 6, y - 4, 0, 0, 44, 24, 44, 24);

                        if (selected == 0) {
                              gui.blitSprite(SHORTHAND_SELECT, x + 3, y - 4, 44, 24);
//                              gui.blit(SHORTHAND_SELECT, x + 3, y - 4, 0, 0, 44, 24, 44, 24);
                        }
                  }

                  gui.renderItem(weapon, x + 17, y, player.getId());
                  gui.renderItemDecorations(minecraft.font, weapon, x + 17, y);
            } else {
                  gui.blitSprite(SHORTHAND_MANY_1, x - 6, y - 4, 44, 24);
//                  gui.blit(SHORTHAND_MANY_1, x - 6, y - 4, 0, 0, 44, 24, 44, 24);

                  gui.blitSprite(SHORTHAND_MANY_0, x - 6, y - 4, 44, 24);
//                  gui.blit(SHORTHAND_MANY_0, x - 6, y - 4, 0, 0, 44, 24, 44, 24);

                  int selectedWeapon = shorthand.getSelectedWeapon();
                  ItemStack stack = weapons.getItem(selectedWeapon);
                  gui.renderItem(stack, x + 8, y, player.getId());
                  gui.renderItemDecorations(minecraft.font, stack, x + 8, y);

                  boolean isSelected = selectedWeapon == selected;
                  if (isSelected)
                        gui.blitSprite(SHORTHAND_SELECT, x - 6, y - 4, 44, 24);
//                        gui.blit(SHORTHAND_SELECT, x - 6, y - 4, 0, 0, 44, 24, 44, 24);

                  int i = selectedWeapon;
                  ItemStack next = ItemStack.EMPTY;
                  do {
                        i++;
                        if (i >= weaponsSize)
                              i = 0;

                        if (i == selectedWeapon)
                              break;

                        next = weapons.getItem(i);
                  } while (next.isEmpty());

                  gui.enableScissor(x - 7, y - 3, x + (isSelected ? 4 : 6), y + 19);
                  gui.renderItem(next, x - 4, y, player.getId());
                  gui.renderItemDecorations(minecraft.font, next, x - 4, y);
                  gui.disableScissor();

                  int j = selectedWeapon;
                  ItemStack last = ItemStack.EMPTY;
                  do {
                        j--;
                        if (j < 0)
                              j = weaponsSize - 1;

                        if (j == selectedWeapon)
                              break;

                        last = weapons.getItem(j);
                  } while (last.isEmpty());

                  gui.enableScissor(x + (isSelected ? 28 : 26), y - 3, x + 39, y + 19);
                  gui.renderItem(last, x + 20, y, player.getId());
                  gui.renderItemDecorations(minecraft.font, last,  x + 20, y);
                  gui.disableScissor();
            }
            RenderSystem.disableBlend();
      }

      private static void renderToolBelt(Minecraft minecraft, GuiGraphics gui, int selected, int slot, Player player, Shorthand shorthand, HumanoidArm mainArm, ShorthandHUD hud, int width, int y) {
            ToolBeltHUD visibility = CommonClass.CLIENT_CONFIG.tool_belt_hud_visibility.get();
            if (ToolBeltHUD.HIDDEN.equals(visibility))
                  return;

            if (shorthand.tools.getSize() == 0)
                  return;

            int toolBeltSlot = -1;
            boolean toolBeltSelected = false;
            if (selected < 0 && slot > -1) {
                  toolBeltSlot = slot;
                  toolBeltSelected = true;
            }
            else {
                  if (ToolBeltHUD.SELECTED.equals(visibility))
                        return;

                  HitResult hitResult = minecraft.hitResult;
                  if (HitResult.Type.BLOCK.equals(hitResult.getType())) {
                        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                        Level level = player.level();
                        BlockPos blockPos = blockHitResult.getBlockPos();
                        BlockState blockState = level.getBlockState(blockPos);
                        float destroySpeed = blockState.getDestroySpeed(level, blockPos);
                        if (destroySpeed >= 0.1f)
                              toolBeltSlot = shorthand.getQuickestSlot(blockState);
                  }

                  int containerSize = shorthand.tools.getContainerSize();
                  if (toolBeltSlot >= containerSize)
                        return;
            }

            if (toolBeltSlot == -1)
                  return;

            RenderSystem.enableBlend();

            ItemStack tool = shorthand.tools.getItem(toolBeltSlot);
            int x = getToolBeltHudX(mainArm, hud, width);
            gui.blitSprite(SHORTHAND_SINGLE, x - 6, y - 4, 44, 24);
            gui.renderItem(tool, x + 17, y, player.getId());
            gui.renderItemDecorations(minecraft.font, tool, x + 17, y);

            if (toolBeltSelected)
                  gui.blitSprite(SHORTHAND_SELECT, x + 3, y - 4, 44, 24);

            RenderSystem.disableBlend();
      }

      private static int getToolBeltHudX(HumanoidArm mainArm, ShorthandHUD hud, int width) {
            return getShorthandHudX(mainArm, width, 1, !ShorthandHUD.FAR_CORNER.equals(hud));
      }
      private static int getShorthandHudX(HumanoidArm mainArm, int width, int weaponsSize, boolean hudIsFarCorner) {
            if (HumanoidArm.LEFT.equals(mainArm)) {
                  if (hudIsFarCorner)
                        return weaponsSize > 1 ? 8 : -12;
                  else
                        return width / 2 + (weaponsSize > 2 ? -136 : -134);
            }
            else {
                  if (hudIsFarCorner)
                        return width + (weaponsSize > 2 ? -40 : -38);
                  else
                        return width / 2 + (weaponsSize > 1 ? 104 : 84);
            }
      }

      public static void handleSendWeaponSlot(int player, int selectedSlot, ItemStack stack) {
            Minecraft minecraft = Minecraft.getInstance();
            Entity entity = minecraft.level.getEntity(player);
            if (entity instanceof Player otherPlayer) {
                  Shorthand shorthand = Shorthand.get(otherPlayer);
                  shorthand.updateSelectedWeapon(selectedSlot, stack);
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
            while (keyPress.SHORTHAND_KEY.consumeClick()) {
                  Shorthand shorthand = Shorthand.get(player);
                  Inventory inventory = player.getInventory();
                  if (keyPress.SECONDARY_KEY.isDown()) {
                        keyPress.SECONDARY_KEY.consumeClick();
                        shorthand.resetSelected(inventory);
                        continue;
                  }

                  shorthand.selectWeapon(inventory, true);
            }
            while (keyPress.SECONDARY_KEY.consumeClick()) {
                  Shorthand shorthand = Shorthand.get(player);
                  Inventory inventory = player.getInventory();
                  if (keyPress.SHORTHAND_KEY.isDown()) {
                        keyPress.SECONDARY_KEY.consumeClick();
                        shorthand.resetSelected(inventory);
                        continue;
                  }

                  shorthand.selectWeapon(inventory, false);
            }
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

      public static void handleSetSelectedSlot(int slot) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            Shorthand shorthand = Shorthand.get(player);

            if (slot < 0 || slot >= shorthand.weapons.getContainerSize())
                  return;

            Inventory inventory = player.getInventory();
            shorthand.setSlot(inventory, slot);
            minecraft.getConnection().send(new ServerboundSetCarriedItemPacket(inventory.selected));
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
