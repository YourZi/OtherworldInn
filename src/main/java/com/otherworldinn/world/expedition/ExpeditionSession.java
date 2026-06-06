package com.otherworldinn.world.expedition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class ExpeditionSession {

    private final ResourceKey<Level> dimensionKey;
    private final Set<UUID> activePlayers;
    private final Set<UUID> departedPlayers = new HashSet<>();
    private final List<String> componentIds;
    private final long deadlineTick;
    private boolean dimensionCreated;
    private int centerX = 0;
    private int centerZ = 0;

    public ExpeditionSession(ResourceKey<Level> dimensionKey, Set<UUID> activePlayers,
            List<String> componentIds, long deadlineTick) {
        this.dimensionKey = dimensionKey;
        this.activePlayers = new HashSet<>(activePlayers);
        this.componentIds = new ArrayList<>(componentIds);
        this.deadlineTick = deadlineTick;
    }

    public void setCenter(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    public int getCenterX() { return centerX; }
    public int getCenterZ() { return centerZ; }

    public ResourceKey<Level> dimensionKey() { return dimensionKey; }
    public Set<UUID> activePlayers() { return Set.copyOf(activePlayers); }
    public Set<UUID> departedPlayers() { return Set.copyOf(departedPlayers); }
    public List<String> componentIds() { return List.copyOf(componentIds); }
    public long deadlineTick() { return deadlineTick; }
    public boolean dimensionCreated() { return dimensionCreated; }
    public void markDimensionCreated() { this.dimensionCreated = true; }

    public void markDeparted(UUID playerId) {
        departedPlayers.add(playerId);
    }

    public boolean canEnter(UUID playerId) {
        return activePlayers.contains(playerId) && !departedPlayers.contains(playerId);
    }

    public boolean isExpired(long currentTick) {
        return currentTick >= deadlineTick;
    }

    public static boolean hasComponent(List<String> componentIds, String id) {
        return componentIds.contains(id);
    }
}
