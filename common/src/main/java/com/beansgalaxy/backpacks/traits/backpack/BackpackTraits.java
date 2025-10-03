package com.beansgalaxy.backpacks.traits.backpack;

import com.beansgalaxy.backpacks.components.equipable.EquipmentGroups;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.bundle.BundleHopper;
import com.beansgalaxy.backpacks.traits.bundle.BundleScreen;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.BundleLikeTraits;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableBundleLike;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.BiConsumer;

public class BackpackTraits extends BundleLikeTraits implements IEntityTraits<BackpackTraits> {
      public static final String NAME = "backpack";
      private final EquipmentGroups slot;
      private final ResourceLocation texture;

      public BackpackTraits(ModSound sound, int size, EquipmentGroups slot, ResourceLocation texture) {
            super(sound, size);
            this.slot = slot;
            this.texture = texture;
      }

      @Nullable
      public static BackpackTraits get(ItemStack stack) {
            return Traits.get(ComponentHolder.of(stack), Traits.BACKPACK);
      }

      @Override
      public void use(Level level, Player player, InteractionHand hand, ComponentHolder holder, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            ItemStack backpack = player.getItemInHand(hand);

            EquipmentSlot slotCanidate = null;
            for (EquipmentSlot value : slots().getValues()) {
                  ItemStack equipped = player.getItemBySlot(value);

                  if (equipped.isEmpty()) {
                        player.setItemSlot(value, backpack);
                        player.setItemInHand(hand, ItemStack.EMPTY);
                        cir.setReturnValue(InteractionResultHolder.success(backpack));
                        return;
                  }

                  if (slotCanidate != null)
                        continue;

                  if (player.isCreative())
                        slotCanidate = value;
                  else {
                        boolean hasBinding = EnchantmentHelper.has(equipped, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
                        boolean isEmpty = !Traits.testIfPresent(equipped, traits -> !traits.isEmpty(equipped));
                        if (!hasBinding && isEmpty) {
                              slotCanidate = value;
                        }
                  }
            }

            if (slotCanidate != null) {
                  ItemStack equipped = player.getItemBySlot(slotCanidate);
                  player.setItemSlot(slotCanidate, backpack);
                  player.setItemInHand(hand, equipped);
                  cir.setReturnValue(InteractionResultHolder.success(backpack));
            }
      }

      public static void runIfPresent(LivingEntity entity, BiConsumer<BackpackTraits, EquipmentSlot> runnable) {
            EquipmentSlot[] values = EquipmentSlot.values();
            for (int i = values.length - 1; i >= 0; i--) {
                  EquipmentSlot slot = values[i];
                  ItemStack backpack = entity.getItemBySlot(slot);
                  BackpackTraits traits = get(backpack);
                  if (traits == null)
                        continue;

                  runnable.accept(traits, slot);
            }
      }

      public EquipmentGroups slots() {
            return slot;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public BackpackClient client() {
            return BackpackClient.INSTANCE;
      }

      @Override
      public TraitComponentKind<? extends GenericTraits> kind() {
            return Traits.BACKPACK;
      }

      @Override
      public MutableBundleLike<?> mutable(ComponentHolder holder) {
            return new BackpackMutable(this, holder);
      }

      public ResourceLocation getTexture() {
            return texture;
      }

      @Override
      public void hotkeyThrow(Slot slot, ComponentHolder backpack, int button, Player player, boolean menuKeyDown, CallbackInfo ci) {
            if (isEmpty(backpack))
                  return;

            MutableBundleLike<?> mutable = mutable(backpack);
            int selectedSlot = mutable.getSelectedSlot(player);

            ItemStack removed;
            if (menuKeyDown)
                  removed = mutable.removeItem(selectedSlot);
            else {
                  ItemStack itemStack = mutable.getItemStacks().get(selectedSlot);
                  removed = itemStack.getCount() == 1 ? mutable.removeItem(selectedSlot) : itemStack.split(1);
            }

            player.drop(removed, true);
            sound().atClient(player, ModSound.Type.REMOVE);
            mutable.push();
            ci.cancel();
      }


// ===================================================================================================================== ENTITY METHODS

      @Override
      public InteractionResult interact(BackpackEntity backpackEntity, Player player, InteractionHand hand) {
            if (player.level().isClientSide)
                  BundleScreen.openScreen(player, backpackEntity.viewable, this);

            return InteractionResult.SUCCESS;
      }

      @Override
      public void onBreak(BackpackEntity backpack, boolean dropItems) {
            List<ItemStack> stacks = backpack.get(ITraitData.ITEM_STACKS);
            if (stacks == null)
                  return;

            Level level = backpack.level();
            double x = backpack.getX();
            double y = backpack.getY();
            double z = backpack.getZ();
            if (dropItems && !level.isClientSide) for (ItemStack stack : stacks) {
                  ItemEntity itementity = new ItemEntity(level, x, y, z, stack);
                  itementity.setDefaultPickUpDelay();
                  RandomSource random = backpack.getRandom();
                  double a = random.nextDouble() - 0.5;
                  double b = a * Math.abs(a);
                  double c = random.nextDouble() - 0.5;
                  double d = c * Math.abs(c);
                  Vec3 vec3 = new Vec3(b, Math.abs(a * c) + 0.5, d).scale(0.35);
                  itementity.setDeltaMovement(vec3);
                  level.addFreshEntity(itementity);
            }

            backpack.remove(ITraitData.ITEM_STACKS);
      }

      @Override
      public Container createHopperContainer(BackpackEntity backpack) {
            return new BundleHopper(backpack, this);
      }
}
