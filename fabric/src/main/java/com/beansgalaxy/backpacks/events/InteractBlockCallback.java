package com.beansgalaxy.backpacks.events;


import com.beansgalaxy.backpacks.shorthand.storage.Shorthand;
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
            if (inventory.selected >= inventory.items.size()) {
                  Shorthand shorthand = Shorthand.get(player);
                  shorthand.resetSelected(inventory);
            }

            return InteractionResult.PASS;
      }
}