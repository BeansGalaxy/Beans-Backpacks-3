package com.beansgalaxy.backpacks.traits.common;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.components.PlaceableComponent;
import com.beansgalaxy.backpacks.components.equipable.EquipableComponent;
import com.beansgalaxy.backpacks.components.reference.NonTrait;
import com.beansgalaxy.backpacks.mixin.common.AllayMixin;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ModItems;
import com.beansgalaxy.backpacks.util.ModSound;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import com.beansgalaxy.backpacks.util.data_fixers.RecoverLocalData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BackpackEntity extends Entity implements PatchedComponentHolder {
      public static final EntityDataAccessor<Boolean> IS_OPEN = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.BOOLEAN);
      public static final EntityDataAccessor<ItemStack> ITEM_STACK = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.ITEM_STACK);
      public static final EntityDataAccessor<Direction> DIRECTION = SynchedEntityData.defineId(BackpackEntity.class, EntityDataSerializers.DIRECTION);
      public static final EntityDataAccessor<PlaceableComponent> PLACEABLE = SynchedEntityData.defineId(BackpackEntity.class, new EntityDataSerializer<>() {

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, PlaceableComponent> codec() {
                  return PlaceableComponent.STREAM_CODEC;
            }

            @Override
            public PlaceableComponent copy(PlaceableComponent placeableComponent) {
                  return new PlaceableComponent(placeableComponent.customModel(), placeableComponent.backpackTexture(), placeableComponent.sound());
            }
      });

      public final ViewableBackpack viewable = new ViewableBackpack() {
            @Override public void setOpen(boolean isOpen) {
                  entityData.set(IS_OPEN, isOpen);
            }

            @Override public boolean isOpen() {
                  return entityData.get(IS_OPEN);
            }

            @Override public void playSound(ModSound.Type type) {
                  getTraits().ifPresent(traits -> traits.sound().at(BackpackEntity.this, type));
            }

            @Override public Entity entity() {
                  return BackpackEntity.this;
            }

            @Override protected PatchedComponentHolder holder() {
                  return BackpackEntity.this;
            }

            @Override public ItemStack toStack() {
                  return BackpackEntity.this.toStack();
            }

            @Override public boolean shouldClose() {
                  return false;
            }

            @Override public float fallDistance() {
                  return BackpackEntity.this.fallDistance;
            }
      };

      public int breakAmount = 0;

      public InteractionResult useTraitInteraction(Player player, InteractionHand hand) {
            return getTraits().map(traits ->
                        traits.entity().interact(BackpackEntity.this, traits, player, hand)
            ).orElse(InteractionResult.PASS);
      }

      public BackpackEntity(EntityType<?> $$0, Level $$1) {
            super($$0, $$1);
            blocksBuilding = true;
      }

      public Optional<GenericTraits> getTraits() {
            ItemStack stack = entityData.get(ITEM_STACK);
            return Traits.get(stack);
      }

      public Optional<EquipableComponent> getEquipable() {
            ItemStack stack = entityData.get(ITEM_STACK);
            return EquipableComponent.get(stack);
      }

      public PlaceableComponent getPlaceable() {
            return entityData.get(PLACEABLE);
      }

      @Nullable
      public static BackpackEntity create(
                  UseOnContext ctx,
                  ItemStack backpackStack,
                  PlaceableComponent placeable,
                  Optional<GenericTraits> traits)
      {
            Level level = ctx.getLevel();
            BlockPos blockPos = ctx.getClickedPos();
            Player player = ctx.getPlayer();

            Direction clickedFace;
            Vec3 clickLocation;
            if (level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()) {
                  BlockHitResult hitResult = Constants.getPlayerPOVHitResult(level, player, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE);
                  clickedFace = HitResult.Type.MISS.equals(hitResult.getType()) ? Direction.UP : hitResult.getDirection();
                  clickLocation = hitResult.getLocation();
            }
            else {
                  clickedFace = ctx.getClickedFace();
                  clickLocation = ctx.getClickLocation();
            }

            float rotation = ctx.getRotation();

            float yRot;
            Vec3 pos;
            switch (clickedFace) {
                  case WEST, EAST -> { // X
                        yRot = clickedFace.toYRot();
                        pos = new Vec3(
                                    clickLocation.x + clickedFace.getAxisDirection().getStep() * (2 / 16f),
                                    roundForY(clickLocation.y, player.getEyeY()) - 6/16.0,
                                    roundToScale(clickLocation.z, 2)
                        );
                  }
                  case NORTH, SOUTH -> { // Z
                        yRot = clickedFace.toYRot();
                        pos = new Vec3(
                                    roundToScale(clickLocation.x, 2),
                                    roundForY(clickLocation.y, player.getEyeY()) - 6/16.0,
                                    clickLocation.z + clickedFace.getAxisDirection().getStep() * (2 / 16f)
                        );
                  }
                  default -> {
                        pos = new Vec3(
                                    Mth.lerp(0.85, player.getX(), clickLocation.x),
                                    clickLocation.y,
                                    Mth.lerp(0.85, player.getZ(), clickLocation.z)
                        );
                        yRot = rotation + 180;
                  }
            }

            AABB aabb = BackpackEntity.newBoundingBox(clickedFace, pos);
            if (!level.noCollision(player, aabb.deflate(0.01))) {
                  switch (clickedFace) {
                        case WEST, EAST -> { // X
                              yRot = clickedFace.toYRot();
                              pos = new Vec3(
                                          clickLocation.x + clickedFace.getAxisDirection().getStep() * (2 / 16f),
                                          blockPos.getY() + 4/16f,
                                          blockPos.getZ() + 0.5
                              );
                        }
                        case NORTH, SOUTH -> { // Z
                              yRot = clickedFace.toYRot();
                              pos = new Vec3(
                                          blockPos.getX() + 0.5,
                                          blockPos.getY() + 4/16f,
                                          clickLocation.z + clickedFace.getAxisDirection().getStep() * (2 / 16f)
                              );
                        }
                        default -> {
                              Vec3 center = blockPos.getBottomCenter();
                              pos = new Vec3(
                                          center.x,
                                          clickLocation.y,
                                          center.z
                              );
                              yRot = rotation + 180;
                        }
                  }

                  aabb = BackpackEntity.newBoundingBox(clickedFace, pos);
                  if (!level.noCollision(player, aabb)) {
                        return null;
                  }
            }

            return create(backpackStack, placeable, traits, level, pos, yRot, clickedFace, player);
      }

      public static @NotNull BackpackEntity create(ItemStack backpackStack, PlaceableComponent placeable, Optional<GenericTraits> traits, Level level, Vec3 pos, float yRot, Direction direction, Player player) {
            BackpackEntity backpackEntity = new BackpackEntity(CommonClass.BACKPACK_ENTITY.get(), level);
            backpackEntity.setPos(pos);
            backpackEntity.setYRot(yRot);
            backpackEntity.setDirection(direction);

            backpackEntity.entityData.set(PLACEABLE, placeable);

            MutableTraits mute = traits.map(trait ->
                        trait.mutable(backpackEntity)
            ).orElse(NonTrait.INSTANCE);

            mute.onPlace(backpackEntity, player, backpackStack);
            backpackEntity.entityData.set(ITEM_STACK, backpackStack.copyWithCount(1));

            if (level instanceof ServerLevel) {
                  level.addFreshEntity(backpackEntity);
            }

            if (direction.getAxis().isHorizontal()) {
                  level.updateNeighbourForOutputSignal(backpackEntity.blockPosition(), Blocks.AIR);
            }

            backpackStack.shrink(1);
            return backpackEntity;
      }

      @Override @NotNull
      protected AABB makeBoundingBox() {
            return newBoundingBox(getDirection(), position());
      }

      private static AABB newBoundingBox(Direction direction, Vec3 pos) {
            double d = (4 / 32.0);
            double w = (8 / 32.0);
            double h = 9 / 16.0;

            return switch (direction) {
                  case NORTH, SOUTH -> new AABB(pos.x - w, pos.y, pos.z + d, pos.x + w, pos.y + h, pos.z - d); // -Z
                  case EAST, WEST -> new AABB(pos.x - d, pos.y, pos.z - w, pos.x + d, pos.y + h, pos.z + w); // X
                  case null, default -> {
                        double width = (7 / 32.0);
                        yield new AABB(pos.add(width, h, width), pos.add(-width, 0, -width));
                  }
            };
      }

      private static double roundForY(double i, double playerEye) {
            i -= 1.0/16;
            double scale = 8;
            double scaled = i * scale;
            double v = i - playerEye;
            if (v > 0) {
                  return 2.0/16 + (int) scaled / scale;
            } else {
                  return 2.0/16 + Mth.ceil(scaled) / scale;
            }
      }

      private static double roundToScale(double position, double scale) {
            int i = position < 0 ? -1 : 1;
            int block = (int) position;
            double v = Math.abs(position - block);
            if (v < 0.3)
                  return block;
            else if (v > 0.7)
                  return block + (i);
            else
                  return block + (i * 0.5);
      }

      public ItemStack toStack() {
            return entityData.get(ITEM_STACK);
      }

      @Override
      public void tick() {
            super.tick();
            updateGravity();
            wobble();
            this.move(MoverType.SELF, this.getDeltaMovement());
            getTraits().ifPresent(traits -> {
                  traits.entity().entityTick(this, traits);
            });
      }

      @Override
      public boolean isPushedByFluid() {
            return false;
      }

      private void wobble() {
            if (viewable.wobble > 0)
                  viewable.wobble--;
            else breakAmount = 0;
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
            List<Entity> entityList = this.getCommandSenderWorld().getEntities(this, box);
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
            return entityData.get(ITEM_STACK).has(DataComponents.FIRE_RESISTANT);
      }

      @Override
      protected void defineSynchedData(SynchedEntityData.Builder builder) {
            builder.define(ITEM_STACK, ModItems.IRON_BACKPACK.get().getDefaultInstance());
//            builder.define(TRAIT, NonTrait.INSTANCE);
            builder.define(PLACEABLE, new PlaceableComponent(null, null, ModSound.HARD));
            builder.define(DIRECTION, Direction.UP);
            builder.define(IS_OPEN, false);
      }

      @Override
      protected void readAdditionalSaveData(CompoundTag tag) {
            if (tag.contains("local_data")) {
                  RecoverLocalData.readEntity(this, tag);
                  return;
            }

            RegistryOps<Tag> serializationContext = registryAccess().createSerializationContext(NbtOps.INSTANCE);
            ItemStack stack = ItemStack.OPTIONAL_CODEC.parse(serializationContext, tag.get("as_stack")).getOrThrow();
            entityData.set(ITEM_STACK, stack);

            Direction direction = Direction.CODEC.parse(serializationContext, tag.get("direction")).getOrThrow();
            setDirection(direction);

            PlaceableComponent.get(stack).ifPresent(placeable ->
                  entityData.set(PLACEABLE, placeable)
            );
      }

      @Override
      protected void addAdditionalSaveData(CompoundTag tag) {
            ItemStack stack = toStack();
            RegistryOps<Tag> serializationContext = registryAccess().createSerializationContext(NbtOps.INSTANCE);
            tag.put("as_stack", ItemStack.OPTIONAL_CODEC.encodeStart(serializationContext, stack).getOrThrow());
            Direction direction = getDirection();
            tag.put("direction", Direction.CODEC.encodeStart(serializationContext, direction).getOrThrow());
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
            return Constants.getName(toStack());
      }

      @Override
      protected boolean repositionEntityAfterLoad() {
            return false;
      }

      @Override
      public boolean canCollideWith(@NotNull Entity that) {
            if (that instanceof LivingEntity livingEntity && !livingEntity.isAlive())
                  return false;

            if (this.isPassengerOfSameVehicle(that))
                  return false;

            if (that instanceof BackpackEntity)
                  return true;

            if (that.position().y < this.position().y)
                  return false;

            return that.canBeCollidedWith() || that.isPushable();
      }

      @Override
      public boolean canBeCollidedWith() {
            return true;
      }

      @Nullable @Override
      public ItemStack getPickResult() {
            return toStack();
      }

      @Override
      public boolean isPickable() {
            return true;
      }

      @Override
      public boolean skipAttackInteraction(Entity attacker) {
            if (attacker instanceof Player player) {
                  return this.hurt(this.damageSources().playerAttack(player), 0.0f);
            }
            return false;
      }

      @Override
      public boolean hurt(DamageSource damageSource, float amount) {
            double height = 0.1D;
            if ((damageSource.is(DamageTypes.IN_FIRE) || damageSource.is(DamageTypes.LAVA))) {
                  if (fireImmune())
                        return false;

                  wobble(5);
                  breakAmount += 1;
                  if (breakAmount >= BACKPACK_HEALTH)
                        breakAndDropContents();

                  if ((breakAmount + 10) % 11 == 0)
                        playSound(SoundEvents.GENERIC_BURN, 0.8f, 1f);

                  return true;
            }
            if (damageSource.is(DamageTypes.PLAYER_ATTACK) && damageSource.getDirectEntity() instanceof Player player) {
                  if (player.isCreative()) {
                        getTraits().ifPresent(traits -> {
                              if (!traits.isEmpty(this))
                                    this.spawnAtLocation(toStack());
                        });

                        killAndUpdate(false);
                  }
                  else {
                        if (breakAmount + 10 >= BACKPACK_HEALTH)
                              breakAndDropContents();
                        else {
                              boolean silent = this.isSilent();
                              getTraits().ifPresentOrElse(traits -> {
                                    traits.entity().onDamage(this, traits, silent, getPlaceable().sound());
                              }, () -> {
                                    wobble(10);
                                    breakAmount += 10;
                                    hop(0.1);
                                    if (!silent) {
                                          float pitch = random.nextFloat() * 0.3f;
                                          ModSound sound = getPlaceable().sound();
                                          sound.at(this, ModSound.Type.HIT, 1f, pitch + 0.9f);
                                    }
                              });
                        }
                  }
                  return true;
            }

            if (damageSource.is(DamageTypes.ON_FIRE))
                  return false;
            if (damageSource.is(DamageTypes.CACTUS)) {
                  breakAndDropContents();
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
            viewable.wobble = Math.min(viewable.wobble + amount, BACKPACK_HEALTH);
            level().updateNeighbourForOutputSignal(blockPosition(), Blocks.AIR);
      }

      public void hop(double height) {
            if (this.isNoGravity())
                  this.setNoGravity(false);
            else
                  this.setDeltaMovement(this.getDeltaMovement().add(0.0D, height, 0.0D));
      }

      protected void breakAndDropContents() {
            boolean dropItems = level().getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS);
            getTraits().ifPresent(traits -> {
                  traits.entity().onBreak(this, traits, dropItems);
            });

            ModSound modSound = getPlaceable().sound();
            if (!this.isSilent())
                  modSound.at(this, ModSound.Type.BREAK);

            killAndUpdate(dropItems);
      }

      private void killAndUpdate(boolean dropItems) {
            BlockPos pPos = blockPosition();
            level().updateNeighbourForOutputSignal(pPos, Blocks.AIR);
            if (!this.isRemoved() && !this.level().isClientSide()) {
                  this.kill();
                  this.markHurt();
                  if (dropItems) {
                        ItemStack backpack = toStack();
                        this.spawnAtLocation(backpack);
                  }
            }
      }

      @Override
      public InteractionResult interact(Player player, InteractionHand hand) {
            BackData backData = BackData.get(player);
            if (backData.isActionKeyDown()) {
                  InteractionResult tryEquip = tryEquip(player);
                  if (!tryEquip.equals(InteractionResult.PASS))
                        return tryEquip;
            }
            return useTraitInteraction(player, hand);
      }

      public InteractionResult tryEquip(Player player) {
            Optional<EquipableComponent> optional = EquipableComponent.get(this);
            if (optional.isEmpty()) {
                  this.getTraits().ifPresent(traits ->
                              traits.entity().onPickup(this, traits, player));

                  tryInsertInventory(player);
                  getPlaceable().sound().at(this, ModSound.Type.EQUIP);
                  this.killAndUpdate(false);
                  return InteractionResult.SUCCESS;
            }

            EquipableComponent equipable = optional.get();
            if (!equipable.slots().test(EquipmentSlot.BODY))
                  return InteractionResult.PASS;

            ItemStack backSlot = player.getItemBySlot(EquipmentSlot.BODY);
            if (!backSlot.isEmpty())
                  return InteractionResult.FAIL;

            this.getTraits().ifPresent(traits ->
                        traits.entity().onPickup(this, traits, player)
            );

            ItemStack stack = this.toStack();
            player.setItemSlot(EquipmentSlot.BODY, stack);
            getPlaceable().sound().at(this, ModSound.Type.EQUIP);
            this.killAndUpdate(false);
            return InteractionResult.SUCCESS;
      }

      private void tryInsertInventory(Player player) {
            ItemStack stack = this.toStack();
            ItemStack handStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
            if (handStack.isEmpty()) {
                  player.setItemSlot(EquipmentSlot.MAINHAND, stack);
                  return;
            }

            Inventory inventory = player.getInventory();
            NonNullList<ItemStack> items = inventory.items;

            for (int i = 0; i < 9; i++) {
                  ItemStack hotbarStack = items.get(0);
                  if (hotbarStack.isEmpty()) {
                        inventory.setItem(i, stack);
                        inventory.selected = i;
                        return;
                  }
            }

            if (!inventory.add(-1, stack))
                  this.spawnAtLocation(stack);
      }

      @Override
      public <T> @Nullable T remove(DataComponentType<? extends T> type) {
            ItemStack stack = entityData.get(ITEM_STACK);
            T removed = stack.remove(type);
            entityData.set(ITEM_STACK, stack);
            return removed;
      }

      @Override
      public <T> void set(DataComponentType<? super T> type, T data) {
            ItemStack stack = entityData.get(ITEM_STACK);
            stack.set(type, data);
            entityData.set(ITEM_STACK, stack);
      }

      @Override
      public <T> T get(DataComponentType<? extends T> type) {
            return entityData.get(ITEM_STACK).get(type);
      }

      @Override
      public void setChanged() {
            ItemStack stack = toStack();
            entityData.set(BackpackEntity.ITEM_STACK, stack, true);
            BlockPos pPos = blockPosition();
            level().updateNeighbourForOutputSignal(pPos, Blocks.AIR);
      }

}
