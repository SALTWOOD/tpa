package top.saltwood.tpa;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class TeleportRequest {
    private static int nextId = 0;

    final int id;
    final ServerPlayer requester;
    final ServerPlayer target;
    final boolean isHere;
    final long createdAt;

    TeleportRequest(ServerPlayer requester, ServerPlayer target, boolean isHere) {
        this.id = nextId++;
        this.requester = requester;
        this.target = target;
        this.isHere = isHere;
        this.createdAt = System.currentTimeMillis();
    }

    boolean isExpired() {
        return System.currentTimeMillis() - createdAt > 60000;
    }
}

