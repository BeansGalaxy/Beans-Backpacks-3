package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.ExperimentFlagAccess;
import com.beansgalaxy.backpacks.components.ender.EmptyEnderItem;
import com.beansgalaxy.backpacks.components.ender.EnderItem;
import com.beansgalaxy.backpacks.components.reference.ReferenceTrait;
import com.beansgalaxy.backpacks.platform.Services;
import com.beansgalaxy.backpacks.traits.Traits;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public enum ModItems {
      LEATHER_BACKPACK("backpack", "leather_backpack"),
      IRON_BACKPACK("iron_backpack", "iron_backpack"),
      GOLD_BACKPACK("gold_backpack", "gold_backpack"),
      NETHERITE_BACKPACK("netherite_backpack", "netherite_backpack",
                  properties -> properties.fireResistant().stacksTo(1)),
      ENDER_POUCH("ender_pouch", EnderItem::new, false),
      EMPTY_ENDER_POUCH("empty_ender_pouch", () -> new EmptyEnderItem("vanilla_bundle")),
      BUNDLE("bundle", "vanilla_bundle"),
      LUNCH_BOX("lunch_box", "lunch_box"),
      QUIVER("quiver", "quiver"),
      ALCHEMIST_BAG("alchemy_bag", "alchemy_bag"),
      NETHERITE_LUNCH_BOX("netherite_lunch_box", "netherite_lunch_box"),

// ===================================================================================================================== EXPERIMENTS

      COPPER_LEGGINGS("copper_leggings", "copper_leggings", p -> p.requiredFeatures(((ExperimentFlagAccess) new FeatureFlags()).backpacks_flag())),
      NETHERITE_BUCKET("netherite_bucket", "netherite_bucket", p -> p.requiredFeatures(((ExperimentFlagAccess) new FeatureFlags()).backpacks_flag())),
      EXPERIENCE_VIAL("experience_vial", "experience_vial", p -> p.requiredFeatures(((ExperimentFlagAccess) new FeatureFlags()).backpacks_flag())
                  .rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).stacksTo(16)),
      BULK_POUCH("bulk_pouch", "bulk_pouch", p -> p.requiredFeatures(((ExperimentFlagAccess) new FeatureFlags()).backpacks_flag())),
      ROSE_BOOTS("rose_boots", "rose_boots", p -> p.requiredFeatures(((ExperimentFlagAccess) new FeatureFlags()).backpacks_flag())),
      ;

      public static final UnaryOperator<CreativeModeTab.Builder> CREATIVE_TAB = builder -> builder
                  .title(Component.translatable("itemGroup." + Constants.MOD_ID))
                  .icon(() -> ModItems.LEATHER_BACKPACK.get().getDefaultInstance())
                  .displayItems((params, output) -> {
                        for (ModItems value : ModItems.values()) {
                              if (value.creativeIncluded)
                                    output.accept(value.get());
                        }
                        output.accept(Items.LEATHER_LEGGINGS);
                        output.accept(Items.DECORATED_POT);
                  });

      public final String id;
      public final Supplier<Item> item;
      public final boolean creativeIncluded;

      ModItems(String id, String reference) {
            this(id, reference, p -> p.stacksTo(1));
      }

      ModItems(String id, String reference, UnaryOperator<Item.Properties> properties) {
            this.id = id;
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, reference);
            ReferenceTrait referenceTrait = new ReferenceTrait(location);
            Item.Properties properties1 = new Item.Properties().component(Traits.REFERENCE, referenceTrait);
            this.item = Services.PLATFORM.register(id, () -> new Item(properties.apply(properties1)));
            this.creativeIncluded = true;
      }

      ModItems(String id, Supplier<Item> item, boolean creativeIncluded) {
            this.id = id;
            this.item = Services.PLATFORM.register(id, item);
            this.creativeIncluded = creativeIncluded;
      }

      ModItems(String id, Supplier<Item> item) {
            this(id, item, true);
      }

      public static void register() {

      }

      public Item get() {
            return item.get();
      }

      public boolean is(ItemStack stack) {
            return stack.is(get());
      }
}
