package minigame.tnt_rocket_leauge.game;

import net.minecraft.ChatFormatting;

public enum PlayerRole {
    ATTACKER("Attacker", ChatFormatting.GOLD),
    GOALIE("Goalie", ChatFormatting.LIGHT_PURPLE);
    
    private final String displayName;
    private final ChatFormatting color;
    
    PlayerRole(String displayName, ChatFormatting color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatFormatting getColor() {
        return color;
    }
}

