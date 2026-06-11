package minigame.tnt_rocket_leauge.game;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Arena {
    private final BlockPos corner1;
    private final BlockPos corner2;
    private final AABB redGoal;
    private final AABB blueGoal;
    private final Vec3 centerPos;
    
    public Arena(BlockPos corner1, BlockPos corner2, AABB redGoal, AABB blueGoal) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.redGoal = redGoal;
        this.blueGoal = blueGoal;
        
        // Calculate center position - at play area height (can be raised later when building sky arenas!)
        double centerX = (corner1.getX() + corner2.getX()) / 2.0;
        double centerY = Math.min(corner1.getY(), corner2.getY()) + 10; // 10 blocks above floor - matches play area
        double centerZ = (corner1.getZ() + corner2.getZ()) / 2.0;
        this.centerPos = new Vec3(centerX, centerY, centerZ);
    }
    
    public AABB getBounds() {
        return AABB.encapsulatingFullBlocks(corner1, corner2);
    }
    
    public AABB getRedGoal() {
        return redGoal;
    }
    
    public AABB getBlueGoal() {
        return blueGoal;
    }
    
    public Vec3 getCenterPos() {
        return centerPos;
    }
    
    public BlockPos getCorner1() {
        return corner1;
    }
    
    public BlockPos getCorner2() {
        return corner2;
    }
}

