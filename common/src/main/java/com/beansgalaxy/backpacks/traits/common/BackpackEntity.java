package com.beansgalaxy.backpacks.traits.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.items.ModItems;
import com.beansgalaxy.backpacks.util.CollidingVertexMap;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;

public class BackpackEntity extends Entity {
      public static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.BOOLEAN);
      public static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.ITEM_STACK);
      public static final EntityDataAccessor<Direction> DIRECTION = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.DIRECTION);
      public static final EntityDataAccessor<Integer> WOBBLE = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.INT);
      public static final EntityDataAccessor<Integer> BREAKING = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.INT);

      public final ViewableBackpack viewable = new ViewableBackpack() {
            @Override public void setOpen(boolean isOpen) {
                  entityData.set(IS_OPEN, isOpen);
            }

            @Override public boolean isOpen() {
                  return entityData.get(IS_OPEN);
            }

            @Override public void playSound(ModSound.Type type) {
                  getTraits().sound().at(BackpackEntity.this, type);
            }

            @Override public @NotNull Entity entity() {
                  return BackpackEntity.this;
            }

            @Override protected ComponentHolder holder() {
                  return ComponentHolder.of(BackpackEntity.this);
            }

            @Override public ItemStack toStack() {
                  ItemStack stack = BackpackEntity.this.getStack();
                  stack.setEntityRepresentation(BackpackEntity.this);
                  return stack;
            }

            @Override public boolean shouldClose() {
                  return false;
            }
            
      };
      
      public InteractionResult useTraitInteraction(Player player, InteractionHand hand) {
            IEntityTraits<?> traits = getTraits();
            return traits.interact(BackpackEntity.this, player, hand);
      }

      public BackpackEntity(EntityType<?> $$0, Level $$1) {
            super($$0, $$1);
            blocksBuilding = true;
      }

      public IEntityTraits<?> getTraits() {
            ItemStack stack = entityData.get(ITEM_STACK);

            BackpackTraits traits = BackpackTraits.get(stack);
            return Objects.requireNonNullElse(traits, NonTrait.INSTANCE);

      }
      
      @Nullable
      public static BackpackEntity create(
            UseOnContext ctx,
            ItemStack backpackStack,
            BackpackTraits traits
      ) {
            Level level = ctx.getLevel();
            BlockPos blockPos = ctx.getClickedPos();
            Player player = ctx.getPlayer();
            
            Direction direction;
            Vec3 cursor;
            
            if (level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()) {
                  BlockHitResult hitResult = Constants.getPlayerPOVHitResult(level, player, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
                  direction = HitResult.Type.MISS.equals(hitResult.getType())
                        ? Direction.UP
                        : hitResult.getDirection();
                  cursor = hitResult.getLocation();
            }
            else {
                  direction = ctx.getClickedFace();
                  cursor = ctx.getClickLocation();
            }
            
            float yRot;
            Vec3 pos;
            switch (direction) {
                  case NORTH, SOUTH -> { // Z
                        double y = snapY(player.getEyeY(), cursor);
                        double z = snapXZ(cursor.x);
                        
                        pos = new Vec3(
                              z,
                              y - 5 / 16f,
                              cursor.z
                        );
                        
                        yRot = direction.toYRot();
                  }
                  case EAST, WEST -> { // X
                        double y = snapY(player.getEyeY(), cursor);
                        double z = snapXZ(cursor.z);
                        
                        pos = new Vec3(
                              cursor.x,
                              y - 5 / 16.0,
                              z
                        );
                        
                        yRot = direction.toYRot();
                  }
                  default -> {
                        if (direction == Direction.DOWN) {
                              cursor = cursor.add(0, -9 / 16.0, 0);
                              direction = Direction.UP;
                        }
                        
                        pos = new Vec3(
                              Mth.lerp(0.85, player.getX(), cursor.x),
                              cursor.y,
                              Mth.lerp(0.85, player.getZ(), cursor.z)
                        );
                        
                        yRot = player.getYHeadRot() + 180;
                  }
            }
            
            Vector3f step = direction.step().mul(2 / 16f, 0, 2 / 16f);
            Vec3 stepped_pos = pos.add(step.x, step.y, step.z);
            AABB aabb = newBoundingBox(direction, stepped_pos);
            
            Vector3f inset = direction.step().mul(-1 / 16f, -1 / 16f, -1 / 16f);
            AABB inset_aabb = aabb.move(inset);
            CollidingVertexMap map = new CollidingVertexMap(inset_aabb, direction, level, cursor);
            map.pushClippedPoints();
            map.pushHangingPoints();
            
            Vector3f offset = inset.mul(-1);
            AABB box = map.box.move(offset);
            
            if (!map.areClippedPointsStable() || !level.noBlockCollision(null, box)) {
                  map.box = box;
                  map.stabilizeHangingPoints();
                  box = map.box;
                  
                  if (!level.noBlockCollision(player, box))
                        return null;
            }
            
            return create(backpackStack, traits, level, box.getBottomCenter(), yRot, direction, player);
      }
      
      private static double snapXZ(double clickLocation) {
            int iX = clickLocation < 0
                  ? -1
                  : 1;
            int block = (int) clickLocation;
            double vX = Math.abs(clickLocation - block);
            
            double z;
            if (vX < 0.09)
                  z = block;
            else if (vX > 0.91)
                  z = block + iX;
            else if (vX < 0.35)
                  z = block + (iX * 0.25);
            else if (vX > 0.65)
                  z = block + (iX * 0.75);
            else
                  z = block + (iX * 0.5);
            
            return z;
      }
      
      private static double snapY(double targetY, Vec3 clickLocation) {
            double i = clickLocation.y;
            i -= 1.0 / 16;
            double scale = 8;
            double scaled = i * scale;
            double v = i - targetY;
            
            double y;
            if (v > 0) {
                  y = (int) scaled / scale;
            }
            else {
                  y = Mth.ceil(scaled) / scale;
            }
            return y;
      }
      
      public static void drop(Player player) {
            ItemStack backpack = player.getItemBySlot(EquipmentSlot.BODY);
            BackpackTraits traits = BackpackTraits.get(backpack);
            if (traits == null)
                  return;
            
            float angle = player.yBodyRot + 180;
            double radians = Math.toRadians(angle);
            double x = Math.sin(radians) * (4.0/16.0);
            double z = Math.cos(radians) * (4.0/16.0);
            
            BackpackEntity.create(backpack, traits, player.level(), player.position().add(x, 0.6, z), angle, Direction.UP, player);
      }

      public static @NotNull BackpackEntity create(ItemStack backpackStack, BackpackTraits traits, Level level, Vec3 pos, float yRot, Direction direction, Player player) {
            BackpackEntity backpackEntity = new BackpackEntity(CommonClass.BACKPACK_ENTITY.get(), level);
            backpackEntity.setPos(pos);
            backpackEntity.setYRot(yRot);
            backpackEntity.setDirection(direction);

            traits.onPlace(backpackEntity, player, backpackStack);
            backpackEntity.entityData.set(ITEM_STACK, backpackStack.copyWithCount(1));
            backpackEntity.entityData.set(WOBBLE, 8);
            
            Component customName = backpackStack.getCustomName();
            if (customName != null)
                  backpackEntity.setCustomName(customName);

            if (level instanceof ServerLevel) {
                  level.addFreshEntity(backpackEntity);
            }

            if (direction.getAxis().isHorizontal()) {
                  level.updateNeighbourForOutputSignal(backpackEntity.blockPosition(), Blocks.AIR);
            }

            backpackStack.shrink(1);
            return backpackEntity;
      }

      @Override
      protected AABB makeBoundingBox(Vec3 position) {
            return newBoundingBox(getDirection(), position);
      }
      
      private static AABB newBoundingBox(Direction direction, Vec3 pos) {
            double d = (4 / 32.0);
            double w = (8 / 32.0);
            double h = 9 / 16.0;
            
            AABB aabb;
            switch (direction) {
                  case NORTH, SOUTH -> {
                        aabb = new AABB(
                              pos.x - w, pos.y, pos.z + d,
                              pos.x + w, pos.y + h, pos.z - d
                        );
                  }
                  case EAST, WEST -> {
                        aabb = new AABB(
                              pos.x - d, pos.y, pos.z - w,
                              pos.x + d, pos.y + h, pos.z + w
                        );
                  }
                  default -> {
                        double width = (7 / 32.0);
                        aabb = new AABB(pos.add(width, h, width), pos.add(-width, 0, -width));
                  }
            }
            
            return aabb;
      }
      
      public ItemStack getStack() {
            return entityData.get(ITEM_STACK);
      }

      @Override
      public void tick() {
            super.tick();
            updateGravity();
            wobble();
            this.move(MoverType.SELF, this.getDeltaMovement());
      }

      @Override
      public boolean isPushedByFluid() {
            return false;
      }

      private void wobble() {
            int wobble = entityData.get(WOBBLE);
            if (wobble > 0)
                  entityData.set(WOBBLE, wobble - 1);
            else entityData.set(BREAKING, 0);
      }

      private void updateGravity() {
            boolean collides = !this.level().noCollision(this, this.getBoundingBox().inflate(0.1, -0.1, 0.1));
            this.setNoGravity(this.isNoGravity() && collides);
            if (!this.isNoGravity()) {
                  if (this.isInWater()) {
                        inWaterGravity();
                  }
                  else if (this.isInLava()) {
                        if (this.isEyeInFluid(FluidTags.LAVA) && getDeltaMovement().y < 0.1) {
                              this.setDeltaMovement(this.getDeltaMovement().add(0D, 0.02D, 0D));
                        }
                        this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
                  }
                  else if (onGround()) {
                        this.setDeltaMovement(this.getDeltaMovement().scale(0.2D));
                  }
                  else {
                        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
                        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
                  }
            }
      }

      private void inWaterGravity() {
            AABB thisBox = this.getBoundingBox();
            AABB box = new AABB(thisBox.maxX, thisBox.maxY + 6D / 16D, thisBox.maxZ, thisBox.minX, thisBox.maxY, thisBox.minZ);
            List<Entity> entityList = level().getEntities(this, box);
            if (!entityList.isEmpty()) {
                  Entity entity = entityList.get(0);
                  double velocity = this.getY() - entity.getY();
                  if (entityList.get(0) instanceof Player player) {
                        this.setDeltaMovement(0, velocity / 10, 0);
//                        if (player instanceof ServerPlayer serverPlayer)
//                              Services.REGISTRY.triggerSpecial(serverPlayer, SpecialCriterion.Special.HOP);
                  }
                  else if (velocity < -0.6)
                        inWaterBob();
                  else this.setDeltaMovement(0, velocity / 20, 0);
            } else inWaterBob();
      }

      private void inWaterBob() {
            if (this.isUnderWater()) {
                  this.setDeltaMovement(this.getDeltaMovement().scale(0.95D));
                  this.setDeltaMovement(this.getDeltaMovement().add(0D, 0.003D, 0D));
            } else if (this.isInWater() && getDeltaMovement().y < 0.01) {
                  this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
                  this.setDeltaMovement(this.getDeltaMovement().add(0D, -0.01D, 0D));
            }
      }

      @Override
      public boolean fireImmune() {
            DamageResistant resistance = get(DataComponents.DAMAGE_RESISTANT);
            if (resistance == null)
                  return false;
            
            TagKey<DamageType> resistance_type = resistance.types();
            TagKey<DamageType> fire_damage_type = DamageTypeTags.IS_FIRE;
            return Objects.equals(resistance_type, fire_damage_type);
      }

      @Override
      protected void defineSynchedData(SynchedEntityData.Builder builder) {
            builder.define(ITEM_STACK, ModItems.IRON_BACKPACK.get().getDefaultInstance());
            builder.define(DIRECTION, Direction.UP);
            builder.define(IS_OPEN, false);
            builder.define(WOBBLE, 0);
            builder.define(BREAKING, 0);
      }
      
      @Override
      protected void readAdditionalSaveData(ValueInput input) {
            input.read("as_stack", ItemStack.OPTIONAL_CODEC)
                  .ifPresent(stack -> {
                        entityData.set(ITEM_STACK, stack);
                        
                        Component customName = stack.getCustomName();
                        if (customName != null)
                              setCustomName(customName);
                  });
            
            input.read("direction", Direction.CODEC).ifPresent(this::setDirection);
            
            boolean hanging = input.getBooleanOr("hanging", getDirection().getAxis().isHorizontal());
            setNoGravity(hanging);
      }
      
      @Override
      protected void addAdditionalSaveData(ValueOutput output) {
            output.store("as_stack", ItemStack.OPTIONAL_CODEC, getStack());
            output.store("direction", Direction.CODEC, getDirection());
            output.putBoolean("hanging", isNoGravity());
      }
      
      @Override
      public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
            return new ClientboundAddEntityPacket(this, entity, getDirection().get3DDataValue());
      }

      @Override
      public void recreateFromPacket(ClientboundAddEntityPacket packet) {
            super.recreateFromPacket(packet);
            int data = packet.getData();
            Direction direction = Direction.from3DDataValue(data);
            setDirection(direction);
      }

      public void setDirection(Direction direction) {
            if (direction != null) {
                  if (direction == Direction.DOWN)
                        direction = Direction.UP;

                  entityData.set(DIRECTION, direction);
                  if (direction != Direction.UP) {
                        this.setNoGravity(true);
                        this.setYRot((float) direction.get2DDataValue() * 90);
                  }
                  this.xRotO = this.getXRot();
                  this.yRotO = this.getYRot();
                  setBoundingBox(makeBoundingBox());
            }
      }

      @Override
      public Direction getDirection() {
            return entityData.get(DIRECTION);
      }

      @Override
      public Component getName() {
            return Constants.getName(getStack());
      }

      @Override
      protected boolean repositionEntityAfterLoad() {
            return false;
      }

      @Override
      public boolean canCollideWith(@NotNull Entity that) {
            if (this.canBeCollidedWith(that))
                  return false;

            if (this.isPassengerOfSameVehicle(that))
                  return false;

            if (that instanceof BackpackEntity)
                  return true;

            if (that.position().y < this.position().y)
                  return false;

            return that.canBeCollidedWith(that) || that.isPushable();
      }

      @Override
      public boolean canBeCollidedWith(Entity that) {
            if (that instanceof LivingEntity livingEntity && !livingEntity.isAlive())
                  return false;
            
            return true;
      }

      @Nullable @Override
      public ItemStack getPickResult() {
            return getStack();
      }

      @Override
      public boolean isPickable() {
            return true;
      }

      @Override
      public boolean skipAttackInteraction(Entity attacker) {
            if (attacker instanceof ServerPlayer player) {
                  return this.hurtServer(player.level(), this.damageSources().playerAttack(player), 0.0f);
            }
            return false;
      }
      
      public int getBreaking() {
            return entityData.get(BREAKING);
      }
      
      @Override
      public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
            double height = 0.1D;
            if ((damageSource.is(DamageTypes.IN_FIRE) || damageSource.is(DamageTypes.LAVA))) {
                  if (fireImmune())
                        return false;

                  wobble(5);
                  int breakAmount = getBreaking();
                  if (breakAmount >= BACKPACK_HEALTH) {
                        breakAndDropContents(level);
                        playSound(SoundEvents.GENERIC_BURN, 1f, 0.8f);
                        return true;
                  }

                  if (breakAmount % 8 == 1)
                        playSound(SoundEvents.GENERIC_BURN, 0.7f, 1f);
                  
                  entityData.set(BREAKING, breakAmount + 1);
                  return true;
            }
            if (damageSource.is(DamageTypes.PLAYER_ATTACK) && damageSource.getDirectEntity() instanceof Player player) {
                  if (player.isCreative()) {
                        IEntityTraits<?> traits = getTraits();
                        if (!traits.isEmpty(ComponentHolder.of(this)))
                              this.spawnAtLocation(level, getStack());

                        killAndUpdate(level, false);
                  }
                  else {
                        if (getBreaking() + 10 >= BACKPACK_HEALTH)
                              breakAndDropContents(level);
                        else {
                              boolean silent = this.isSilent();
                              IEntityTraits<?> traits = getTraits();
                              traits.onDamage(this, silent, traits.sound());
                        }
                  }
                  return true;
            }

            if (damageSource.is(DamageTypes.ON_FIRE))
                  return false;
            if (damageSource.is(DamageTypes.CACTUS)) {
                  breakAndDropContents(level);
                  return true;
            }
            if (damageSource.is(DamageTypes.EXPLOSION) || damageSource.is(DamageTypes.PLAYER_EXPLOSION)) {
                  height += Math.sqrt(amount) / 20;
                  hop(height);
                  return true;
            }
            if (damageSource.is(DamageTypes.ARROW) || damageSource.is(DamageTypes.THROWN) || damageSource.is(DamageTypes.TRIDENT) || damageSource.is(DamageTypes.MOB_PROJECTILE)) {
                  hop(height);
                  return false;
            }

            hop(height);
            return true;
      }

      public static final int BACKPACK_HEALTH = 24;
      public void wobble(int amount) {
            int wobble = Math.min(entityData.get(WOBBLE) + amount, BACKPACK_HEALTH);
            entityData.set(WOBBLE, wobble);
            level().updateNeighbourForOutputSignal(blockPosition(), Blocks.AIR);
      }

      public void hop(double height) {
            if (this.isNoGravity())
                  this.setNoGravity(false);
            else
                  this.setDeltaMovement(this.getDeltaMovement().add(0.0D, height, 0.0D));
      }

      protected void breakAndDropContents(ServerLevel level) {
            boolean dropItems = level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
            IEntityTraits<?> traits = getTraits();
            traits.onBreak(this, dropItems);

            if (!this.isSilent())
                  traits.sound().at(this, ModSound.Type.BREAK);

            killAndUpdate(level, dropItems);
      }

      private void killAndUpdate(ServerLevel level, boolean dropItems) {
            BlockPos pPos = blockPosition();
            level().updateNeighbourForOutputSignal(pPos, Blocks.AIR);
            if (!this.isRemoved() && !this.level().isClientSide()) {
                  this.kill(level);
                  this.markHurt();
                  if (dropItems) {
                        ItemStack backpack = getStack();
                        this.spawnAtLocation(level, backpack);
                  }
            }
      }

      @Override
      public InteractionResult interact(Player player, InteractionHand hand) {
            BackData backData = BackData.get(player);
            if (backData.isActionKeyDown())
                  return tryEquip(player);
            
            return InteractionResult.PASS;
            
      }

      public InteractionResult tryEquip(Player player) {
            if (!getTraits().slots().test(EquipmentSlot.BODY))
                  return InteractionResult.PASS;

            ItemStack backSlot = player.getItemBySlot(EquipmentSlot.BODY);
            if (!backSlot.isEmpty())
                  return InteractionResult.FAIL;

            ItemStack stack = this.getStack();
            player.setItemSlot(EquipmentSlot.BODY, stack);
            this.getTraits().sound().at(this, ModSound.Type.EQUIP);
            
            if (level() instanceof ServerLevel serverLevel)
                  this.killAndUpdate(serverLevel, false);
            
            return InteractionResult.SUCCESS;
      }
      
      public <T> @Nullable T remove(DataComponentType<? extends T> type) {
            ItemStack stack = entityData.get(ITEM_STACK);
            T removed = stack.remove(type);
            entityData.set(ITEM_STACK, stack);
            return removed;
      }

      public <T> void set(DataComponentType<? super T> type, T data) {
            setComponent(type, data);
            
            ItemStack stack = entityData.get(ITEM_STACK);
            stack.set(type, data);
            entityData.set(ITEM_STACK, stack);
      }

      @Override
      public <T> T get(DataComponentType<? extends T> type) {
            return entityData.get(ITEM_STACK).get(type);
      }
      
      @Override
      public boolean shouldShowName() {
            return getStack().getCustomName() != null;
      }
}
