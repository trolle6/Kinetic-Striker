package minigame.tnt_rocket_leauge.game;

public enum GameMode {
    MODE_2V2(2, 60, 40, 20),  // 2 players per team, 60x40 arena, 20 height
    MODE_3V3(3, 80, 60, 30);  // 3 players per team, 80x60 arena, 30 height
    
    private final int maxPlayersPerTeam;
    private final int arenaWidth;
    private final int arenaLength;
    private final int arenaHeight;
    
    GameMode(int maxPlayersPerTeam, int arenaWidth, int arenaLength, int arenaHeight) {
        this.maxPlayersPerTeam = maxPlayersPerTeam;
        this.arenaWidth = arenaWidth;
        this.arenaLength = arenaLength;
        this.arenaHeight = arenaHeight;
    }
    
    public int getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }
    
    public int getArenaWidth() {
        return arenaWidth;
    }
    
    public int getArenaLength() {
        return arenaLength;
    }
    
    public int getArenaHeight() {
        return arenaHeight;
    }
    
    public String getDisplayName() {
        return switch (this) {
            case MODE_2V2 -> "2v2";
            case MODE_3V3 -> "3v3";
        };
    }
}

