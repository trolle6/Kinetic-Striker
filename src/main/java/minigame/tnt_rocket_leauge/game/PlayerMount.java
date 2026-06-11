package minigame.tnt_rocket_leauge.game;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.phys.Vec3;

public class PlayerMount {
    
    public static Allay spawnAndMountAllay(ServerPlayer player, Vec3 position, Team team) {
        ServerLevel level = (ServerLevel) player.level();
        
        // Spawn an Allay
        Allay allay = (Allay) EntityType.ALLAY.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (allay == null) return null;
        
        // Position the Allay
        allay.setPos(position.x, position.y, position.z);
        allay.setNoGravity(true);
        allay.setInvulnerable(true);
        allay.setPersistenceRequired();
        allay.setCanPickUpLoot(false);
        
        // DISABLE ALL AI - Make it completely static
        allay.setNoAi(true);
        
        // Set custom name with team color
        allay.setCustomName(net.minecraft.network.chat.Component.literal("⚡ " + player.getName().getString())
            .withStyle(team.getColor()));
        allay.setCustomNameVisible(true);
        
        // Add to world
        level.addFreshEntity(allay);
        
        // Mount player on Allay
        player.startRiding(allay, true, false);
        
        return allay;
    }
    
    public static Vex spawnAndMountVex(ServerPlayer player, Vec3 position, Team team) {
        ServerLevel level = (ServerLevel) player.level();
        
        // Spawn a Vex
        Vex vex = (Vex) EntityType.VEX.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (vex == null) return null;
        
        // Position the Vex
        vex.setPos(position.x, position.y, position.z);
        vex.setNoGravity(true);
        vex.setInvulnerable(true);
        vex.setPersistenceRequired();
        
        // DISABLE ALL AI - Make it completely static
        vex.setNoAi(true);
        
        // Set custom name with team color
        vex.setCustomName(net.minecraft.network.chat.Component.literal("⚡ " + player.getName().getString())
            .withStyle(team.getColor()));
        vex.setCustomNameVisible(true);
        
        // Add to world
        level.addFreshEntity(vex);
        
        // Mount player on Vex
        player.startRiding(vex, true, false);
        
        return vex;
    }
    
    public static void dismountPlayer(ServerPlayer player) {
        if (player.getVehicle() instanceof Allay allay) {
            player.stopRiding();
            allay.discard();
        } else if (player.getVehicle() instanceof Vex vex) {
            player.stopRiding();
            vex.discard();
        }
    }
}

