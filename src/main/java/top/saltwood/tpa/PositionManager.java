package top.saltwood.tpa;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PositionManager {
    // Key: Player UUID, Value: Last Death Location Object
    private static final Map<UUID, PlayerLocation> deathLocations = new HashMap<>();
    private static final Map<UUID, PlayerLocation> tpaLocations = new HashMap<>();

    public static void setDeath(ServerPlayer player) {
        deathLocations.put(player.getUUID(), PlayerLocation.of(player));
    }

    public static @Nullable PlayerLocation getDeath(UUID playerId) {
        return deathLocations.get(playerId);
    }

    public static void setTpa(ServerPlayer player) {
        tpaLocations.put(player.getUUID(), PlayerLocation.of(player));
    }

    public static @Nullable PlayerLocation getTpa(UUID playerId) {
        return tpaLocations.get(playerId);
    }

    // Simple container class to hold location data
    public static class PlayerLocation {
        public final double x;
        public final double y;
        public final double z;
        public final float yRot;
        public final float xRot;
        public final ResourceKey<Level> dimension;

        public PlayerLocation(double x, double y, double z, float yRot, float xRot, ResourceKey<Level> dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
            this.dimension = dimension;
        }

        public static PlayerLocation of(ServerPlayer player) {
            return new PlayerLocation(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), player.serverLevel().dimension());
        }

        public void teleport(ServerPlayer player) {
            ServerLevel world = player.server.getLevel(this.dimension);
            if (world != null) player.teleportTo(world, this.x, this.y, this.z, this.yRot, this.xRot);
        }
    }
}
