package com.beansgalaxy.backpacks.client;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.PlayerAccessor;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.network.serverbound.BackpackUseOn;
import com.beansgalaxy.backpacks.network.serverbound.InstantKeyPress;
import com.beansgalaxy.backpacks.network.serverbound.SyncHotkey;
import com.beansgalaxy.backpacks.traits.chest.screen.MenuChestScreen;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.OptionalInt;

public class KeyPress {
      public static final KeyPress INSTANCE = new KeyPress();

      public static final String KEY_CATEGORY = "key.beansbackpacks.category";

      public static final String ACTION_KEY_IDENTIFIER = "key.beansbackpacks.action";
      public static final String MENUS_KEY_IDENTIFIER = "key.beansbackpacks.inventory";
      public static final String INSTANT_KEY_IDENTIFIER = "key.beansbackpacks.instant";
      public static final String ACTION_KEY_DESC = "key.beansbackpacks.desc.action";
      public static final String MENUS_KEY_DESC = "key.beansbackpacks.desc.inventory";
      public static final String INSTANT_KEY_DESC = "key.beansbackpacks.desc.instant";
      public static final String ACTION_KEY_DISABLED = "key.beansbackpacks.action_disabled";
      public static final String ACTION_KEY_DISABLED_DESC = "key.beansbackpacks.desc.action_disabled";

      public static final String SPYGLASS_KEY_IDENTIFIER = "key.beansbackpacks.spyglass";

      public final KeyMapping ACTION_KEY = new KeyMapping(
                  ACTION_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_UNKNOWN,
                  KEY_CATEGORY);

      public final KeyMapping MENUS_KEY = new KeyMapping(
                  MENUS_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_UNKNOWN,
                  KEY_CATEGORY);

      public final KeyMapping INSTANT_KEY = new KeyMapping(
                  INSTANT_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_UNKNOWN,
                  KEY_CATEGORY);

      public final KeyMapping SPYGLASS_KEY = new KeyMapping(
                  SPYGLASS_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_B,
                  KEY_CATEGORY);

      public void tick(Minecraft minecraft, LocalPlayer player, DeltaTracker delta) {
            if (coyoteClick != null) {
                  if (coyoteClick.shouldUnload(minecraft, player))
                        unloadCoyoteClick(minecraft, player, coyoteClick);
                  else if (player.isUsingItem())
                        cancelCoyoteClick();
            }

            isPressed actionKey = KeyPress.isPressed(minecraft, KeyPress.getActionKeyBind());
            boolean actionKeyPressed = actionKey.pressed() && INSTANT_KEY.isUnbound();
            isPressed menusKey = KeyPress.isPressed(minecraft, KeyPress.getMenusKeyBind());
            int tinyChestSlot = minecraft.screen instanceof MenuChestScreen screen ? screen.slotIndex() : -1;
            boolean menuKeyPressed = tinyChestSlot == -1 && menusKey.pressed();

            BackData backData = BackData.get(player);

            if (actionKeyPressed == backData.isActionKeyDown() && menuKeyPressed == backData.isMenuKeyDown() && tinyChestSlot == backData.getTinySlot())
                  return;

            backData.setActionKey(actionKeyPressed);
            backData.setMenuKey(menuKeyPressed);
            backData.setTinySlot(tinyChestSlot);
            SyncHotkey.send(actionKeyPressed, menuKeyPressed, tinyChestSlot);
      }

      private void unloadCoyoteClick(Minecraft minecraft, LocalPlayer player, CoyoteClick coyoteClick) {
            for (InteractionHand interactionhand : InteractionHand.values()) {
                  ItemStack itemstack = player.getItemInHand(interactionhand);
                  if (!itemstack.isItemEnabled(minecraft.level.enabledFeatures()))
                        break;

                  int i = itemstack.getCount();
                  InteractionResult interactionresult = minecraft.gameMode.useItemOn(player, interactionhand, coyoteClick.blockhitresult);
                  if (!interactionresult.consumesAction())
                        continue;

                  if (interactionresult.shouldSwing()) {
                        player.swing(interactionhand);
                        if (!itemstack.isEmpty() && (itemstack.getCount() != i || minecraft.gameMode.hasInfiniteItems())) {
                              minecraft.gameRenderer.itemInHandRenderer.itemUsed(interactionhand);
                        }
                  }

                  break;
            }

            cancelCoyoteClick();
      }

