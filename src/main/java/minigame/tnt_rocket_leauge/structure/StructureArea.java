package minigame.tnt_rocket_leauge.structure;

import minigame.tnt_rocket_leauge.game.Team;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a defined area within a game structure.
 * Areas have a bounding box, type, optional team, and support collision detection.
 */
public class StructureArea {
    private final String name;
    private final AreaType type;
    private BlockPos corner1;
    private BlockPos corner2;
    private Team team; // Optional - only for team-specific areas
    private AABB boundingBox;
    
    public StructureArea(String name, AreaType type) {
        this.name = name;
        this.type = type;
        this.team = null;
    }
    
    public StructureArea(String name, AreaType type, Team team) {
        this.name = name;
        this.type = type;
        this.team = team;
    }
    
    public void setCorner1(BlockPos pos) {
        this.corner1 = pos;
        updateBoundingBox();
    }
    
    public void setCorner2(BlockPos pos) {
        this.corner2 = pos;
        updateBoundingBox();
    }
    
    private void updateBoundingBox() {
        if (corner1 != null && corner2 != null) {
            this.boundingBox = AABB.encapsulatingFullBlocks(corner1, corner2);
        }
    }
    
    public boolean isComplete() {
        return corner1 != null && corner2 != null;
    }
    
    public boolean contains(Vec3 position) {
        return boundingBox != null && boundingBox.contains(position);
    }
    
    public boolean contains(BlockPos position) {
        return contains(Vec3.atCenterOf(position));
    }
    
    public Vec3 getCenterPos() {
        if (boundingBox == null) {
            return Vec3.ZERO;
        }
        return boundingBox.getCenter();
    }
    
    // NBT Serialization
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name", name);
        tag.putString("type", type.name());
        
        if (corner1 != null) {
            tag.putLong("corner1", corner1.asLong());
        }
        if (corner2 != null) {
            tag.putLong("corner2", corner2.asLong());
        }
        if (team != null) {
            tag.putString("team", team.name());
        }
        
        return tag;
    }
    
    public static StructureArea fromNBT(CompoundTag tag) {
        String name = tag.getString("name").orElse("");
        AreaType type = AreaType.valueOf(tag.getString("type").orElse(AreaType.PLAYING_AREA.name()));
        
        StructureArea area = new StructureArea(name, type);
        
        tag.getLong("corner1").ifPresent(corner -> area.setCorner1(BlockPos.of(corner)));
        tag.getLong("corner2").ifPresent(corner -> area.setCorner2(BlockPos.of(corner)));
        tag.getString("team").ifPresent(teamName -> area.team = Team.valueOf(teamName));
        
        return area;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public AreaType getType() {
        return type;
    }
    
    public BlockPos getCorner1() {
        return corner1;
    }
    
    public BlockPos getCorner2() {
        return corner2;
    }
    
    public Team getTeam() {
        return team;
    }
    
    public void setTeam(Team team) {
        this.team = team;
    }
    
    public AABB getBoundingBox() {
        return boundingBox;
    }
}

