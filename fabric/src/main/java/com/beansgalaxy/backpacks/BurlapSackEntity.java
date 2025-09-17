package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BurlapSackEntity extends AbstractBurlapSackEntity implements ExtendedScreenHandlerFactory<BlockPos> {
      public BurlapSackEntity(BlockPos pPos, BlockState pBlockState) {
            super(pPos, pBlockState);
      }

      @Override
      public void openMenu(Player player) {
            player.openMenu(this);
      }

      @Override
      public BlockPos getScreenOpeningData(ServerPlayer player) {
            return this.worldPosition;
      }

      @Override @NotNull
      public Component getDisplayName() {
            return NAME;
      }

      @Nullable @Override
      public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
            return new BurlapSackMenu(pContainerId, pPlayerInventory, this);
      }
}
