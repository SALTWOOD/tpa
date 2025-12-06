package top.saltwood.tpa;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathPositionManager {
    // Key: Player UUID, Value: Last Death Location Object
    private static final Map<UUID, PlayerDeathLocation> deathLocations = new HashMap<>();

    public static void setLocation(UUID playerId, PlayerDeathLocation death) {
        deathLocations.put(playerId, death);
    }

    public static PlayerDeathLocation getLocation(UUID playerId) {
        return deathLocations.get(playerId);
    }

    // Simple container class to hold location data
    public static class PlayerDeathLocation {
        public final double x;
        public final double y;
        public final double z;
        public final ResourceKey<Level> dimension;

        public PlayerDeathLocation(double x, double y, double z, ResourceKey<Level> dimension) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
        }
    }
}
