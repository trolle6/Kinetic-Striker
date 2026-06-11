package minigame.tnt_rocket_leauge.game;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.PrimedTnt;

import java.util.*;

public class GameManager {
    private static GameManager instance;
    
    private Game currentGame;
    private final Map<UUID, PrimedTnt> gameBalls = new HashMap<>();
    private final Map<UUID, BallData> ballData = new HashMap<>();
    
    // Lobby system
    private net.minecraft.core.BlockPos lobbyCenter;
    private GameMode lobbyGameMode;
    private net.minecraft.core.BlockPos arenaCorner1;
    private net.minecraft.core.BlockPos arenaCorner2;
    private net.minecraft.world.phys.AABB redGoal;
    private net.minecraft.world.phys.AABB blueGoal;
    private long lastLobbyTrigger = 0; // Cooldown for lobby triggering (5 seconds)
    
    private GameManager() {}
    
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public boolean isGameBall(PrimedTnt tnt) {
        return gameBalls.containsKey(tnt.getUUID());
    }
    
    public PrimedTnt getGameBall(UUID tntUUID) {
        return gameBalls.get(tntUUID);
    }
    
    public void registerGameBall(PrimedTnt tnt) {
        gameBalls.put(tnt.getUUID(), tnt);
        ballData.put(tnt.getUUID(), new BallData());
    }
    
    public void unregisterGameBall(PrimedTnt tnt) {
        gameBalls.remove(tnt.getUUID());
        ballData.remove(tnt.getUUID());
    }
    
    public BallData getBallData(PrimedTnt tnt) {
        return ballData.get(tnt.getUUID());
    }
    
    public void recordBallHit(PrimedTnt tnt, UUID playerUUID, Team team) {
        BallData data = ballData.get(tnt.getUUID());
        if (data != null) {
            data.recordHit(playerUUID, team);
        }
    }
    
    public void tickGameBall(PrimedTnt tnt) {
        if (currentGame != null) {
            currentGame.tickBall(tnt);
        }
        
        // Spawn trail particles (only on server, every few ticks to reduce spam)
        if (tnt != null && tnt.level() instanceof ServerLevel serverLevel && tnt.tickCount % 5 == 0) {
            minigame.tnt_rocket_leauge.effects.ParticleEffects.spawnBallTrail(
                serverLevel, tnt.position());
        }
    }
    
    // Tick the game even without a ball (for countdown)
    public void tickGame() {
        if (currentGame != null && currentGame.isActive()) {
            currentGame.tickBall(null); // Null ball is OK - just ticks countdown
        }
    }
    
    
    public void startGame(ServerLevel level, Arena arena, GameMode gameMode) {
        if (currentGame != null) {
            currentGame.end();
        }
        
        // Stop all music discs playing (from previous game victory)
        for (net.minecraft.server.level.ServerPlayer player : level.players()) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                null, // Stop all sounds
                net.minecraft.sounds.SoundSource.RECORDS // Only stop music disc sounds
            ));
        }
        
        currentGame = new Game(level, arena, gameMode);
        currentGame.start();
    }
    
    public void endGame() {
        if (currentGame != null) {
            currentGame.end();
            currentGame = null;
        }
        
        // Clean up all game balls
        gameBalls.values().forEach(tnt -> {
            if (!tnt.isRemoved()) {
                tnt.discard();
            }
        });
        gameBalls.clear();
    }
    
    public void setLobby(net.minecraft.core.BlockPos center, GameMode mode, 
                         net.minecraft.core.BlockPos corner1, net.minecraft.core.BlockPos corner2,
                         net.minecraft.world.phys.AABB redGoal, net.minecraft.world.phys.AABB blueGoal) {
        this.lobbyCenter = center;
        this.lobbyGameMode = mode;
        this.arenaCorner1 = corner1;
        this.arenaCorner2 = corner2;
        this.redGoal = redGoal;
        this.blueGoal = blueGoal;
    }
    
    public net.minecraft.core.BlockPos getLobbyCenter() {
        return lobbyCenter;
    }
    
    public void triggerLobbyStart(ServerLevel level) {
        if (lobbyCenter == null || lobbyGameMode == null) {
            return;
        }
        
        // Don't start if a game is already active
        if (currentGame != null && currentGame.isActive()) {
            return;
        }
        
        // Cooldown check (5 seconds between triggers)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLobbyTrigger < 5000) {
            return; // Still on cooldown
        }
        
        // Find all players in lobby area (15 block radius)
        List<ServerPlayer> lobbyPlayers = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            double distance = player.position().distanceTo(
                net.minecraft.world.phys.Vec3.atCenterOf(lobbyCenter));
            if (distance <= 15.0) {
                lobbyPlayers.add(player);
            }
        }
        
        if (lobbyPlayers.isEmpty()) {
            return;
        }
        
        // Update last trigger time
        lastLobbyTrigger = currentTime;
        
        // Shuffle and assign to teams
        Collections.shuffle(lobbyPlayers);
        
        // Stop any playing music
        for (ServerPlayer player : level.players()) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                null, net.minecraft.sounds.SoundSource.RECORDS));
        }
        
        // Start fresh game
        Arena arena = new Arena(arenaCorner1, arenaCorner2, redGoal, blueGoal);
        currentGame = new Game(level, arena, lobbyGameMode);
        currentGame.start();
        
        // Add players to teams (alternate between RED and BLUE)
        int maxPerTeam = lobbyGameMode.getMaxPlayersPerTeam();
        int redCount = 0;
        int blueCount = 0;
        
        for (ServerPlayer player : lobbyPlayers) {
            Team team;
            PlayerRole role = PlayerRole.ATTACKER; // Default to attacker
            
            if (redCount < maxPerTeam && (blueCount >= maxPerTeam || redCount <= blueCount)) {
                team = Team.RED;
                redCount++;
                // First player on each team is goalie
                if (redCount == 1) {
                    role = PlayerRole.GOALIE;
                }
            } else {
                team = Team.BLUE;
                blueCount++;
                // First player on each team is goalie
                if (blueCount == 1) {
                    role = PlayerRole.GOALIE;
                }
            }
            
            currentGame.addPlayerToTeam(player, team, role);
        }
        
        // Broadcast message
        currentGame.broadcastMessage(
            net.minecraft.network.chat.Component.literal("🎮 GAME STARTING! 🎮")
                .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD));
        currentGame.broadcastMessage(
            net.minecraft.network.chat.Component.literal("Teams: " + redCount + "v" + blueCount)
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }
    
    public boolean isGameActive() {
        return currentGame != null && currentGame.isActive();
    }
    
    public Game getCurrentGame() {
        return currentGame;
    }
    
    public void addPlayerToTeam(ServerPlayer player, Team team, PlayerRole role) {
        if (currentGame != null) {
            currentGame.addPlayerToTeam(player, team, role);
        }
    }
    
    public void removePlayer(ServerPlayer player) {
        if (currentGame != null) {
            currentGame.removePlayer(player);
        }
    }
}

