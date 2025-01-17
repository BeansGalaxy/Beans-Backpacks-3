package com.beansgalaxy.backpacks.data.config;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.container.Shorthand;
import com.beansgalaxy.backpacks.data.config.screen.IConfig;
import com.beansgalaxy.backpacks.data.config.types.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.*;

public class CommonConfig implements IConfig {
      @Deprecated(since = "0.8-beta") public HSetConfigVariant<Item> tool_belt_additions;
      public IntConfigVariant shorthand_size;
      public HSetConfigVariant<Item> shorthand_additions;
      public BoolConfigVariant tool_belt_break_items;
      public BoolConfigVariant keep_back_on_death;
      public BoolConfigVariant keep_shorthand_on_death;
      public BoolConfigVariant do_nbt_stacking;

      private final ConfigLine[] LINES = new ConfigLine[] {
                  shorthand_size = new IntConfigVariant("shorthand_size", Shorthand.SHORTHAND_DEFAU, 0, Shorthand.SHORTHAND_MAX),
                  tool_belt_additions = HSetConfigVariant.Builder.create(Constants::itemShortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in))).comment("!!DOES NOTING!! SOON TO BE REMOVED!")
                              .build("tool_belt_additions"),
                  shorthand_additions = HSetConfigVariant.Builder.create(Constants::itemShortString, in -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(in)))
                              .isValid(in -> BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(in)))
                              .build("shorthand_additions"),
                  tool_belt_break_items = new BoolConfigVariant("tool_belt_break_items", false, "Will the Tool Belt continue to use a tool until it breaks"),
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
            int clamp = Mth.clamp((int) player.getAttributeValue(attribute) + configSize, config.min, config.max);
            return clamp;
      }

}
