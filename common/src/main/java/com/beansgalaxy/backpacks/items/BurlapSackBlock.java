package com.beansgalaxy.backpacks.items;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
      
      @Override
      protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
            if (level.getBlockEntity(pos) instanceof AbstractBurlapSackEntity entity) {
                  Fraction weight = Traits.getWeight(entity.getItemStacks(), 16);
                  float signal = weight.floatValue() * 14f;
                  return signal == 14f ? 15 : Mth.ceil(signal);
            }
            
            return super.getAnalogOutputSignal(state, level, pos, direction);
      }
      
      @Override
      protected boolean hasAnalogOutputSignal(BlockState pState) {
            return true;
      }
      
      @Override
      protected boolean propagatesSkylightDown(BlockState state) {
            return state.getFluidState().isEmpty();
      }

      protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
            return 1.0F;
      }
      
      @Override
      protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
      ) {
            if (state.getValue(WATERLOGGED)) {
                  scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            
            return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
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
            return Services.PLATFORM.createBurlapSackEntity(pos, state);
      }
      
      @Override protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            BlockEntity blockentity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
            if (blockentity instanceof AbstractBurlapSackEntity entity) {
                  params.withDynamicDrop(
                        Constants.defaultLocation("stacks"),
                        entity::getDrops
                  );
            }
            
            return super.getDrops(state, params);
      }

      @Override
      protected InteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof AbstractBurlapSackEntity entity) {
                  boolean crouching = pPlayer.isCrouching();
                  if (!crouching && !pLevel.isClientSide()) {
                        entity.openMenu(pPlayer);
                        return InteractionResult.SUCCESS;
                  }
            }

            return InteractionResult.SUCCESS;
      }
}
