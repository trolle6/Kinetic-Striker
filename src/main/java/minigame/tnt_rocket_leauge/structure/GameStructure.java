package minigame.tnt_rocket_leauge.structure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import minigame.tnt_rocket_leauge.game.Team;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.*;

/**
 * Container for a complete game structure with multiple defined areas.
 * Supports serialization to both NBT (for world data) and JSON (for sharing).
 */
public class GameStructure {
    private final String id;
    private String name;
    private final Map<String, StructureArea> areas;
    
    public GameStructure(String id, String name) {
        this.id = id;
        this.name = name;
        this.areas = new HashMap<>();
    }
    
    public void addArea(StructureArea area) {
        areas.put(area.getName(), area);
    }
    
    public void removeArea(String areaName) {
        areas.remove(areaName);
    }
    
    public StructureArea getArea(String areaName) {
        return areas.get(areaName);
    }
    
    public Collection<StructureArea> getAllAreas() {
        return areas.values();
    }
    
    public List<StructureArea> getAreasByType(AreaType type) {
        return areas.values().stream()
            .filter(area -> area.getType() == type)
            .toList();
    }
    
    public List<StructureArea> getAreasByTeam(Team team) {
        return areas.values().stream()
            .filter(area -> area.getTeam() == team)
            .toList();
    }
    
    public StructureArea getGoalForTeam(Team team) {
        // Get the opponent's goal (where this team scores)
        Team opponentTeam = team == Team.RED ? Team.BLUE : Team.RED;
        return areas.values().stream()
            .filter(area -> area.getType() == AreaType.GOAL_ZONE)
            .filter(area -> area.getTeam() == opponentTeam)
            .findFirst()
            .orElse(null);
    }
    
    public boolean isComplete() {
        // Check if all areas have both corners set
        return areas.values().stream().allMatch(StructureArea::isComplete);
    }
    
    // NBT Serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("name", name);
        
        ListTag areasList = new ListTag();
        for (StructureArea area : areas.values()) {
            areasList.add(area.toNBT());
        }
        tag.put("areas", areasList);
        
        return tag;
    }
    
    public static GameStructure fromNBT(CompoundTag tag) {
        String id = tag.getString("id").orElse("");
        String name = tag.getString("name").orElse("");
        
        GameStructure structure = new GameStructure(id, name);
        
        ListTag areasList = tag.getListOrEmpty("areas");
        for (int i = 0; i < areasList.size(); i++) {
            CompoundTag areaTag = areasList.getCompoundOrEmpty(i);
            StructureArea area = StructureArea.fromNBT(areaTag);
            structure.addArea(area);
        }
        
        return structure;
    }
    
    // JSON Serialization (for sharing structures)
    public String toJSON() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
    
    public static GameStructure fromJSON(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, GameStructure.class);
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getAreaCount() {
        return areas.size();
    }
}

