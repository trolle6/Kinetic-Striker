package minigame.tnt_rocket_leauge.game;

import net.minecraft.ChatFormatting;
import net.minecraft.util.ARGB;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class Game {
    private final ServerLevel level;
    private final Arena arena;
    private final GameMode gameMode;
    private final Map<Team, Integer> scores = new HashMap<>();
    private final Map<UUID, Team> playerTeams = new HashMap<>();
    private final Map<UUID, PlayerRole> playerRoles = new HashMap<>();
    private final Map<UUID, net.minecraft.world.entity.Entity> playerMounts = new HashMap<>();
    private final Map<Team, List<net.minecraft.world.entity.decoration.ArmorStand>> teamArmorStands = new HashMap<>();
    private double beaconRotationAngle = 0; // Current rotation angle for beam light show
    private Team victoryTeam = null; // Track which team won for beam colors
    private final ChaosItemManager chaosItemManager = new ChaosItemManager();
    private final CountdownManager countdownManager;
    private final TNTDispenserManager dispenserManager = new TNTDispenserManager();
    private final ServingSystem servingSystem = new ServingSystem();
    private PrimedTnt ball;
    private boolean active = false;
    private int goalCooldown = 0; // Prevent duplicate scoring
    private static final int POINTS_TO_WIN = 3;
    private int victoryCleanupTimer = -1; // Timer for victory celebration cleanup
    
    public Game(ServerLevel level, Arena arena, GameMode gameMode) {
        this.level = level;
        this.arena = arena;
        this.gameMode = gameMode;
        this.countdownManager = new CountdownManager(this);
        scores.put(Team.RED, 0);
        scores.put(Team.BLUE, 0);
        teamArmorStands.put(Team.RED, new ArrayList<>());
        teamArmorStands.put(Team.BLUE, new ArrayList<>());
    }
    
    public void start() {
        active = true;
        spawnTestArmorStands();
        
        // Fill all dispensers with TNT at game start!
        fillDispensersWithTNT();
        
        // DON'T spawn ball yet - wait for countdown to finish and dispenser to activate!
        
        broadcastMessage(Component.literal("⚽ Game Lobby Ready! [" + gameMode.getDisplayName() + "]").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        broadcastMessage(Component.literal("🚪 Walk through banners to join your team!").withStyle(ChatFormatting.YELLOW));
    }
    
    private void fillDispensersWithTNT() {
        // Search arena for all dispensers and fill them with TNT
        int minX = Math.min(arena.getCorner1().getX(), arena.getCorner2().getX());
        int maxX = Math.max(arena.getCorner1().getX(), arena.getCorner2().getX());
        int minY = Math.min(arena.getCorner1().getY(), arena.getCorner2().getY());
        int maxY = Math.max(arena.getCorner1().getY(), arena.getCorner2().getY());
        int minZ = Math.min(arena.getCorner1().getZ(), arena.getCorner2().getZ());
        int maxZ = Math.max(arena.getCorner1().getZ(), arena.getCorner2().getZ());
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(x, y, z);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                    
                    if (state.getBlock() instanceof net.minecraft.world.level.block.DispenserBlock) {
                        // Fill this dispenser with TNT!
                        if (level.getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.DispenserBlockEntity dispenser) {
                            // Clear and add 64 TNT
                            for (int i = 0; i < dispenser.getContainerSize(); i++) {
                                dispenser.setItem(i, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.TNT, 64));
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void tryStartCountdown() {
        // Check if we have at least one player
        long playerCount = playerTeams.size();
        
        if (playerCount >= 1 && !countdownManager.isCountdownActive()) {
            // Start countdown when first player joins!
            countdownManager.startCountdown();
        }
    }
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    private void spawnTestArmorStands() {
        Vec3 center = arena.getCenterPos();
        
        // Spawn armor stands based on game mode (6 for 3v3, 4 for 2v2)
        int standCount = gameMode.getMaxPlayersPerTeam() * 2;
        for (int i = 0; i < standCount; i++) {
            double angle = (i * Math.PI * 2) / standCount;
            double offsetX = Math.cos(angle) * 8;
            double offsetZ = Math.sin(angle) * 8;
            
            Team team = i % 2 == 0 ? Team.RED : Team.BLUE;
            
            net.minecraft.world.entity.decoration.ArmorStand stand = 
                new net.minecraft.world.entity.decoration.ArmorStand(
                    net.minecraft.world.entity.EntityType.ARMOR_STAND, level);
            stand.setPos(center.x + offsetX, center.y, center.z + offsetZ);
            stand.setCustomName(Component.literal("Player " + (i + 1))
                .withStyle(team.getColor()));
            stand.setCustomNameVisible(true);
            stand.setNoGravity(true);
            stand.setInvulnerable(true);
            level.addFreshEntity(stand);
            
            // Store reference to armor stand by team
            teamArmorStands.get(team).add(stand);
        }
    }
    
    private void startVictoryBeamShow(Team winningTeam) {
        broadcastMessage(Component.literal("🎆 " + winningTeam.getFullName() + " BEAM LIGHT SHOW!")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        
        beaconRotationAngle = 0;
        victoryTeam = winningTeam;
        
        broadcastMessage(Component.literal("🌟 Rotating " + winningTeam.getDisplayName() + " light beams surround the arena!")
            .withStyle(winningTeam.getColor(), ChatFormatting.BOLD));
    }
    
    public void end() {
        active = false;
        if (ball != null && !ball.isRemoved()) {
            ball.discard();
            GameManager.getInstance().unregisterGameBall(ball);
        }
        
        // Reset beam show
        beaconRotationAngle = 0;
        victoryTeam = null;
        
        // Announce winner
        Team winner = scores.get(Team.RED) > scores.get(Team.BLUE) ? Team.RED : Team.BLUE;
        int redScore = scores.get(Team.RED);
        int blueScore = scores.get(Team.BLUE);
        
        if (redScore == blueScore) {
            broadcastMessage(Component.literal("Game Ended! It's a tie! " + redScore + " - " + blueScore)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        } else {
            broadcastMessage(Component.literal("Game Ended! " + winner.getFullName() + " wins! " + 
                redScore + " - " + blueScore)
                .withStyle(winner.getColor(), ChatFormatting.BOLD));
        }
        
        playerTeams.clear();
    }
    
    private void spawnBall() {
        if (ball != null && !ball.isRemoved()) {
            ball.discard();
            GameManager.getInstance().unregisterGameBall(ball);
        }
        
        // Spawn ball at CENTER HEIGHT for sky gameplay!
        Vec3 spawnPos = arena.getCenterPos();
        
        // Create vanilla primed TNT with infinite fuse
        ball = new PrimedTnt(level, spawnPos.x, spawnPos.y, spawnPos.z, null);
        ball.setFuse(Integer.MAX_VALUE); // Infinite fuse - no explosion
        ball.setDeltaMovement(Vec3.ZERO);
        
        // Slow falling effect applied by TntEntityMixin!
        ball.setNoGravity(false);
        
        level.addFreshEntity(ball);
        GameManager.getInstance().registerGameBall(ball);
        
        // Play spawn sound
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, 
            SoundEvents.TNT_PRIMED, SoundSource.MASTER, 1.0f, 1.0f);
    }
    
    public void tickBall(PrimedTnt tnt) {
        // Handle victory celebration cleanup timer
        if (victoryCleanupTimer >= 0) {
            victoryCleanupTimer--;
            
            // BEAM LIGHT SHOW: Spawn rotating COLORED particle beams every tick!
            if (victoryTeam != null) {
                // Increment rotation angle (slow rotation)
                beaconRotationAngle += Math.PI / 180.0; // Full rotation in ~360 ticks (18 seconds)
                
                Vec3 center = arena.getCenterPos();
                double radius = 18.0; // Distance from center
                int groundY = (int) Math.min(arena.getCorner1().getY(), arena.getCorner2().getY());
                int beamHeight = 30; // How tall the beams are
                
                // Create COLORED dust particles based on team!
                net.minecraft.core.particles.ParticleOptions beamParticle;
                if (victoryTeam == Team.BLUE) {
                    // Bright cyan/blue color (R=0, G=0.6, B=1.0)
                    beamParticle = new net.minecraft.core.particles.DustParticleOptions(
                        ARGB.colorFromFloat(1.0f, 0.0f, 0.6f, 1.0f), 1.5f
                    );
                } else {
                    // Bright red color (R=1.0, G=0.1, B=0.1)
                    beamParticle = new net.minecraft.core.particles.DustParticleOptions(
                        ARGB.colorFromFloat(1.0f, 1.0f, 0.1f, 0.1f), 1.5f
                    );
                }
                
                // Spawn 6 rotating beams
                int beamCount = 6;
                for (int i = 0; i < beamCount; i++) {
                    double angle = beaconRotationAngle + (i * Math.PI * 2.0) / beamCount;
                    
                    double beamX = center.x + Math.cos(angle) * radius;
                    double beamZ = center.z + Math.sin(angle) * radius;
                    
                    // Create a vertical beam by spawning particles at different heights
                    for (int h = 0; h < beamHeight; h += 2) { // Every 2 blocks
                        double y = groundY + h;
                        
                        // Main beam particle (dense)
                        level.sendParticles(
                            beamParticle,
                            beamX, y, beamZ,
                            5, 0.1, 0.1, 0.1, 0.0 // Tight cluster
                        );
                        
                        // Add some spiral effect for extra flair
                        if (h % 4 == 0) {
                            double spiralRadius = 0.5;
                            double spiralAngle = h * 0.3;
                            level.sendParticles(
                                beamParticle,
                                beamX + Math.cos(spiralAngle) * spiralRadius,
                                y,
                                beamZ + Math.sin(spiralAngle) * spiralRadius,
                                2, 0.05, 0.05, 0.05, 0.0
                            );
                        }
                    }
                }
            }
            
            // No constant particle spam - cleaner look!
            
            if (victoryCleanupTimer == 0) {
                // Time's up - actually end the game now
                GameManager.getInstance().endGame();
            }
            return; // Don't process ball during victory celebration
        }
        
        if (!active) {
            return;
        }
        
        // Set ball reference if this is a new ball from dispenser
        if (ball == null && tnt != null && GameManager.getInstance().isGameBall(tnt)) {
            ball = tnt;
        }
        
        // Check if this is the current ball
        if (tnt != null && tnt != ball) {
            return;
        }
        
        // Tick countdown (works even without ball)
        countdownManager.tick(level);
        
        // Only do ball-specific logic if we have a ball
        if (ball == null || ball.isRemoved()) {
            return;
        }
        
        // Tick chaos item manager (only after countdown)
        if (!countdownManager.isCountdownActive()) {
            chaosItemManager.tick(this, level);
        }
        
        // Decrement cooldown
        if (goalCooldown > 0) {
            goalCooldown--;
        }
        
        // Check for goals every tick (more responsive)
        if (goalCooldown <= 0) {
            checkGoals();
        }
        
        // Arena bounds - reset if it goes behind goals or too far out (only if ball exists!)
        if (ball != null && !ball.isRemoved()) {
            Vec3 pos = ball.position();
            AABB bounds = arena.getBounds();
            
            // STRICT check for behind goals (5 blocks) - more lenient for sides
            boolean behindGoal = pos.z < bounds.minZ - 5 || pos.z > bounds.maxZ + 5;
            boolean tooFarSides = pos.x < bounds.minX - 15 || pos.x > bounds.maxX + 15;
            boolean tooFarVertical = pos.y < bounds.minY - 10 || pos.y > bounds.maxY + 10;
            
            if (behindGoal || tooFarSides || tooFarVertical) {
                // Ball went out of bounds - reset to center
                resetBallPosition();
                
                broadcastMessage(Component.literal("⚠ Ball out of bounds - Reset!").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
            }
        }
    }
    
    private void checkGoals() {
        if (ball == null || ball.isRemoved()) {
            return;
        }
        
        if (goalCooldown > 0) {
            return;
        }
        
        BallData ballData = GameManager.getInstance().getBallData(ball);
        if (ballData == null || !ballData.hasBeenHit()) {
            return;
        }
        
        Team hittingTeam = ballData.getLastHitTeam();
        
        // Extra safety check before getting position
        if (ball == null || ball.isRemoved()) {
            return;
        }
        
        Vec3 ballPos = ball.position();
        
        // Check if ball is in RED goal BOUNDING BOX (BLUE team scores)
        if (arena.getRedGoal().contains(ballPos)) {
            if (hittingTeam == Team.BLUE) {
                hitGoal(Team.RED, Team.BLUE);
                return;
            }
        }
        
        // Check if ball is in BLUE goal BOUNDING BOX (RED team scores)
        if (arena.getBlueGoal().contains(ballPos)) {
            if (hittingTeam == Team.RED) {
                hitGoal(Team.BLUE, Team.RED);
                return;
            }
        }
    }
    
    private void hitGoal(Team goalTeam, Team scoringTeam) {
        // Set cooldown to prevent duplicate scoring
        goalCooldown = 40; // 2 seconds
        
        // Safety check for ball
        if (ball == null || ball.isRemoved()) {
            // Ball is gone, just award the score
            score(scoringTeam);
            return;
        }
        
        // Double-check ball still exists
        if (ball == null || ball.isRemoved()) {
            score(scoringTeam);
            return;
        }
        
        Vec3 pos = ball.position();
        net.minecraft.core.BlockPos blockPos = ball.blockPosition();
        
        // Determine which wool to explode (red or blue)
        var targetWool = (goalTeam == Team.RED) ? 
            net.minecraft.world.level.block.Blocks.RED_WOOL : 
            net.minecraft.world.level.block.Blocks.BLUE_WOOL;
        
        // Explode nearby WOOL blocks (not powder snow)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    net.minecraft.core.BlockPos checkPos = blockPos.offset(dx, dy, dz);
                    net.minecraft.world.level.block.state.BlockState state = level.getBlockState(checkPos);
                    
                    if (state.is(targetWool)) {
                        // Destroy this wool block with explosion effect
                        level.destroyBlock(checkPos, true);
                    }
                }
            }
        }
        
        // Spawn explosion particles
        minigame.tnt_rocket_leauge.effects.ParticleEffects.spawnGoalExplosion(
            level, pos, goalTeam == Team.RED);
        
        // Play hit sound
        level.playSound(null, pos.x, pos.y, pos.z, 
            SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 1.0f, 1.5f);
        
        // IMMEDIATELY ADD POINT - no "3 hits" system
        broadcastMessage(Component.literal("⚽ GOAL! " + scoringTeam.getFullName() + " scores!")
            .withStyle(scoringTeam.getColor(), ChatFormatting.BOLD));
        
        score(scoringTeam);
        // Note: score() now handles ball removal and player reset
    }
    
    private void resetBallPosition() {
        if (ball != null && !ball.isRemoved()) {
            Vec3 center = arena.getCenterPos();
            ball.teleportTo(center.x, center.y, center.z);
            ball.setDeltaMovement(Vec3.ZERO);
            // Slow falling effect applied by TntEntityMixin!
            ball.setNoGravity(false);
            
            // Reset ball data
            BallData ballData = GameManager.getInstance().getBallData(ball);
            if (ballData != null) {
                ballData.reset();
            }
            
            // Reset all player equipment and teleport to spawn
            for (ServerPlayer player : level.players()) {
                if (playerTeams.containsKey(player.getUUID())) {
                    Team team = playerTeams.get(player.getUUID());
                    
                    // SKY GAME: Spawn players HIGH IN THE AIR in front of their goals!
                    Vec3 basePos;
                    Vec3 arenaCenter = arena.getCenterPos(); // Now 45 blocks high!
                    
                    if (team == Team.RED) {
                        // Spawn in front of red goal (toward center) at sky height
                        Vec3 goalCenter = arena.getRedGoal().getCenter();
                        Vec3 direction = arenaCenter.subtract(goalCenter).normalize();
                        basePos = new Vec3(
                            goalCenter.x + direction.x * 10,
                            arenaCenter.y, // Use high center Y!
                            goalCenter.z + direction.z * 10
                        );
                    } else {
                        // Spawn in front of blue goal (toward center) at sky height
                        Vec3 goalCenter = arena.getBlueGoal().getCenter();
                        Vec3 direction = arenaCenter.subtract(goalCenter).normalize();
                        basePos = new Vec3(
                            goalCenter.x + direction.x * 10,
                            arenaCenter.y, // Use high center Y!
                            goalCenter.z + direction.z * 10
                        );
                    }
                    
                    // Small random offset
                    double offsetX = (Math.random() - 0.5) * 4;
                    double offsetZ = (Math.random() - 0.5) * 4;
                    Vec3 playerSpawn = new Vec3(basePos.x + offsetX, basePos.y, basePos.z + offsetZ);
                    
                    // Respawn Allay mount
                    PlayerMount.dismountPlayer(player);
                    net.minecraft.world.entity.animal.allay.Allay allay = PlayerMount.spawnAndMountAllay(player, playerSpawn, team);
                    if (allay != null) {
                        playerMounts.put(player.getUUID(), allay);
                    }
                    
                    // Give fresh equipment based on role
                    PlayerRole role = playerRoles.get(player.getUUID());
                    if (role != null) {
                        PlayerLoadout.giveEquipment(player, role);
                    }
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(20);
                }
            }
        }
    }
    
    private void score(Team team) {
        int oldScore = scores.get(team);
        scores.put(team, oldScore + 1);
        int newScore = scores.get(team);
        
        // Show updated score with nice formatting
        broadcastMessage(Component.literal(""));  // Blank line
        broadcastMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━")
            .withStyle(ChatFormatting.GOLD));
        broadcastMessage(Component.literal("📊 Score: ")
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            .append(Component.literal(Team.RED.getFullName() + " " + scores.get(Team.RED))
                .withStyle(Team.RED.getColor(), ChatFormatting.BOLD))
            .append(Component.literal(" - ")
                .withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(Team.BLUE.getFullName() + " " + scores.get(Team.BLUE))
                .withStyle(Team.BLUE.getColor(), ChatFormatting.BOLD)));
        broadcastMessage(Component.literal("━━━━━━━━━━━━━━━━━━━━━━")
            .withStyle(ChatFormatting.GOLD));
        broadcastMessage(Component.literal(""));  // Blank line
        
        // Get celebration position (use ball if available, otherwise arena center)
        Vec3 pos = (ball != null && !ball.isRemoved()) ? ball.position() : arena.getCenterPos();
        
        // Play celebration sound
        level.playSound(null, pos.x, pos.y, pos.z, 
            SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0f, 1.0f);
        
        // Spawn massive goal celebration particles
        for (int i = 0; i < 3; i++) {
            minigame.tnt_rocket_leauge.effects.ParticleEffects.spawnGoalExplosion(
                level, pos, team == Team.RED);
        }
        
        // Check for winner
        if (newScore >= POINTS_TO_WIN) {
            broadcastMessage(Component.literal("")); // Empty line
            broadcastMessage(Component.literal("╔══════════════════════════════════╗")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            broadcastMessage(Component.literal("║   " + team.getFullName() + " WIN!   ║")
                .withStyle(team.getColor(), ChatFormatting.BOLD));
            broadcastMessage(Component.literal("║   Final: " + Team.RED.getEmoji() + scores.get(Team.RED) + 
                " - " + scores.get(Team.BLUE) + Team.BLUE.getEmoji() + "   ║")
                .withStyle(ChatFormatting.GOLD));
            broadcastMessage(Component.literal("╚══════════════════════════════════╝")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            
            // START ROTATING BEAM LIGHT SHOW FOR VICTORY!
            startVictoryBeamShow(team);
            
            // PLAY VICTORY CELEBRATION MUSIC!
            Vec3 centerPos = arena.getCenterPos();
            
            // Play music ONLY for Red Team (Blue has jukebox!)
            if (team == Team.RED) {
                level.playSound(null, centerPos.x, centerPos.y, centerPos.z,
                    SoundEvents.MUSIC_DISC_CREATOR.value(), SoundSource.RECORDS, 3.0f, 1.0f);
            }
            // Blue team music comes from the jukebox automatically!
            
            // Mark game as inactive (stop gameplay) but DON'T clean up yet!
            // Let animals dance while music plays!
            active = false;
            
            // Start 1-MINUTE celebration timer (1200 ticks)
            victoryCleanupTimer = 1200;
            
            broadcastMessage(Component.literal("🎊 Game will end in 1 minute... Enjoy the celebration!")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        } else {
            // Continue playing - Remove ball and reset players
            if (ball != null && !ball.isRemoved()) {
                ball.discard();
                GameManager.getInstance().unregisterGameBall(ball);
                ball = null;
            }
            
            // Reset players to spawn positions
            resetPlayersToSpawn();
            
            // Spawn new ball after a short delay
            broadcastMessage(Component.literal("⏳ Next round starting soon...")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC));
            
            // Spawn ball immediately for continuous gameplay
            spawnBall();
        }
    }
    
    private void resetPlayersToSpawn() {
        // Reset all player equipment and teleport to spawn (without ball)
        for (ServerPlayer player : level.players()) {
            if (playerTeams.containsKey(player.getUUID())) {
                Team team = playerTeams.get(player.getUUID());
                
                // Spawn players at their team's spawn area
                Vec3 basePos;
                Vec3 arenaCenter = arena.getCenterPos();
                
                if (team == Team.RED) {
                    // Spawn in front of red goal (toward center)
                    Vec3 goalCenter = arena.getRedGoal().getCenter();
                    Vec3 direction = arenaCenter.subtract(goalCenter).normalize();
                    basePos = new Vec3(
                        goalCenter.x + direction.x * 10,
                        arenaCenter.y,
                        goalCenter.z + direction.z * 10
                    );
                } else {
                    // Spawn in front of blue goal (toward center)
                    Vec3 goalCenter = arena.getBlueGoal().getCenter();
                    Vec3 direction = arenaCenter.subtract(goalCenter).normalize();
                    basePos = new Vec3(
                        goalCenter.x + direction.x * 10,
                        arenaCenter.y,
                        goalCenter.z + direction.z * 10
                    );
                }
                
                // Small random offset
                double offsetX = (Math.random() - 0.5) * 4;
                double offsetZ = (Math.random() - 0.5) * 4;
                Vec3 playerSpawn = new Vec3(basePos.x + offsetX, basePos.y, basePos.z + offsetZ);
                
                // Respawn Allay mount
                PlayerMount.dismountPlayer(player);
                net.minecraft.world.entity.animal.allay.Allay allay = PlayerMount.spawnAndMountAllay(player, playerSpawn, team);
                if (allay != null) {
                    playerMounts.put(player.getUUID(), allay);
                }
                
                // Give fresh equipment based on role
                PlayerRole role = playerRoles.get(player.getUUID());
                if (role != null) {
                    PlayerLoadout.giveEquipment(player, role);
                }
                player.setHealth(player.getMaxHealth());
                player.getFoodData().setFoodLevel(20);
            }
        }
    }
    
    public void addPlayerToTeam(ServerPlayer player, Team team, PlayerRole role) {
        // Check team size limit
        long currentTeamSize = playerTeams.values().stream()
            .filter(t -> t == team)
            .count();
        
        if (currentTeamSize >= gameMode.getMaxPlayersPerTeam()) {
            player.sendSystemMessage(Component.literal("Team " + team.name() + " is full! (" + gameMode.getMaxPlayersPerTeam() + "/" + gameMode.getMaxPlayersPerTeam() + ")")
                .withStyle(ChatFormatting.RED));
            return;
        }
        
        // Remove one armor stand from the team to make room for the player
        List<net.minecraft.world.entity.decoration.ArmorStand> stands = teamArmorStands.get(team);
        if (!stands.isEmpty()) {
            net.minecraft.world.entity.decoration.ArmorStand standToRemove = stands.remove(0);
            standToRemove.discard();
        }
        
        playerTeams.put(player.getUUID(), team);
        playerRoles.put(player.getUUID(), role);
        
        // SKY GAME: Spawn teams HIGH IN THE AIR in front of their goals!
        Vec3 baseSpawnPos;
        Vec3 arenaCenter = arena.getCenterPos(); // Now 45 blocks high!
        
        if (team == Team.RED) {
            // Red team spawns IN FRONT OF red goal (toward center) at sky height
            Vec3 goalCenter = arena.getRedGoal().getCenter();
            // Move 10 blocks toward center from goal AT SKY HEIGHT
            Vec3 direction = arenaCenter.subtract(goalCenter).normalize();
            baseSpawnPos = new Vec3(
                goalCenter.x + direction.x * 10,
                arenaCenter.y, // Use the high center Y position!
                goalCenter.z + direction.z * 10
            );
        } else {
            // Blue team spawns IN FRONT OF blue goal (toward center) at sky height
            Vec3 goalCenter = arena.getBlueGoal().getCenter();
            // Move 10 blocks toward center from goal AT SKY HEIGHT
            Vec3 direction = arenaCenter.subtract(goalCenter).normalize();
            baseSpawnPos = new Vec3(
                goalCenter.x + direction.x * 10,
                arenaCenter.y, // Use the high center Y position!
                goalCenter.z + direction.z * 10
            );
        }
        
        // Small offset per player so they don't stack
        long teamPlayerCount = playerTeams.values().stream()
            .filter(t -> t == team)
            .count();
        
        double angle = (teamPlayerCount * Math.PI * 2) / 4.0;
        double offsetX = Math.cos(angle) * 3;
        double offsetZ = Math.sin(angle) * 3;
        
        Vec3 playerSpawn = new Vec3(baseSpawnPos.x + offsetX, baseSpawnPos.y, baseSpawnPos.z + offsetZ);
        
        // PVP: Set player to 3 hearts (6 health) and survival mode
        player.setHealth(6.0f);
        player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(6.0);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL); // Ensure survival mode for PvP
        
        // Spawn team-specific mounts
        if (team == Team.RED) {
            // RED team rides VEX
            net.minecraft.world.entity.monster.Vex vex = PlayerMount.spawnAndMountVex(player, playerSpawn, team);
            if (vex != null) {
                playerMounts.put(player.getUUID(), vex);
            }
        } else {
            // BLUE team rides ALLAYS
            net.minecraft.world.entity.animal.allay.Allay allay = PlayerMount.spawnAndMountAllay(player, playerSpawn, team);
            if (allay != null) {
                playerMounts.put(player.getUUID(), allay);
            }
        }
        
        // Give player equipment based on role (includes swords for PvP)
        PlayerLoadout.giveEquipment(player, role);
        
        player.sendSystemMessage(Component.literal("You joined the " + team.getFullName() + " as a " + role.getDisplayName() + "!")
            .withStyle(team.getColor(), ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(""));  // Blank line
        player.sendSystemMessage(Component.literal("🎮 Controls:")
            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal("  • WASD to move | SPACE (hold & release) to boost")
            .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("  • XP bar shows boost charge")
            .withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal(""));  // Blank line
        
        if (role == PlayerRole.ATTACKER) {
            player.sendSystemMessage(Component.literal("⚔️ Attacker: Punch TNT or shoot Wind Charges to score!")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        } else {
            player.sendSystemMessage(Component.literal("🛡️ Goalie: Defend your goal! Block with shield!")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        }
        
        // Try to start countdown if this is the first player
        tryStartCountdown();
    }
    
    public void removePlayer(ServerPlayer player) {
        // Dismount and remove Allay
        PlayerMount.dismountPlayer(player);
        playerMounts.remove(player.getUUID());
        playerTeams.remove(player.getUUID());
        playerRoles.remove(player.getUUID());
    }
    
    public void respawnPlayer(ServerPlayer player) {
        // Check if player is in the game
        Team team = playerTeams.get(player.getUUID());
        PlayerRole role = playerRoles.get(player.getUUID());
        
        if (team == null || role == null) {
            return; // Player not in game
        }
        
        // Ensure survival mode for PvP
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        
        // Respawn player with same team and role
        addPlayerToTeam(player, team, role);
        
        player.sendSystemMessage(Component.literal("💀 You died! Respawned in the game!")
            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
    }
    
    public PlayerRole getPlayerRole(UUID playerUUID) {
        return playerRoles.get(playerUUID);
    }
    
    public Team getPlayerTeam(UUID playerUUID) {
        return playerTeams.get(playerUUID);
    }
    
    public Map<UUID, Team> getPlayerTeams() {
        return playerTeams;
    }
    
    public PrimedTnt getBall() {
        return ball;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public Map<UUID, net.minecraft.world.entity.Entity> getPlayerMounts() {
        return playerMounts;
    }
    
    public void broadcastMessage(Component message) {
        for (ServerPlayer player : level.players()) {
            if (playerTeams.containsKey(player.getUUID())) {
                player.sendSystemMessage(message);
            }
        }
    }
    
    public boolean isActive() {
        return active;
    }
    
    public Map<Team, Integer> getScores() {
        return scores;
    }
    
    public void setGameBall(PrimedTnt newBall) {
        // Clean up old ball if it exists
        if (this.ball != null && !this.ball.isRemoved()) {
            GameManager.getInstance().unregisterGameBall(this.ball);
        }
        this.ball = newBall;
    }
    
    public CountdownManager getCountdownManager() {
        return countdownManager;
    }
    
    public boolean isCountdownActive() {
        return countdownManager.isCountdownActive();
    }
    
    public ChaosItemManager getChaosItemManager() {
        return chaosItemManager;
    }
    
    public void activateDispensers() {
        // Activate all dispensers in the arena
        dispenserManager.activateAllDispensers(level, arena.getCorner1(), arena.getCorner2());
    }
    
    public TNTDispenserManager getDispenserManager() {
        return dispenserManager;
    }
    
    public ServingSystem getServingSystem() {
        return servingSystem;
    }
}

