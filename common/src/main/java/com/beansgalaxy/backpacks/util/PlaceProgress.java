package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.access.BackData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public class PlaceProgress {

      float progress = 0;
      BlockPos targetBlock = null;
      Entity targetEntity = null;

      public static PlaceProgress get(Player player) {
            return BackData.get(player).getPlaceProgress();
      }

      boolean targetMatches(BlockPos target) {
            return Objects.equals(targetBlock, target);
      }

      boolean targetMatches(Entity target) {
            return Objects.equals(targetEntity, target);
      }

      float getSpeed() {
            return 0.1f;
      }

}
