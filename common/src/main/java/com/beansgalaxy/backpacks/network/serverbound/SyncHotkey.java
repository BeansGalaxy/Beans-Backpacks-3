package com.beansgalaxy.backpacks.network.serverbound;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.access.BackData;
import com.beansgalaxy.backpacks.network.Network2S;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class SyncHotkey implements Packet2S {
      final boolean actionKey;
      final boolean menuKey;
      final int tinySlot;

      public SyncHotkey(RegistryFriendlyByteBuf buf) {
            this(buf.readBoolean(), buf.readBoolean(), buf.readInt());
      }

      private SyncHotkey(boolean actionKey, boolean menuKey, int tinySlot) {
            this.actionKey = actionKey;
            this.menuKey = menuKey;
            this.tinySlot = tinySlot;
      }

      public static void send(boolean actionKey, boolean menuKey, int tinySlot) {
            new SyncHotkey(actionKey, menuKey, tinySlot).send2S();
      }

      @Override
      public Network2S getNetwork() {
            return Network2S.HOTKEY_2S;
      }

      @Override
      public void encode(RegistryFriendlyByteBuf buf) {
            buf.writeBoolean(actionKey);
            buf.writeBoolean(menuKey);
            buf.writeInt(tinySlot);

      }

      @Override
      public void handle(Player sender) {
            BackData backData = BackData.get(sender);
            backData.setActionKey(actionKey);
            backData.setMenuKey(menuKey);
            backData.setTinySlot(tinySlot);
      }

      public static Type<SyncHotkey> ID = new Type<>(Constants.defaultLocation("sync_hotkey_s"));

      @Override
      public Type<? extends CustomPacketPayload> type() {
            return ID;
      }
}
