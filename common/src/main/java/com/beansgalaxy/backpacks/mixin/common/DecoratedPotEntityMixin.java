package com.beansgalaxy.backpacks.mixin.common;

import com.beansgalaxy.backpacks.access.DecoratedPotEntityAccess;
import com.beansgalaxy.backpacks.components.BulkComponent;
import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DecoratedPotBlockEntity.class)
public abstract class DecoratedPotEntityMixin extends BlockEntity implements DecoratedPotEntityAccess, RandomizableContainer {
      @Shadow private ItemStack item;
      
      public DecoratedPotEntityMixin(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
            super(pType, pPos, pBlockState);
      }
      
      @Unique private final SlotAccess focusedItemAccess = new SlotAccess() {
            @Override public ItemStack get() {
                  return item;
            }
            
            @Override public boolean set(ItemStack stack) {
                  item = stack;
                  return true;
            }
      };
      
      @Override
      public SlotAccess item() {
            return focusedItemAccess;
      }
      
      @Unique @Nullable BulkComponent bulkContainer = null;
      
      @Override @Nullable @Unique
      public BulkComponent getBulkComponent() {
            return bulkContainer;
      }
      
      @Override
      public void setBulkComponent(@Nullable BulkComponent bulk) {
            bulkContainer = bulk;
      }
      
      @Inject(method="saveAdditional", at=@At("TAIL"))
      private void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries, CallbackInfo ci) {
            if (bulkContainer != null) {
                  RegistryOps<Tag> ops = pRegistries.createSerializationContext(NbtOps.INSTANCE);
                  pTag.put("bulk", BulkComponent.CODEC.encodeStart(ops, bulkContainer).getOrThrow());
            }
      }
      
      @Inject(method="loadAdditional", at=@At("TAIL"))
      private void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries, CallbackInfo ci) {
            Tag tag = pTag.get("bulk");
            if (tag != null) {
                  RegistryOps<Tag> ops = pRegistries.createSerializationContext(NbtOps.INSTANCE);
                  bulkContainer = BulkComponent.CODEC.parse(ops, tag).result().orElse(null);
            }
      }
      
      @Inject(method="collectImplicitComponents", at=@At("TAIL"))
      private void collectImplicitComponents(DataComponentMap.Builder pComponents, CallbackInfo ci) {
            pComponents.set(Traits.BULK, bulkContainer);
      }
      
      @Inject(method="applyImplicitComponents", at=@At("TAIL"))
      private void applyImplicitComponents(BlockEntity.DataComponentInput pComponentInput, CallbackInfo ci) {
            bulkContainer = pComponentInput.get(Traits.BULK);
      }
}