      public OptionalInt loadCoyoteClick(LocalPlayer player, BlockHitResult hitResult) {
            if (coyoteClick != null) {
                  if (coyoteClick.isFinished()) {
                        EquipmentSlot slot = coyoteClick.slot;
                        PlaceableComponent placeable = coyoteClick.placeable;
                        coyoteClick = null;

                        ItemStack backpack = player.getItemBySlot(slot);
                        if (BackpackUseOn.placeBackpack(player, hitResult, backpack, placeable)) {
                              BackpackUseOn.send(hitResult, slot);
                              return OptionalInt.of(8);
                        }

                        return OptionalInt.of(4);
                  }
                  else {
                        coyoteClick.indexProgress();
                        return OptionalInt.of(0);
                  }
            }

            if (pickUpThru(player)) {
                  return OptionalInt.of(0);
            }

            EquipmentSlot[] slots = {
                        EquipmentSlot.BODY,
                        EquipmentSlot.MAINHAND,
                        EquipmentSlot.OFFHAND
            };

            for (EquipmentSlot slot : slots) {
                  ItemStack backpack = player.getItemBySlot(slot);

                  Optional<PlaceableComponent> component = PlaceableComponent.get(backpack);
                  if (component.isEmpty())
                        continue;

                  PlaceableComponent placeable = component.get();
                  coyoteClick = new CoyoteClick(slot, placeable, hitResult);
                  return OptionalInt.of(0);
            }

            return OptionalInt.empty();
      }

      public void cancelCoyoteClick() {
            coyoteClick = null;
      }

      @Nullable
      private KeyPress.CoyoteClick coyoteClick = null;

      public boolean hasCoyoteClick() {
            return coyoteClick != null;
      }

      private static class CoyoteClick {
            final EquipmentSlot slot;
            final PlaceableComponent placeable;
            final BlockHitResult blockhitresult;
            int progress = 1;

            private CoyoteClick(EquipmentSlot slot, PlaceableComponent placeable, BlockHitResult blockhitresult) {
                  this.slot = slot;
                  this.placeable = placeable;
                  this.blockhitresult = blockhitresult;
            }


            void indexProgress() {
                  progress += 1;
            }

            boolean isFinished() {
                  return progress > 4;
            }

            public boolean shouldUnload(Minecraft minecraft, LocalPlayer player) {
                  if (!minecraft.options.keyUse.isDown()) {
                        return true;
                  }

                  if (minecraft.hitResult instanceof BlockHitResult hitResult) {
                        return !hitResult.getBlockPos().equals(blockhitresult.getBlockPos())
                        || !hitResult.getDirection().equals(blockhitresult.getDirection());
                  }

                  return true;
            }
      }

      public float placementProgress() {
            if (coyoteClick == null) {
                  return 0f;
            }

            return coyoteClick.progress / 4f;
      }

      public boolean pickUpThru(LocalPlayer player) {
            double pBlockInteractionRange = player.blockInteractionRange();
            double d0 = Math.max(pBlockInteractionRange, player.entityInteractionRange());
            double d1 = Mth.square(d0);
            float pPartialTick = 1F;

            Vec3 vec3 = player.getEyePosition(pPartialTick);
            Vec3 vec31 = player.getViewVector(pPartialTick);
            Vec3 vec32 = vec3.add(vec31.x * d0, vec31.y * d0, vec31.z * d0);
            AABB aabb = player.getBoundingBox().expandTowards(vec31.scale(d0)).inflate(1.0, 1.0, 1.0);
            EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
                        player, vec3, vec32, aabb, p_234237_ -> !p_234237_.isSpectator() && p_234237_.isPickable(), d1
            );

            if (entityhitresult == null || HitResult.Type.MISS.equals(entityhitresult.getType()))
                  return false;

