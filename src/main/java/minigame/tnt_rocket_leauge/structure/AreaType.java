package minigame.tnt_rocket_leauge.structure;

import net.minecraft.ChatFormatting;

/**
 * Defines different types of areas in a game structure.
 * Each type has a specific purpose and color coding for visualization.
 */
public enum AreaType {
    PLAYING_AREA("Playing Area", ChatFormatting.GREEN, 0x00FF00),
    TEAM_WAITING("Team Waiting Room", ChatFormatting.YELLOW, 0xFFFF00),
    SPAWN_POINT("Spawn Point", ChatFormatting.AQUA, 0x00FFFF),
    GOAL_ZONE("Goal Zone", ChatFormatting.RED, 0xFF0000),
    VICTORY_PODIUM("Victory Podium", ChatFormatting.GOLD, 0xFFD700),
    SPECTATOR_AREA("Spectator Area", ChatFormatting.GRAY, 0x808080),
    SAFE_ZONE("Safe Zone", ChatFormatting.BLUE, 0x0000FF),
    LOBBY("Lobby", ChatFormatting.WHITE, 0xFFFFFF);
    
    private final String displayName;
    private final ChatFormatting chatColor;
    private final int visualColor; // RGB color for rendering
    
    AreaType(String displayName, ChatFormatting chatColor, int visualColor) {
        this.displayName = displayName;
        this.chatColor = chatColor;
        this.visualColor = visualColor;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatFormatting getChatColor() {
        return chatColor;
    }
    
    public int getVisualColor() {
        return visualColor;
    }
    
    // Get RGB components for rendering
    public float getRed() {
        return ((visualColor >> 16) & 0xFF) / 255.0f;
    }
    
    public float getGreen() {
        return ((visualColor >> 8) & 0xFF) / 255.0f;
    }
    
    public float getBlue() {
        return (visualColor & 0xFF) / 255.0f;
    }
}

