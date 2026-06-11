package minigame.tnt_rocket_leauge.game;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChaosItemManager {
    private static final Random RANDOM = new Random();
    private static final int DROP_INTERVAL = 200; // 10 seconds (20 ticks/sec)
    
    private java.util.Map<java.util.UUID, Long> frozenPlayers = new java.util.HashMap<>(); // UUID -> unfreeze time (game time)
    
    public enum ChaosItem {
        // Original items
        SPYGLASS(Items.SPYGLASS, "Position Swap", ChatFormatting.AQUA),
        TNT(Items.TNT, "Reset Ball", ChatFormatting.RED),
        ICE(Items.ICE, "Freeze Enemy", ChatFormatting.DARK_AQUA),
        
        // NEW AERIAL POWER-UPS for sky gameplay!
        SHULKER_SHELL(Items.SHULKER_SHELL, "Levitation Boost", ChatFormatting.LIGHT_PURPLE),
        END_ROD(Items.END_ROD, "Altitude Lock", ChatFormatting.YELLOW), // Freezes enemy vertical movement for 0.5s
        FIRE_CHARGE(Items.FIRE_CHARGE, "Meteor Strike", ChatFormatting.GOLD), // PVP: Aerial slam attack
        FEATHER(Items.FEATHER, "Updraft Current", ChatFormatting.WHITE),
        ENDER_PEARL(Items.ENDER_PEARL, "Gravity Well", ChatFormatting.DARK_PURPLE),
        PHANTOM_MEMBRANE(Items.PHANTOM_MEMBRANE, "Wing Boost", ChatFormatting.GRAY),
        WIND_CHARGE(Items.WIND_CHARGE, "Air Blast", ChatFormatting.AQUA);
        
        private final net.minecraft.world.item.Item item;
        private final String displayName;
        private final ChatFormatting color;
        
        ChaosItem(net.minecraft.world.item.Item item, String displayName, ChatFormatting color) {
            this.item = item;
            this.displayName = displayName;
            this.color = color;
        }
        
        public net.minecraft.world.item.Item getItem() {
            return item;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public ChatFormatting getColor() {
            return color;
        }
        
        public static ChaosItem fromItem(net.minecraft.world.item.Item item) {
            for (ChaosItem chaosItem : values()) {
                if (chaosItem.item == item) {
                    return chaosItem;
                }
            }
            return null;
        }
    }
    
    private int tickCounter = 0;
    
    public void tick(Game game, ServerLevel level) {
        tickCounter++;
        
        // Update frozen players - unfreeze those whose time has expired
        long currentTime = level.getGameTime();
        frozenPlayers.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                // Player unfrozen!
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    player.sendSystemMessage(
                        Component.literal("❄ You're unfrozen!").withStyle(ChatFormatting.AQUA)
                    );
                }
                return true;
            }
            return false;
        });
        
        if (tickCounter >= DROP_INTERVAL) {
            tickCounter = 0;
            dropRandomItem(game, level);
        }
    }
    
    private void dropRandomItem(Game game, ServerLevel level) {
        // Get all active players in the game, grouped by team
        List<ServerPlayer> redPlayers = new ArrayList<>();
        List<ServerPlayer> bluePlayers = new ArrayList<>();
        
        for (ServerPlayer player : level.players()) {
            Team team = game.getPlayerTeam(player.getUUID());
            if (team == Team.RED) {
                redPlayers.add(player);
            } else if (team == Team.BLUE) {
                bluePlayers.add(player);
            }
        }
        
        if (redPlayers.isEmpty() && bluePlayers.isEmpty()) {
            return;
        }
        
        // Check if any team already has chaos items - only one player per team can hold items
        boolean redHasItems = redPlayers.stream().anyMatch(this::playerHasChaosItem);
        boolean blueHasItems = bluePlayers.stream().anyMatch(this::playerHasChaosItem);
        
        // Pick a team that doesn't have items
        List<ServerPlayer> eligiblePlayers = new ArrayList<>();
        if (!redHasItems && !redPlayers.isEmpty()) {
            eligiblePlayers.addAll(redPlayers);
        }
        if (!blueHasItems && !bluePlayers.isEmpty()) {
            eligiblePlayers.addAll(bluePlayers);
        }
        
        if (eligiblePlayers.isEmpty()) {
            return; // Both teams have items, wait until they use them
        }
        
        // Pick random player from eligible players
        ServerPlayer luckyPlayer = eligiblePlayers.get(RANDOM.nextInt(eligiblePlayers.size()));
        
        // Pick random chaos item
        ChaosItem chaosItem = ChaosItem.values()[RANDOM.nextInt(ChaosItem.values().length)];
        
        // Give item
        ItemStack stack = new ItemStack(chaosItem.getItem(), 1);
        luckyPlayer.getInventory().add(stack);
        
        // Announce
        game.broadcastMessage(Component.literal("⚡ CHAOS ITEM! ")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
            .append(Component.literal(luckyPlayer.getName().getString())
                .withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" received ")
                .withStyle(ChatFormatting.GRAY))
            .append(Component.literal(chaosItem.getDisplayName())
                .withStyle(chaosItem.getColor())));
        
        // Effects
        level.playSound(null, luckyPlayer.getX(), luckyPlayer.getY(), luckyPlayer.getZ(),
            SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.5f);
        
        // Spawn particles around player
        for (int i = 0; i < 20; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 2;
            double offsetY = RANDOM.nextDouble() * 2;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 2;
            level.sendParticles(ParticleTypes.ENCHANT,
                luckyPlayer.getX() + offsetX,
                luckyPlayer.getY() + offsetY,
                luckyPlayer.getZ() + offsetZ,
                1, 0, 0, 0, 0);
        }
    }
    
    public void reset() {
        tickCounter = 0;
        frozenPlayers.clear();
    }
    
    private boolean playerHasChaosItem(ServerPlayer player) {
        for (ChaosItem chaosItem : ChaosItem.values()) {
            if (player.getInventory().contains(new ItemStack(chaosItem.getItem()))) {
                return true;
            }
        }
        return false;
    }
    
    public void freezePlayer(java.util.UUID playerUUID, ServerLevel level) {
        long unfreezeTime = level.getGameTime() + 60; // 3 seconds (20 ticks/sec)
        frozenPlayers.put(playerUUID, unfreezeTime);
    }
    
    public boolean isPlayerFrozen(java.util.UUID playerUUID) {
        return frozenPlayers.containsKey(playerUUID);
    }
}

