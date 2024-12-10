package com.beansgalaxy.backpacks.client;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.access.BackData;
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
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
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

      public static final String SHORTHAND_KEY_IDENTIFIER = "key.beansbackpacks.shorthand";
      public static final String UTILITY_KEY_IDENTIFIER = "key.beansbackpacks.utility";

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

      public final KeyMapping SHORTHAND_KEY = new KeyMapping(
                  SHORTHAND_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_GRAVE_ACCENT,
                  KEY_CATEGORY);

      public final KeyMapping UTILITY_KEY = new KeyMapping(
                  UTILITY_KEY_IDENTIFIER,
                  GLFW.GLFW_KEY_0,
                  KEY_CATEGORY);

      public void tick(Minecraft minecraft, LocalPlayer player) {
            isPressed actionKey = KeyPress.isPressed(minecraft, KeyPress.getActionKeyBind());
            boolean actionKeyPressed = actionKey.pressed() && INSTANT_KEY.isUnbound();
            isPressed menusKey = KeyPress.isPressed(minecraft, KeyPress.getMenusKeyBind());
            int tinyChestSlot = minecraft.screen instanceof MenuChestScreen screen ? screen.slotIndex() : -1;
            boolean menuKeyPressed = tinyChestSlot == -1 && menusKey.pressed();

//            if(actionKey.pressed() && minecraft.hitResult instanceof BlockHitResult hitResult && Constants.CLIENT_CONFIG.instant_place.get())
//                  consumeActionUseOn(minecraft, hitResult);

            BackData backData = BackData.get(player);

            if (actionKeyPressed == backData.isActionKeyDown() && menuKeyPressed == backData.isMenuKeyDown() && tinyChestSlot == backData.getTinySlot())
                  return;

            backData.setActionKey(actionKeyPressed);
            backData.setMenuKey(menuKeyPressed);
            backData.setTinySlot(tinyChestSlot);
            SyncHotkey.send(actionKeyPressed, menuKeyPressed, tinyChestSlot);

//            boolean instantPlace = Constants.CLIENT_CONFIG.instant_place.get();
//            if (actionKey.pressed() && (instantPlace || actionKey.onMouse()) && minecraft.screen == null) {
//                  KeyPress.instantPlace(instance);
//            }
//            else if (menusKey.pressed() && menusKey.onMouse() && minecraft.screen instanceof ClickAccessor clickAccessor)
//                  clickAccessor.beans_Backpacks_2$instantPlace();

      }
//
//      public boolean consumeActionUse(Level level, Player player) {
//            ItemStack backStack = player.getItemBySlot(EquipmentSlot.BODY);
//            CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir =
//                        new CallbackInfoReturnable<>("backpack_action_use", true, InteractionResultHolder.pass(backStack));
//
//            Traits.runIfPresent(backStack, traits -> {
//                  traits.use(level, player, InteractionHand.MAIN_HAND, PatchedComponentHolder.of(backStack), backStack, cir);
//            });
//
//            if (cir.getReturnValue().getResult().consumesAction()) {
//                  BackpackUse.send();
//                  return true;
//            }
//
//            return false;
//      }

      public boolean consumeActionUseOn(Minecraft instance, BlockHitResult hitResult) {
            BlockPos blockPos = hitResult.getBlockPos();
            if (!instance.level.getWorldBorder().isWithinBounds(blockPos))
                  return false;

            LocalPlayer player = instance.player;
            boolean cancel = player.isSprinting() || player.isSwimming();
            if (cancel && INSTANCE.ACTION_KEY.isUnbound())
                  storeCoyoteClick(instance);
            else if (placeBackpack(player, hitResult))
                  return true;

            return pickUpThru(player);
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

      private void storeCoyoteClick(Minecraft instance) {

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

      public static Component getKeyReadable(KeyMapping keyBind) {
            String name = "tldr." + keyBind.saveString();
            if (Language.getInstance().has(name)) {
                  return Component.translatable(name);
            }

            return keyBind.getTranslatedKeyMessage();
      }

      public static Component getReadable(boolean inMenu) {
            KeyMapping keyBind = inMenu ? getMenusKeyBind() : getActionKeyBind();
            InputConstants.Key key = InputConstants.getKey(keyBind.saveString());
            Component readable = getKeyReadable(keyBind);
            if (InputConstants.Type.MOUSE.equals(key.getType())) {
                  return Component.translatable("help.beansbackpacks.mouse_button", readable);
            }
            boolean instantPlace = false;// Constants.CLIENT_CONFIG.instant_place.get();
            if (instantPlace) {
                  return Component.translatable("help.beansbackpacks.instant_place", readable);
            }
            if (inMenu)
                  return Component.translatable("help.beansbackpacks.menu_hotkey", readable);

            return Component.translatable("help.beansbackpacks.action_hotkey", readable, getKeyReadable(Minecraft.getInstance().options.keyUse));
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

      public record isPressed(boolean onMouse, boolean pressed) {}
}
