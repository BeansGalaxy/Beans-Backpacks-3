package com.beansgalaxy.backpacks.items;

import com.beansgalaxy.backpacks.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BurlapSackEntity extends BlockEntity {

      public BurlapSackEntity(BlockPos pPos, BlockState pBlockState) {
            super(Services.PLATFORM.getBurlapSackEntityType(), pPos, pBlockState);
      }

      public void clearContents() {

      }

      public void dropAll() {
      }

      @Override
      protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
            super.saveAdditional(pTag, pRegistries);
      }

      @Override
      protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
            super.loadAdditional(pTag, pRegistries);
      }
}
