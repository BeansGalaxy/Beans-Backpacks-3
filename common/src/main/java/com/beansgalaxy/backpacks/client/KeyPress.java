package com.beansgalaxy.backpacks.client;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.access.PlayerAccessor;
import com.beansgalaxy.backpacks.components.UtilityComponent;
import com.beansgalaxy.backpacks.network.serverbound.BackpackUseOn;
import com.beansgalaxy.backpacks.network.serverbound.InstantKeyPress;
import com.beansgalaxy.backpacks.network.serverbound.SyncHotkey;
import com.beansgalaxy.backpacks.traits.chest.screen.MenuChestScreen;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

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

      public void tick(Minecraft minecraft, LocalPlayer player) {
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

      public boolean consumeActionUseOn(Minecraft instance, BlockHitResult hitResult) {
            doCoyoteClick = false;

            BlockPos blockPos = hitResult.getBlockPos();
            if (!instance.level.getWorldBorder().isWithinBounds(blockPos))
                  return false;

            LocalPlayer player = instance.player;
            if (INSTANCE.ACTION_KEY.isUnbound()) {
                  Vec3 movement = player.getDeltaMovement();
                  if (Math.abs(movement.x) + Math.abs(movement.z) != 0) {
                        if (pickUpThru(player))
                              return true;

                        doCoyoteClick = true;
                        return false;
                  }
            }

            return placeBackpack(player, hitResult) || pickUpThru(player);
      }

      private boolean doCoyoteClick = false;
      public boolean tryCoyoteClick(LocalPlayer player, BlockHitResult hitResult) {
            if (doCoyoteClick) {
                  doCoyoteClick = false;
                  return placeBackpack(player, hitResult);
            }

            return false;
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
            EquipmentSlot equipmentSlot;
            if (BackpackUseOn.placeBackpack(player, hitResult, EquipmentSlot.MAINHAND))
                  equipmentSlot = EquipmentSlot.MAINHAND;
            else if (BackpackUseOn.placeBackpack(player, hitResult, EquipmentSlot.BODY))
                  equipmentSlot = EquipmentSlot.BODY;
            else return false;

            BackpackUseOn.send(hitResult, equipmentSlot);
            return true;
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
