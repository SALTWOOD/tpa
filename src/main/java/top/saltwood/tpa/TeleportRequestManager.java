package top.saltwood.tpa;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class TeleportRequestManager {
    private final Map<Integer, TeleportRequest> requests = new HashMap<>();
    private final Map<UUID, List<Integer>> playerRequests = new HashMap<>();

    void addRequest(ServerPlayer requester, ServerPlayer target, boolean isHere) {
        TeleportRequest request = new TeleportRequest(requester, target, isHere);
        requests.put(request.id, request);

        playerRequests.computeIfAbsent(requester.getUUID(), k -> new ArrayList<>()).add(request.id);
        playerRequests.computeIfAbsent(target.getUUID(), k -> new ArrayList<>()).add(request.id);
    }

    void removeRequest(int id) {
        TeleportRequest request = requests.remove(id);
        if (request != null) {
            playerRequests.get(request.requester.getUUID()).remove(Integer.valueOf(id));
            playerRequests.get(request.target.getUUID()).remove(Integer.valueOf(id));
        }
    }

    TeleportRequest getRequest(UUID requesterId, UUID targetId) {
        for (TeleportRequest request : requests.values()) {
            if (request.requester.getUUID().equals(requesterId) &&
                    request.target.getUUID().equals(targetId)) {
                return request;
            }
        }
        return null;
    }

    TeleportRequest getLatestRequestFor(UUID playerId) {
        List<Integer> ids = playerRequests.get(playerId);
        if (ids == null || ids.isEmpty()) return null;

        TeleportRequest latest = null;
        for (Integer id : ids) {
            TeleportRequest request = requests.get(id);
            if (request != null && (latest == null || request.createdAt > latest.createdAt)) {
                latest = request;
            }
        }
        return latest;
    }

    boolean hasPendingRequest(UUID requesterId, UUID targetId) {
        return getRequest(requesterId, targetId) != null;
    }

    void update() {
        List<Integer> expired = new ArrayList<>();

        for (TeleportRequest request : requests.values()) {
            if (request.isExpired()) {
                expired.add(request.id);
            }
        }

        for (Integer id : expired) {
            TeleportRequest request = requests.get(id);
            if (request != null) {
                MutableComponent requestMsg = Component.literal("[").withStyle(ChatFormatting.GRAY)
                        .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(request.isHere ? " ← " : " → ").withStyle(ChatFormatting.WHITE))
                        .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal("]").withStyle(ChatFormatting.GRAY));

                request.requester.sendSystemMessage(requestMsg.copy()
                        .append(Component.literal(" Teleport request expired").withStyle(ChatFormatting.RED)));
                request.target.sendSystemMessage(requestMsg.copy()
                        .append(Component.literal(" Teleport request expired").withStyle(ChatFormatting.RED)));
                removeRequest(id);
            }
        }
    }
}
