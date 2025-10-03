package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.CommonClient;
import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.platform.Services;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public enum ModSound implements StringRepresentable {
      HARD(0),
      SOFT(1),
      VWOOMP(2),
      CRUNCH(3),
      CLAY(4);

      private final byte i;
      ModSound(int i) {
            this.i = (byte) i;
      }

      public SoundEvent get(Type type) {
            return get(this, type);
      }

      public static SoundEvent get(ModSound sound, Type type) {
            switch (sound) {
                  case SOFT -> {
                        switch (type) {
                              case PLACE -> {
                                    return Events.LEATHER_PLACE.get();
                              }
                              case EQUIP -> {
                                    return Events.LEATHER_EQUIP.get();
                              }
                              case HIT -> {
                                    return Events.LEATHER_HIT.get();
                              }
                              case BREAK -> {
                                    return Events.LEATHER_BREAK.get();
                              }
                              case INSERT -> {
                                    return Events.LEATHER_INSERT.get();
                              }
                              case REMOVE -> {
                                    return Events.LEATHER_TAKE.get();
                              }
                              case OPEN -> {
                                    return Events.LEATHER_OPEN.get();
                              }
                              case CLOSE -> {
                                    return Events.LEATHER_CLOSE.get();
                              }
                        }
                  }
                  case HARD -> {
                        switch (type) {
                              case PLACE -> {
                                    return Events.METAL_PLACE.get();
                              }
                              case EQUIP -> {
                                    return Events.METAL_EQUIP.get();
                              }
                              case HIT -> {
                                    return Events.METAL_HIT.get();
                              }
                              case BREAK -> {
                                    return Events.METAL_BREAK.get();
                              }
                              case INSERT -> {
                                    return Events.METAL_INSERT.get();
                              }
                              case REMOVE -> {
                                    return Events.METAL_TAKE.get();
                              }
                              case OPEN -> {
                                    return Events.METAL_OPEN.get();
                              }
                              case CLOSE -> {
                                    return Events.METAL_CLOSE.get();
                              }
                        }
                  }
                  case VWOOMP -> {
                        switch (type) {
                              case PLACE -> {
                                    return Events.ENDER_PLACE.get();
                              }
                              case EQUIP -> {
                                    return Events.ENDER_EQUIP.get();
                              }
                              case HIT -> {
                                    return Events.ENDER_HIT.get();
                              }
                              case BREAK -> {
                                    return Events.ENDER_BREAK.get();
                              }
                              case OPEN -> {
                                    return Events.ENDER_OPEN.get();
                              }
                              case CLOSE -> {
                                    return Events.ENDER_CLOSE.get();
                              }
                              case REMOVE -> {
                                    return Events.ENDER_TAKE.get();
                              }
                              case INSERT -> {
                                    return Events.ENDER_INSERT.get();
                              }
                        }
                  }
                  case CRUNCH -> {
                        switch (type) {
                              case PLACE -> {
                                    return Events.WINGED_PLACE.get();
                              }
                              case EQUIP -> {
                                    return Events.WINGED_EQUIP.get();
                              }
                              case HIT -> {
                                    return Events.WINGED_HIT.get();
                              }
                              case BREAK -> {
                                    return Events.WINGED_BREAK.get();
                              }
                              case INSERT -> {
                                    return Events.LEATHER_INSERT.get();
                              }
                              case REMOVE -> {
                                    return Events.LEATHER_TAKE.get();
                              }
                              case OPEN -> {
                                    return Events.WINGED_OPEN.get();
                              }
                              case CLOSE -> {
                                    return Events.WINGED_CLOSE.get();
                              }
                        }
                  }
                  case CLAY -> {
                        switch (type) {
                              case HIT -> {
                                    return Events.POT_HIT.get();
                              }
                              case INSERT -> {
                                    return Events.POT_INSERT.get();
                              }
                              case REMOVE -> {
                                    return Events.POT_TAKE.get();
                              }
                        }
                  }
            }
            return type.defau;
      }

      @Override
      public String getSerializedName() {
            return name().toLowerCase();
      }

      public void at(Entity entity, Type type, float volume, float pitch) {
            entity.playSound(get(type), volume, pitch);
      }

      public void at(Entity entity, Type type) {
            at(entity, type, 1, 1);
      }

      public void atClient(Player player, Type type, float volume, float pitch) {
            if (player.level().isClientSide) {
                  CommonClient.playSound(get(type), volume, pitch);
            }
      }

      public void toClient(Player player, Type type, float volume, float pitch) {
            if (!player.level().isClientSide) {
                  player.playNotifySound(get(type), SoundSource.PLAYERS, volume, pitch);
            }
      }

      public void atClient(Player player, Type type) {
            atClient(player, type, 1f, 1f);
      }

      public enum Type {
            EQUIP(SoundEvents.ARMOR_EQUIP_ELYTRA.value()),
            PLACE(SoundEvents.ITEM_FRAME_PLACE),
            HIT(SoundEvents.PLAYER_ATTACK_WEAK),
            BREAK(SoundEvents.PLAYER_ATTACK_CRIT),
            INSERT(SoundEvents.BUNDLE_INSERT),
            REMOVE(SoundEvents.BUNDLE_REMOVE_ONE),
            OPEN(SoundEvents.CHEST_OPEN),
            CLOSE(SoundEvents.CHEST_CLOSE);

            private final SoundEvent defau;
            Type(SoundEvent defau) {
                  this.defau = defau;
            }
      }

      public enum Events {
            LEATHER_PLACE  ("leather_place"),
            LEATHER_EQUIP  ("leather_equip"),
            LEATHER_HIT    ("leather_hit"),
            LEATHER_BREAK  ("leather_break"),
            LEATHER_INSERT ("leather_insert"),
            LEATHER_TAKE ("leather_take"),
            LEATHER_OPEN   ("leather_open"),
            LEATHER_CLOSE  ("leather_close"),
            METAL_PLACE    ("metal_place"),
            METAL_EQUIP    ("metal_equip"),
            METAL_HIT      ("metal_hit"),
            METAL_BREAK    ("metal_break"),
            METAL_INSERT   ("metal_insert"),
            METAL_TAKE     ("metal_take"),
            METAL_OPEN     ("metal_open"),
            METAL_CLOSE    ("metal_close"),
            ENDER_PLACE    ("ender_place"),
            ENDER_EQUIP    ("ender_equip"),
            ENDER_HIT      ("ender_hit"),
            ENDER_BREAK    ("ender_break"),
            ENDER_INSERT   ("ender_insert"),
            ENDER_TAKE     ("ender_take"),
            ENDER_OPEN     ("ender_open"),
            ENDER_CLOSE    ("ender_close"),
            WINGED_PLACE   ("winged_place"),
            WINGED_EQUIP   ("winged_equip"),
            WINGED_HIT     ("winged_hit"),
            WINGED_BREAK   ("winged_break"),
            WINGED_OPEN    ("winged_open"),
            WINGED_CLOSE   ("winged_close"),
            POT_HIT        ("pot_hit"),
            POT_INSERT     ("pot_insert"),
            POT_TAKE       ("pot_take"),
            LOCK           ("lock_backpack"),
            UNLOCK         ("unlock_backpack");

            public final SoundEvent event;
            Events(String id) {
                  ResourceLocation location = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, id);
                  SoundEvent event = SoundEvent.createVariableRangeEvent(location);
                  this.event = Services.PLATFORM.register(id, event);
            }

            public SoundEvent get() {
                  return event;
            }

            private Playable playable(float volume, float pitch) {
                  return new Playable(get(), volume, pitch);
            }

            public record Playable(SoundEvent event, float volume, float pitch) {}

            public static void register() {

            }
      }

      public static Codec<ModSound> CODEC = new StringRepresentableCodec<>(ModSound.values(), in -> {
            for (ModSound value : ModSound.values()) {
                  if (value.getSerializedName().equals(in)) {
                        return value;
                  }
            }

            return ModSound.HARD;
      }, Enum::ordinal);

      public static final MapCodec<ModSound> MAP_CODEC = ModSound.CODEC.fieldOf("sound").orElse(ModSound.SOFT);

      public static StreamCodec<? super ByteBuf, ModSound> STREAM_CODEC = ByteBufCodecs.BYTE.map(in -> {
            for (ModSound value : ModSound.values()) {
                  if (value.i == in) {
                        return value;
                  }
            }
            return ModSound.HARD;
      }, in -> in.i);
}
