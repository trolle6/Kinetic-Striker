package minigame.tnt_rocket_leauge.game;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class CountdownManager {
    private int countdownTicks = 0;
    private boolean countdownActive = false;
    private final Game game;
    
    public CountdownManager(Game game) {
        this.game = game;
    }
    
    public void startCountdown() {
        countdownActive = true;
        countdownTicks = 80; // 4 seconds (3, 2, 1, GO)
        
        // Freeze all players (disable Allay movement will be handled in AllayMixin)
        game.broadcastMessage(
            Component.literal("⏱ Game starting soon...")
                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
        );
    }
    
    public void tick(ServerLevel level) {
        if (!countdownActive) {
            return;
        }
        
        countdownTicks--;
        
        // Get all players in the game
        List<ServerPlayer> players = level.players().stream()
            .filter(p -> game.getPlayerTeam(p.getUUID()) != null)
            .toList();
        
        if (countdownTicks == 60) {
            // 1
            showTitle(players, "1", ChatFormatting.RED, level, 1.0f);
        } else if (countdownTicks == 40) {
            // 2
            showTitle(players, "2", ChatFormatting.YELLOW, level, 1.2f);
        } else if (countdownTicks == 20) {
            // 3
            showTitle(players, "3", ChatFormatting.GREEN, level, 1.4f);
        } else if (countdownTicks == 0) {
            // GO!
            showTitle(players, "GO!", ChatFormatting.GOLD, level, 2.0f);
            countdownActive = false;
            
            // Play exciting sound
            for (ServerPlayer player : players) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.MASTER, 0.5f, 2.0f);
            }
            
            // AUTO-ACTIVATE DISPENSERS!
            game.activateDispensers();
            
            game.broadcastMessage(
                Component.literal("🎮 GO! GO! GO!")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
            );
        }
    }
    
    private void showTitle(List<ServerPlayer> players, String text, ChatFormatting color, ServerLevel level, float pitch) {
        Component title = Component.literal(text)
            .withStyle(color, ChatFormatting.BOLD);
        
        for (ServerPlayer player : players) {
            // Set title animation timing (fade in, stay, fade out)
            player.connection.send(new ClientboundSetTitlesAnimationPacket(5, 10, 5));
            
            // Send title
            player.connection.send(new ClientboundSetTitleTextPacket(title));
            
            // Play countdown sound with increasing pitch
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.MASTER, 1.0f, pitch);
        }
    }
    
    public boolean isCountdownActive() {
        return countdownActive;
    }
    
    public int getCountdownTicks() {
        return countdownTicks;
    }
}

