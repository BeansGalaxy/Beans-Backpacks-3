package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.*;

public class CommonConfig implements IConfig {
      @Deprecated(since = "0.8-beta") public HSetConfigVariant<Item> tool_belt_additions;
      public IntConfigVariant shorthand_size;
      public HSetConfigVariant<Item> shorthand_additions;
      public HSetConfigVariant<ResourceLocation> extend_shorthand_by_advancement;
      public BoolConfigVariant keep_back_on_death;
      public BoolConfigVariant keep_shorthand_on_death;
      public BoolConfigVariant do_nbt_stacking;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  shorthand_size = new IntConfigVariant("shorthand_size", Shorthand.SHORTHAND_DEFAU, 0, Shorthand.SHORTHAND_MAX),
                  tool_belt_additions = HSetConfigVariant.Builder.create(Constants::shortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in))).comment("!!DOES NOTING!! SOON TO BE REMOVED!")
                              .build("tool_belt_additions"),
                  shorthand_additions = HSetConfigVariant.Builder.create(Constants::shortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in)))
                              .build("shorthand_additions"),
                  extend_shorthand_by_advancement = HSetConfigVariant.Builder.create(Constants::shortString, ResourceLocation::parse)
                              .defau(ResourceLocation.withDefaultNamespace("story/enter_the_end"), ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shorthand/level_30"))
                              .build("extend_shorthand_by_advancement"),
                  keep_back_on_death = new BoolConfigVariant("keep_back_on_death", false, "On death, the player will drop their equipment in the Back Slot"),
                  keep_shorthand_on_death = new BoolConfigVariant("keep_shorthand_on_death", false, "On death, the player will drop their equipment in the Shorthand"),
                  do_nbt_stacking = new BoolConfigVariant("do_nbt_stacking", false, "Matching items which do not stack due to differing nbt now can stack")
      };

      @Override
      public String getPath() {
            return "common";
      }

      @Override
      public Collection<ConfigLine> getLines() {
            return List.of(LINES);
      }

      public int getShorthandSize(Player player) {
            IntConfigVariant config = shorthand_size;
            Holder<Attribute> attribute = CommonClass.SHORTHAND_ATTRIBUTE;
            int configSize = config.get() - config.defau();
            int advancements = getAdvancements(player);
            int value = (int) player.getAttributeValue(attribute) + configSize + advancements;
            return Mth.clamp(value, config.min, config.max);
      }

      private int getAdvancements(Player player) {
            HashSet<ResourceLocation> locations = extend_shorthand_by_advancement.get();
            if (player instanceof ServerPlayer serverPlayer) {

                  int i = 0;
                  for (ResourceLocation location : locations) {
                        ServerAdvancementManager advancements = serverPlayer.getServer().getAdvancements();
                        if (advancements == null)
                              continue;

                        AdvancementHolder holder = advancements.get(location);
                        if (holder == null)
                              continue;

                        PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
                        if (playerAdvancements == null)
                              continue;

                        AdvancementProgress progress = playerAdvancements.getOrStartProgress(holder);
                        if (progress.isDone()) {
                              i++;
                        }
                  }

                  return i;
            }

            if (player.level().isClientSide)
                  return CommonClient.getAdvancements(locations);

            return 0;
      }

}
