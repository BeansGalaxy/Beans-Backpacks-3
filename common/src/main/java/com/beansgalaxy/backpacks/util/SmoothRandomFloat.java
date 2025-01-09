package com.beansgalaxy.backpacks.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class SmoothRandomFloat {
      private int timeTillRandom = 0;
      private float randomFloat = 1f;
      private float direction = 1f;
      private float velocity = 0f;

      public float getDirection(RandomSource random, int fps) {
            if (timeTillRandom == 0) {
                  float f = random.nextFloat();
                  boolean b = random.nextBoolean();
                  float v1 = f * f;
                  int v2 = b ? 1 : -1;
                  float v3 = v1 * v2 * 0.5f;
                  float v = v3 + 0.5f;


                  randomFloat = (v * 0.6f) + 0.2f;
                  float t = fps * (1 - v);
                  timeTillRandom = random.nextInt(Mth.ceil(t) + fps + fps) + fps;
            } else timeTillRandom--;

            float c = 0.7f;
            float v = (randomFloat - direction) / 2;
            float delta = 1f / fps;
            float v2 = velocity + (v * delta);
            velocity = Mth.clamp(v2, -c, c) * c;

            float d = direction + velocity;
            direction = d > 1 ? 1 : d < 0 ? 0 : d;

            return direction;
      }
}
