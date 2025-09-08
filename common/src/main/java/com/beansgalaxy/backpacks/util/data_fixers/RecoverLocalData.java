package com.beansgalaxy.backpacks.util.data_fixers;

import com.beansgalaxy.backpacks.CommonClass;
import com.beansgalaxy.backpacks.components.ender.EmptyEnderItem;
import com.beansgalaxy.backpacks.data.EnderStorage;
import com.beansgalaxy.backpacks.traits.ITraitData;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.common.BackpackEntity;
import com.beansgalaxy.backpacks.items.ModItems;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;

import java.util.ArrayList;
import java.util.List;

public class RecoverLocalData {

      public static void readEntity(BackpackEntity entity, CompoundTag tag) {
//            entityData.set(LOCKED, tag.getBoolean("Locked"));
//            this.entityData.set(OWNER, Optional.of(localData.getUUID("owner")));
//            entityData.set(LOCAL_DATA, localData);
//            itemTags = tag.getCompound("item_tags");

            RegistryAccess access = entity.registryAccess();

            entity.setDirection(Direction.from3DDataValue(tag.getByte("facing")));
            entity.setNoGravity(tag.getBoolean("hanging"));

            ItemStack backpack;
            EntityType<?> type = entity.getType();
            if (type == CommonClass.LEGACY_ENDER_ENTITY) {
                  backpack = ModItems.EMPTY_ENDER_POUCH.get().getDefaultInstance();
                  backpack.set(Traits.EMPTY_ENDER, new EmptyEnderItem.UnboundEnderTraits(EnderStorage.LEGACY_ENDER_LOCATION));
            }
            else {
                  CompoundTag localDataTag = tag.getCompound("local_data");
                  LocalData localData = new LocalData(localDataTag);
                  backpack = localData.toStack(access, tag);
            }

            entity.getEntityData().set(BackpackEntity.ITEM_STACK, backpack);
      }

      public static ArrayList<ItemStack> readStackNbt(CompoundTag nbt, RegistryAccess access) {
            ArrayList<ItemStack> list = new ArrayList<>();
            ListTag nbtList = nbt.getList("Items", Tag.TAG_COMPOUND);
            for (int i = 0; i < nbtList.size(); ++i) {
                  CompoundTag nbtCompound = nbtList.getCompound(i);

                  RegistryOps<Tag> context = access.createSerializationContext(NbtOps.INSTANCE);
                  Dynamic<Tag> dynamic = new Dynamic<>(context, nbtCompound);
                  Dynamic<Tag> updated = BPack2PlayerFix.updateVersion(dynamic);

                  ItemStack.CODEC.parse(updated).ifSuccess(stack -> {
                        if (!stack.isEmpty())
                              list.add(stack);
                  });
            }

            return list;
      }

      public static class LocalData {
            public final Kind kind;
            public final String backpack_id;
            public int color = 0xFFFFFF;
            private CompoundTag trim = new CompoundTag();

            public LocalData(CompoundTag tag) {
                  String kindString = tag.getString("kind");
                  this.kind = Kind.fromName(kindString);
                  this.backpack_id = tag.getString("backpack_id");
                  if (!tag.contains("empty") || !tag.getBoolean("empty")) {
                        this.color = tag.getInt("color");
                        this.trim = tag.getCompound("Trim");
                  }
            }

            public ItemStack toStack(RegistryAccess access, CompoundTag tag) {
                  ItemStack backpack;
                  switch (kind) {
                        case METAL -> {
                              backpack = switch (backpack_id) {
                                    case "gold" -> ModItems.GOLD_BACKPACK.get().getDefaultInstance();
                                    case "netherite" -> ModItems.NETHERITE_BACKPACK.get().getDefaultInstance();
                                    default -> ModItems.IRON_BACKPACK.get().getDefaultInstance();
                              };

                              RegistryOps<Tag> ops = access.createSerializationContext(NbtOps.INSTANCE);
                              ArmorTrim.CODEC.parse(ops, trim).ifSuccess(armorTrim -> {
                                    backpack.set(DataComponents.TRIM, armorTrim);
                              });

                              List<ItemStack> stacks = readStackNbt(tag, access);
                              backpack.set(ITraitData.ITEM_STACKS, stacks);
                        }
                        case LEATHER, BIG_BUNDLE -> {
                              backpack = ModItems.LEATHER_BACKPACK.get().getDefaultInstance();

                              DyedItemColor itemColor = new DyedItemColor(color, true);
                              backpack.set(DataComponents.DYED_COLOR, itemColor);

                              List<ItemStack> stacks = readStackNbt(tag, access);
                              backpack.set(ITraitData.ITEM_STACKS, stacks);
                        }
                        case WINGED -> {
                              backpack = ModItems.LEATHER_BACKPACK.get().getDefaultInstance();

                              DyedItemColor itemColor = new DyedItemColor(color, true);
                              backpack.set(DataComponents.DYED_COLOR, itemColor);

                              List<ItemStack> stacks = readStackNbt(tag, access);

                              Tag itemTags = tag.get("item_tags");
                              if (itemTags != null) {
                                    CompoundTag compoundTag = new CompoundTag();
                                    compoundTag.putString("id", "minecraft:elytra");
                                    compoundTag.putInt("count", 1);
                                    compoundTag.put("tag", itemTags);

                                    RegistryOps<Tag> context = access.createSerializationContext(NbtOps.INSTANCE);
                                    Dynamic<Tag> dynamic = new Dynamic<>(context, compoundTag);
                                    Dynamic<Tag> updated = BPack2PlayerFix.updateVersion(dynamic);
                                    DataResult<ItemStack> parse = ItemStack.CODEC.parse(updated);
                                    if (parse.isSuccess())
                                          stacks.add(parse.getOrThrow());
                                    else
                                          stacks.add(Items.ELYTRA.getDefaultInstance());
                              }
                              else stacks.add(Items.ELYTRA.getDefaultInstance());

                              backpack.set(ITraitData.ITEM_STACKS, stacks);
                        }
                        case ENDER -> {
                              backpack = ModItems.EMPTY_ENDER_POUCH.get().getDefaultInstance();
                              backpack.set(Traits.EMPTY_ENDER, new EmptyEnderItem.UnboundEnderTraits(EnderStorage.LEGACY_ENDER_LOCATION));
                        }
                        default ->
                              backpack = ModItems.IRON_BACKPACK.get().getDefaultInstance();
                  }
                  return backpack;
            }
      }

      public enum Kind {
            LEATHER(),
            METAL(),
            WINGED(),
            ENDER(),
            POT(),
            CAULDRON(),
            BIG_BUNDLE(),
            ;

            public static Kind fromName(String string) {
                  for(Kind kind: Kind.values())
                        if (kind.name().equals(string))
                              return kind;

                  return METAL;
            }
      }
}
