package minigame.tnt_rocket_leauge.game;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Manages tennis-style serving system.
 * After a goal, the opposing team serves by giving a random attacker a TNT item to throw.
 */
public class ServingSystem {
    private static final Random RANDOM = new Random();
    private UUID currentServer = null; // Who is serving
    
    /**
     * Give the serve to a random attacker on the specified team.
     * Called after the opposing team scores.
     */
    public void serveToTeam(Game game, Team servingTeam, ServerLevel level) {
        // Get all attackers on the serving team
        List<ServerPlayer> attackers = new ArrayList<>();
        
        for (ServerPlayer player : level.players()) {
            if (game.getPlayerTeam(player.getUUID()) == servingTeam) {
                PlayerRole role = game.getPlayerRole(player.getUUID());
                if (role == PlayerRole.ATTACKER) {
                    attackers.add(player);
                }
            }
        }
        
        if (attackers.isEmpty()) {
            // No attackers available, use dispenser instead
            currentServer = null;
            return;
        }
        
        // Pick random attacker
        ServerPlayer server = attackers.get(RANDOM.nextInt(attackers.size()));
        currentServer = server.getUUID();
        
        // Give TNT item
        ItemStack tntStack = new ItemStack(Items.TNT, 1);
        server.getInventory().add(tntStack);
        
        // Broadcast who is serving
        game.broadcastMessage(
            Component.literal("🎾 ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal(server.getName().getString()).withStyle(servingTeam.getColor()))
                .append(Component.literal(" is serving!").withStyle(ChatFormatting.GRAY))
        );
        
        // Send message to server
        server.sendSystemMessage(
            Component.literal("🎾 You're serving! Throw the TNT to start!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
        );
        
        // Play sound
        level.playSound(null, server.getX(), server.getY(), server.getZ(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
    
    public UUID getCurrentServer() {
        return currentServer;
    }
    
    public void clearServer() {
        currentServer = null;
    }
}