            Vec3 vec33 = entityhitresult.getLocation();
            if (!vec33.closerThan(vec33, pBlockInteractionRange))
                  return false;

            Entity entity = entityhitresult.getEntity();

            if (!player.hasLineOfSight(entity))
                  return false;

            return tryEquip(player, entity);
      }

      public static boolean tryEquip(LocalPlayer player, Entity entity) {
            if (entity instanceof BackpackEntity backpack) {
                  InteractionResult tryEquip = backpack.tryEquip(player);
                  if (tryEquip.consumesAction())
                        InstantKeyPress.send(entity.getId());
                  return true;
            }
            else if (entity instanceof ArmorStand armorStand) {
                  InteractionResult tryEquip = CommonClass.swapBackWith(armorStand, player);
                  if (tryEquip.consumesAction())
                        InstantKeyPress.send(entity.getId());
                  return true;
            }
            else if (entity instanceof Allay allay) {
                  InteractionResult tryEquip = CommonClass.swapBackWith(allay, player);
                  if (tryEquip.consumesAction())
                        InstantKeyPress.send(entity.getId());
                  return true;
            }
            return false;
      }

      public static boolean placeBackpack(Player player, BlockHitResult hitResult) {
            return tryPlace(player, hitResult, EquipmentSlot.MAINHAND) || tryPlace(player, hitResult, EquipmentSlot.BODY);
      }

      private static boolean tryPlace(Player player, BlockHitResult hitResult, EquipmentSlot slot) {
            ItemStack backpack = player.getItemBySlot(slot);
            Optional<PlaceableComponent> component = PlaceableComponent.get(backpack);
            if (component.isEmpty())
                  return false;

            if (BackpackUseOn.placeBackpack(player, hitResult, backpack, component.get())) {
                  BackpackUseOn.send(hitResult, slot);
                  return true;
            }

            return false;
      }

      public static KeyMapping getDefaultKeyBind() {
            Minecraft instance = Minecraft.getInstance();
            return instance.options.keySprint;
      }

      public static KeyMapping getActionKeyBind() {
            KeyMapping sprintKey = getDefaultKeyBind();
            KeyMapping customKey = INSTANCE.ACTION_KEY;

            return customKey.isUnbound() ? sprintKey : customKey;
      }

      public static KeyMapping getMenusKeyBind() {
            KeyMapping sprintKey = getActionKeyBind();
            KeyMapping customKey = INSTANCE.MENUS_KEY;

            return customKey.isUnbound() ? sprintKey : customKey;
      }

      public static @NotNull isPressed isPressed(Minecraft minecraft, KeyMapping bind) {
            KeyMapping sneakKey = minecraft.options.keyShift;
            if (sneakKey.same(bind))
                  sneakKey.setDown(bind.isDown());

            InputConstants.Key key = InputConstants.getKey(bind.saveString());
            long window = minecraft.getWindow().getWindow();
            int value = key.getValue();

            boolean isMouseKey = key.getType().equals(InputConstants.Type.MOUSE);
            boolean isPressed = isMouseKey ? GLFW.glfwGetMouseButton(window, value) == 1 : InputConstants.isKeyDown(window, value);
            return new isPressed(isMouseKey, isPressed);
      }

      private boolean wasUtilityDown = false;

      public void handleUtility(LocalPlayer player, HitResult result) {
            boolean isUtilityDown = SPYGLASS_KEY.isDown();
            if (isUtilityDown != wasUtilityDown) {
                  UtilityComponent.testItems(player, (item, mute) -> {
                        if (item.is(Items.SPYGLASS)) {
                              PlayerAccessor access = (PlayerAccessor) player;
                              access.setUtilityScoped(isUtilityDown);
                              if (isUtilityDown)
                                    player.playSound(SoundEvents.SPYGLASS_USE, 1.0F, 1.0F);
                              return true;
                        }
                        return false;
                  });
            }

            wasUtilityDown = isUtilityDown;
      }

      public record isPressed(boolean onMouse, boolean pressed) {}
}
