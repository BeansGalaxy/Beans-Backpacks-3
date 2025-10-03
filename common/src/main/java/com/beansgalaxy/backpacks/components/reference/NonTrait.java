package com.beansgalaxy.backpacks.components.reference;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.components.equipable.EquipmentGroups;
import com.beansgalaxy.backpacks.screen.TinyClickType;
import com.beansgalaxy.backpacks.traits.IEntityTraits;
import com.beansgalaxy.backpacks.traits.ITraitCodec;
import com.beansgalaxy.backpacks.traits.TraitComponentKind;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.traits.generic.GenericTraits;
import com.beansgalaxy.backpacks.traits.generic.MutableTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.ModSound;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class NonTrait extends GenericTraits implements ITraitCodec<NonTrait>, MutableTraits, IEntityTraits<NonTrait> {
      public static final String NAME = "non";
      public static final NonTrait INSTANCE = new NonTrait();
      public static final TraitComponentKind<NonTrait> KIND = new TraitComponentKind<>(-1, NAME, INSTANCE);

      private NonTrait() {
            super(null);
      }

      public static <T> boolean is(T trait) {
            return INSTANCE.equals(trait);
      }

      @Override
      public TraitComponentKind<NonTrait> kind() {
            return KIND;
      }

      @Override
      public String name() {
            return NAME;
      }

      @Override
      public NonTraitClient client() {
            return NonTraitClient.INSTANCE;
      }

      @Override
      public Fraction fullness(ComponentHolder holder) {
            return Fraction.ONE;
      }

      @Override
      public void stackedOnMe(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public void stackedOnOther(ComponentHolder backpack, ItemStack other, Slot slot, ClickAction click, Player player, CallbackInfoReturnable<Boolean> cir) {

      }

      @Override
      public ResourceLocation getTexture() {
            return ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "backpacks/null");
      }

      @Override
      public void tinyHotbarClick(ComponentHolder backpack, int index, TinyClickType type, InventoryMenu menu, Player sender) {

      }

      @Override
      public void tinyMenuClick(ComponentHolder backpack, int index, TinyClickType type, SlotAccess access, Player sender) {

      }

      @Override
      public boolean isEmpty(ComponentHolder holder) {
            return true;
      }

      @Override
      public EquipmentGroups slots() {
            return EquipmentGroups.BODY;
      }

      @Override
      public int size() {
            return 0;
      }

      @Override
      public MutableTraits mutable(ComponentHolder holder) {
            return this;
      }

      public static final Codec<NonTrait> CODEC = new Codec<>() {

            @Override
            public <T> DataResult<Pair<NonTrait, T>> decode(DynamicOps<T> ops, T input) {
                  return DataResult.success(Pair.of(INSTANCE, input));
            }

            @Override
            public <T> DataResult<T> encode(NonTrait input, DynamicOps<T> ops, T prefix) {
                  return DataResult.success(prefix);
            }
      };

      @Override
      public Codec<NonTrait> codec() {
            return CODEC;
      }

      public static final StreamCodec<RegistryFriendlyByteBuf, NonTrait> STREAM_CODEC = new StreamCodec<>() {
            @Override
            public NonTrait decode(RegistryFriendlyByteBuf buf) {
                  return INSTANCE;
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, NonTrait trait) {

            }
      };

      @Override
      public StreamCodec<RegistryFriendlyByteBuf, NonTrait> streamCodec() {
            return STREAM_CODEC;
      }

      @Override
      public String toString() {
            return "NonTrait{}";
      }

      @Override
      public boolean equals(Object o) {
            return o == this;
      }

      @Override
      public void push() {

      }

      @Override
      public Fraction fullness() {
            return Fraction.ONE;
      }

      @Override
      public boolean isFull() {
            return false;
      }
}
