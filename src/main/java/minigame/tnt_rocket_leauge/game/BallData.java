package minigame.tnt_rocket_leauge.game;

import java.util.UUID;

/**
 * Tracks metadata about the game ball
 */
public class BallData {
    private UUID lastHitBy; // UUID of the player who hit it last
    private Team lastHitTeam; // Team of that player
    private long lastHitTime; // When it was hit
    
    public void recordHit(UUID playerUUID, Team team) {
        this.lastHitBy = playerUUID;
        this.lastHitTeam = team;
        this.lastHitTime = System.currentTimeMillis();
    }
    
    public UUID getLastHitBy() {
        return lastHitBy;
    }
    
    public Team getLastHitTeam() {
        return lastHitTeam;
    }
    
    public long getLastHitTime() {
        return lastHitTime;
    }
    
    public boolean hasBeenHit() {
        return lastHitBy != null;
    }
    
    public void reset() {
        this.lastHitBy = null;
        this.lastHitTeam = null;
        this.lastHitTime = 0;
    }
}

