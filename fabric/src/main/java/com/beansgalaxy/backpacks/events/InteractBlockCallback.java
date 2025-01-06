package com.beansgalaxy.backpacks.events;


import com.beansgalaxy.backpacks.container.Shorthand;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class InteractBlockCallback implements UseBlockCallback {
      @Override
      public InteractionResult interact(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
            Inventory inventory = player.getInventory();
            int slot = inventory.selected - inventory.items.size();
            if (slot >= 0) {
                  Shorthand shorthand = Shorthand.get(player);
                  if (shorthand.getTimer() != 0)
                        shorthand.resetSelected(inventory);
            }

            return InteractionResult.PASS;
      }
}
