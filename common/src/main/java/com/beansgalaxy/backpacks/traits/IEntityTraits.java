package com.beansgalaxy.backpacks.traits;

import com.beansgalaxy.backpacks.components.equipable.EquipmentGroups;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IEntityTraits<T extends GenericTraits> {

      default InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
            return InteractionResult.PASS;
      }

      default void onBreak(BackpackEntity backpack, boolean dropItems) {

      }

      default void onDamage(BackpackEntity backpack, boolean silent, ModSound sound) {
            backpack.wobble(10);
            backpack.breakAmount += 10;
            backpack.hop(0.1);
            if (!silent) {
                  float pitch = backpack.getRandom().nextFloat() * 0.3f;
                  sound.at(backpack, ModSound.Type.HIT, 1f, pitch + 0.9f);
            }
      }

      @Nullable
      default Container createHopperContainer(BackpackEntity backpack) {
            return null;
      }

      default void onPlace(BackpackEntity entity, Player player, ItemStack stack) {

      }

      default int getAnalogOutput(BackpackEntity backpack) {
            return 0;
      }

      ResourceLocation getTexture();

      void tinyHotbarClick(ComponentHolder backpack, int index, TinyClickType type, InventoryMenu menu, Player sender);

      void tinyMenuClick(ComponentHolder backpack, int index, TinyClickType type, SlotAccess access, Player sender);

      ModSound sound();

      boolean isEmpty(ComponentHolder entity);

      EquipmentGroups slots();

      int size();
}
