package com.beansgalaxy.backpacks;

import com.beansgalaxy.backpacks.items.AbstractBurlapSackEntity;
import com.beansgalaxy.backpacks.screen.BurlapSackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BurlapSackEntity extends AbstractBurlapSackEntity implements MenuProvider {
      public BurlapSackEntity(BlockPos pPos, BlockState pBlockState) {
            super(pPos, pBlockState);
      }

      @Override
      public void openMenu(Player player) {
            player.openMenu(this, buf -> BlockPos.STREAM_CODEC.encode(buf, this.worldPosition));
      }

      @Override
      public Component getDisplayName() {
            return NAME;
      }

      @Nullable @Override
      public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
            return new BurlapSackMenu(pContainerId, pPlayerInventory, this);
      }
}
