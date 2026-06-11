package minigame.tnt_rocket_leauge.game;

import net.minecraft.ChatFormatting;

public enum Team {
    RED(ChatFormatting.RED, "Red Hoglins", "🐗"),
    BLUE(ChatFormatting.BLUE, "Blue Parrots", "🦜");
    
    private final ChatFormatting color;
    private final String displayName;
    private final String emoji;
    
    Team(ChatFormatting color, String displayName, String emoji) {
        this.color = color;
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    public ChatFormatting getColor() {
        return color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getFullName() {
        return emoji + " " + displayName;
    }
}

