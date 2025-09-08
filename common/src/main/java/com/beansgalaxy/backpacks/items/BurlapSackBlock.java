package com.beansgalaxy.backpacks.items;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BurlapSackBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
      public static final VoxelShape SHAPE = BurlapSackBlock.box(1, 0, 1, 15, 16, 15);
      public static final MapCodec<BurlapSackBlock> CODEC = simpleCodec(BurlapSackBlock::new);
      public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
      public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

      public BurlapSackBlock(Properties pProperties) {
            super(pProperties);

            BlockState state = this.defaultBlockState()
                        .setValue(WATERLOGGED, false)
                        .setValue(OPEN, false);

            this.registerDefaultState(state);
      }

      protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
            return state.getFluidState().isEmpty();
      }

      protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
            return 1.0F;
      }

      protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
            if (state.getValue(WATERLOGGED)) {
                  level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }

            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
      }

      protected FluidState getFluidState(BlockState state) {
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
      }

      @Nullable
      public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
      }

      protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            builder.add(WATERLOGGED);
            builder.add(OPEN);
      }

      @Override
      protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
            return SHAPE;
      }

      @Override
      protected MapCodec<? extends BaseEntityBlock> codec() {
            return CODEC;
      }

      @Override
      protected RenderShape getRenderShape(BlockState pState) {
            return RenderShape.MODEL;
      }

      @Nullable
      @Override
      public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return new BurlapSackEntity(pos, state);
      }

      @Override
      protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
            if (pState.getBlock() != pNewState.getBlock()) {
                  if (pLevel.getBlockEntity(pPos) instanceof BurlapSackEntity burlapSack) {
                        burlapSack.dropAll();
                        pLevel.updateNeighbourForOutputSignal(pPos, this);
                  }
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
      }

      @Override
      protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
            pLevel.playSound(pPlayer, pPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1f, 1f);

            Boolean isOpen = pState.getValue(OPEN);
            pLevel.setBlock(pPos, pState.setValue(OPEN, !isOpen), 3);

            return ItemInteractionResult.SUCCESS;
      }
}
