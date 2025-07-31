package com.beansgalaxy.backpacks.components.equipable;

import com.beansgalaxy.backpacks.access.EquipmentSlotAccess;
import com.beansgalaxy.backpacks.components.ender.EnderTraits;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.util.OptionalEitherMapCodec;
import com.beansgalaxy.backpacks.util.PatchedComponentHolder;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class EquipableComponent {
      public static final String NAME = "equipable";

      private final EquipmentGroups slots;
      private final @Nullable EquipmentModel customModel;
      private final @Nullable ResourceLocation backpackTexture;
      private final boolean traitRemovable;
      private final @Nullable Holder<SoundEvent> equip;
      private final @Nullable Holder<SoundEvent> unequip;
      private final ArrayList<EquipmentSlot> values;

      public EquipableComponent(EquipmentGroups slots, @Nullable EquipmentModel customModel, @Nullable ResourceLocation backpackTexture, boolean traitRemovable, @Nullable Holder<SoundEvent> equip, @Nullable Holder<SoundEvent> unequip) {
            this.slots = slots;
            this.customModel = customModel;
            this.backpackTexture = backpackTexture;
            this.traitRemovable = traitRemovable;
            this.equip = equip;
            this.unequip = unequip;

            ArrayList<EquipmentSlot> list = new ArrayList<>();
            List<EquipmentSlot> values = Lists.reverse(List.of(EquipmentSlot.values()));
            for (EquipmentSlot value : values)
                  if (slots.test(value))
                        list.add(value);

            this.values = list;
      }

      public static Optional<EquipableComponent> get(PatchedComponentHolder backpack) {
            if (backpack instanceof EnderTraits)
                  return Optional.empty();

            EquipableComponent equipable = backpack.get(Traits.EQUIPABLE);
            if (equipable != null)
                  return Optional.of(equipable);

            ReferenceTrait referenceTrait = backpack.get(Traits.REFERENCE);
            if (referenceTrait != null)
                  return referenceTrait.getEquipable();

            return Optional.empty();
      }

      public static Optional<EquipableComponent> get(ItemStack stack) {
            ReferenceTrait referenceTrait = stack.get(Traits.REFERENCE);
            if (referenceTrait != null)
                  return referenceTrait.getEquipable();

            EquipableComponent equipable = stack.get(Traits.EQUIPABLE);
            return Optional.ofNullable(equipable);
      }

      public static void runIfPresent(LivingEntity player, BiConsumer<EquipableComponent, EquipmentSlot> consumer) {
            for (EquipmentSlot value : EquipmentSlot.values()) {
                  ItemStack itemStack = player.getItemBySlot(value);
                  get(itemStack).ifPresent(equipable -> {
                        if (equipable.slots.test(value)) {
                              consumer.accept(equipable, value);
                        }
                  });
            }
      }

      public static boolean canEquip(PatchedComponentHolder backpack, Slot slot) {
            if (slot instanceof EquipmentSlotAccess access) {
                  return get(backpack).map(equipable -> {
                        EquipmentSlot accessSlot = access.getSlot();
                        return equipable.slots().test(accessSlot);
                  }).orElse(false);
            }

            return false;
      }

      public static void use(Player player, InteractionHand hand, ItemStack backpack, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
            get(backpack).ifPresent(equipable -> {
                  if (equipable.traitRemovable)
                        return;

                  EquipmentSlot slot = null;
                  for (EquipmentSlot value : EquipmentSlot.values()) {
                        if (EquipmentSlot.Type.HAND.equals(value.getType()) || !equipable.slots().test(value))
                              continue;


                        ItemStack equipped = player.getItemBySlot(value);
                        if (equipped.isEmpty()) {
                              player.setItemSlot(value, backpack);
                              player.setItemInHand(hand, ItemStack.EMPTY);
                              cir.setReturnValue(InteractionResultHolder.success(backpack));
                              return;
                        }

                        if (slot != null) continue;

                        if (player.isCreative())
                              slot = value;
                        else {
                              boolean hasBinding = EnchantmentHelper.has(equipped, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
                              boolean removable = EquipableComponent.testIfPresent(equipped, EquipableComponent::traitRemovable);
                              boolean isEmpty = !Traits.testIfPresent(equipped, traits -> !traits.isEmpty(equipped));
                              if (!hasBinding && (removable || isEmpty)) {
                                    slot = value;
                              }
                        }
                  }

                  if (slot != null) {
                        ItemStack equipped = player.getItemBySlot(slot);
                        player.setItemSlot(slot, backpack);
                        player.setItemInHand(hand, equipped);
                        cir.setReturnValue(InteractionResultHolder.success(backpack));
                  }
            });
      }

      public static boolean testIfPresent(ItemStack backpack, Predicate<EquipableComponent> predicate) {
            return testIfPresent(PatchedComponentHolder.of(backpack), predicate);
      }

      public static boolean testIfPresent(PatchedComponentHolder backpack, Predicate<EquipableComponent> predicate) {
            Optional<EquipableComponent> optional = get(backpack);
            if (optional.isEmpty())
                  return false;

            EquipableComponent equipable = optional.get();
            return predicate.test(equipable);
      }

      public Optional<EquipmentModel> getModel() {
            return Optional.ofNullable(customModel);
      }

      public static final OptionalEitherMapCodec<EquipmentModel, ResourceLocation> DISPLAY =
                  new OptionalEitherMapCodec<>(
                              "equipment_model", EquipmentModel.CODEC,
                              "backpack_texture", ResourceLocation.CODEC
                  );

      public static final Codec<EquipableComponent> CODEC = RecordCodecBuilder.create(in ->
                in.group(
                            EquipmentGroups.CODEC.fieldOf("slots")
                                                 .forGetter(EquipableComponent::slots),
                            DISPLAY.forGetter(equipable -> {
                                  if (equipable.backpackTexture != null)
                                        return Optional.of(Either.right(equipable.backpackTexture));

                                  if (equipable.customModel != null)
                                        return Optional.of(Either.left(equipable.customModel));

                                  return Optional.empty();
                            }),
                            Codec.BOOL.optionalFieldOf("trait_removable", false)
                                      .forGetter(EquipableComponent::traitRemovable),
                            SoundEvent.CODEC.listOf().validate(list -> list.size() < 3
                                       ? !list.isEmpty()
                                       ? DataResult.success(list)
                                       : DataResult.error(() -> "field \"sound_event\" must contain at least 1 entry")
                                       : DataResult.error(() -> "field \"sound_event\" must contain no more than 2 entries")
                            ).optionalFieldOf("sound_event").forGetter(EquipableComponent::packageSound)
                )
                .apply(in, (slots, custom_model, trait_removable, packagedSound) -> {
                      Holder<SoundEvent> equip;
                      Holder<SoundEvent> unequip;
                      if (packagedSound.isEmpty()) {
                            equip = null;
                            unequip = null;
                      }
                      else {
                            List<Holder<SoundEvent>> holders = packagedSound.get();
                            equip = holders.get(0);
                            unequip = holders.size() == 2
                                      ? holders.get(1)
                                      : null;
                      }

                      if (custom_model.isEmpty())
                            return new EquipableComponent(slots, null, null, trait_removable, equip, unequip);
                      Either<EquipmentModel, ResourceLocation> either = custom_model.get();
                      Optional<ResourceLocation> right = either.right();
                      if (right.isPresent())
                            return new EquipableComponent(slots, null, right.get(), trait_removable, equip, unequip);

                      Optional<EquipmentModel> left = either.left();
                      if (left.isPresent())
                            return new EquipableComponent(slots, left.get(), null, trait_removable, equip, unequip);

                      return new EquipableComponent(slots, null, null, trait_removable, equip, unequip);
                })
      );

      private Optional<List<Holder<SoundEvent>>> packageSound() {
            if (equip == null)
                  return Optional.empty();

            return Optional.of(unequip == null
                         ? List.of(equip)
                         : List.of(equip, unequip)
            );
      }

      public Optional<Holder<SoundEvent>> getEquipSound() {
            return Optional.ofNullable(equip);
      }

      public Optional<Holder<SoundEvent>> getUnEquipOrFallback() {
            return Optional.ofNullable(unequip == null ? equip : unequip);
      }

      public EquipmentGroups slots() {
            return slots;
      }

      public @Nullable EquipmentModel customModel() {
            return customModel;
      }

      public @Nullable ResourceLocation backpackTexture() {
            return backpackTexture;
      }

      public boolean traitRemovable() {
            return traitRemovable;
      }

      public EquipmentSlot[] values() {
            return values.toArray(EquipmentSlot[]::new);
      }

      @Override
      public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (EquipableComponent) obj;
            return Objects.equals(this.slots, that.slots) &&
                        Objects.equals(this.customModel, that.customModel) &&
                        Objects.equals(this.backpackTexture, that.backpackTexture) &&
                        this.traitRemovable == that.traitRemovable &&
                        Objects.equals(this.equip, that.equip) &&
                        Objects.equals(this.unequip, that.unequip);
      }

      @Override
      public int hashCode() {
            return Objects.hash(slots, customModel, backpackTexture, traitRemovable, equip, unequip);
      }

      @Override
      public String toString() {
            return "EquipableComponent[" +
                        "slots=" + slots + ", " +
                        "customModel=" + customModel + ", " +
                        "backpackTexture=" + backpackTexture + ", " +
                        "traitRemovable=" + traitRemovable + ", " +
                        "sound={" + equip + ", " + unequip + "}]";
      }


      public static final StreamCodec<RegistryFriendlyByteBuf, EquipableComponent> STREAM_CODEC = new StreamCodec<>() {

            @Override
            public void encode(@NotNull RegistryFriendlyByteBuf buf, EquipableComponent component) {
                  EquipmentGroups.STREAM_CODEC.encode(buf, component.slots);
                  buf.writeBoolean(component.traitRemovable);

                  EquipmentModel model = component.customModel;
                  boolean hasModel = model != null;
                  buf.writeBoolean(hasModel);
                  if (hasModel)
                        EquipmentModel.STREAM_CODEC.encode(buf, model);

                  ResourceLocation texture = component.backpackTexture;
                  boolean hasTexture = texture != null;
                  buf.writeBoolean(hasTexture);
                  if (hasTexture)
                        buf.writeResourceLocation(texture);

                  Holder<SoundEvent> equip = component.equip;
                  boolean hasSound = equip != null;
                  buf.writeBoolean(hasSound);
                  if (hasSound)
                        SoundEvent.STREAM_CODEC.encode(buf, component.equip);

                  Holder<SoundEvent> unequip = component.unequip;
                  boolean hasUnequip = unequip != null;
                  buf.writeBoolean(hasUnequip);
                  if (hasUnequip)
                        SoundEvent.STREAM_CODEC.encode(buf, component.unequip);
            }

            @Override
            public EquipableComponent decode(RegistryFriendlyByteBuf buf) {
                  EquipmentGroups slotGroup = EquipmentGroups.STREAM_CODEC.decode(buf);
                  boolean traitRemovable = buf.readBoolean();

                  boolean hasModel = buf.readBoolean();
                  EquipmentModel model = hasModel
                                         ? EquipmentModel.STREAM_CODEC.decode(buf)
                                         : null;

                  boolean hasTexture = buf.readBoolean();
                  ResourceLocation texture = hasTexture
                                             ? buf.readResourceLocation()
                                             : null;

                  boolean hasSound = buf.readBoolean();
                  Holder<SoundEvent> sound = hasSound
                                             ? SoundEvent.STREAM_CODEC.decode(buf)
                                             : null;

                  boolean hasUnEquip = buf.readBoolean();
                  Holder<SoundEvent> unequip = hasUnEquip
                                             ? SoundEvent.STREAM_CODEC.decode(buf)
                                             : null;

                  return new EquipableComponent(slotGroup, model, texture, traitRemovable, sound, unequip);
            }
      };
}
