package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.access.ViewableAccessor;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public abstract class ViewableBackpack implements PatchedComponentHolder {

      @Nullable
      public static ViewableBackpack get(LivingEntity livingEntity) {
            if (livingEntity instanceof ViewableAccessor access) {
                  return access.beans_Backpacks_3$getViewable();
            }

            return null;
      }

      public static ViewableBackpack get(BackpackEntity backpack) {
            return backpack.viewable;
      }

      private final HashSet<Player> viewers = new HashSet<>();
      public void onOpen(Player player) {
            if (!player.level().isClientSide) {
                  viewers.add(player);
                  int size = viewers.size();
                  if (size == 1) {
                        setOpen(true);
                        playSound(ModSound.Type.OPEN);
                  }
            }
      }

      public void onClose(Player player) {
            viewers.remove(player);
            int size = viewers.size();
            if (size == 0) {
                  setOpen(false);
                  playSound(ModSound.Type.CLOSE);
            }
      }

      public float headPitch = 0f;
      public float lastPitch = 0;
      public float velocity = 0;
      public float lastDelta = 0;
      public int wobble = 8;

      public void updateOpen() {
            float impulse = 22f;
            float resonance = 9f;
            float height = 18f;
            float closing = 20f;

            if (isOpen()) {
                  if (headPitch == 0)
                        velocity = impulse;
                  else
                        velocity = velocity + (lastPitch * resonance + height);
            }
            else if (velocity > 0) {
                  if (headPitch == 0)
                        velocity = (velocity * 0.5f) - 0.2f;
                  else
                        velocity = 0;
            } else
                  velocity = (velocity - 0.1f) * closing;

            float resistance = 0.3f;
            velocity *= resistance;
            float newPitch = headPitch - velocity * 0.1f;

            if (newPitch > 0)
                  newPitch = 0;

//            newPitch = -2f; // HOLDS TOP OPEN FOR TEXTURING
            lastPitch = headPitch;
            headPitch = newPitch;
      }

      public float fallDistance() {
            return 0f;
      }

      @Override public <T> @Nullable T remove(DataComponentType<? extends T> type) {
            return holder().remove(type);
      }

      @Override public <T> void set(DataComponentType<? super T> type, T trait) {
            holder().set(type, trait);
      }

      @Override public <T> @Nullable T get(DataComponentType<? extends T> type) {
            return holder().get(type);
      }

      public abstract void setOpen(boolean isOpen);

      public abstract boolean isOpen();

      public abstract void playSound(ModSound.Type type);

      @NotNull
      public abstract Entity entity();

      public int getId() {
            return entity().getId();
      }

      protected abstract PatchedComponentHolder holder();

      public abstract ItemStack toStack();

      public abstract boolean shouldClose();
}
