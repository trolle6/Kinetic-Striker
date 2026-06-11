package minigame.tnt_rocket_leauge.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class ParticleEffects {
    
    public static void spawnGoalExplosion(ServerLevel level, Vec3 position, boolean isRedTeam) {
        // Spawn a massive particle explosion for goals
        var particleType = isRedTeam ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
        
        // Create a circular explosion pattern
        for (int i = 0; i < 100; i++) {
            double angle = Math.random() * Math.PI * 2;
            double radius = Math.random() * 3;
            double x = position.x + Math.cos(angle) * radius;
            double y = position.y + Math.random() * 2;
            double z = position.z + Math.sin(angle) * radius;
            
            double vx = Math.cos(angle) * 0.3;
            double vy = Math.random() * 0.5;
            double vz = Math.sin(angle) * 0.3;
            
            level.sendParticles(particleType, x, y, z, 1, vx, vy, vz, 0.1);
        }
        
        // Add some firework particles
        for (int i = 0; i < 50; i++) {
            double x = position.x + (Math.random() - 0.5) * 4;
            double y = position.y + Math.random() * 3;
            double z = position.z + (Math.random() - 0.5) * 4;
            
            level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 1, 
                (Math.random() - 0.5) * 0.5, 
                Math.random() * 0.5, 
                (Math.random() - 0.5) * 0.5, 
                0.1);
        }
    }
    
    public static void spawnBallTrail(ServerLevel level, Vec3 position) {
        // Spawn a subtle trail behind the TNT ball
        level.sendParticles(ParticleTypes.SMOKE, 
            position.x, position.y, position.z, 
            2, 0.1, 0.1, 0.1, 0.01);
        
        level.sendParticles(ParticleTypes.FLAME, 
            position.x, position.y, position.z, 
            1, 0.05, 0.05, 0.05, 0.01);
    }
    
    public static void spawnBoostEffect(ServerLevel level, Vec3 position, Vec3 direction) {
        // Spawn particles when a player boosts with elytra
        for (int i = 0; i < 10; i++) {
            double x = position.x - direction.x * i * 0.3;
            double y = position.y - direction.y * i * 0.3;
            double z = position.z - direction.z * i * 0.3;
            
            level.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 
                (Math.random() - 0.5) * 0.2, 
                (Math.random() - 0.5) * 0.2, 
                (Math.random() - 0.5) * 0.2, 
                0.05);
        }
    }
    
    public static void spawnHitEffect(ServerLevel level, Vec3 position) {
        // Spawn particles when TNT is hit
        level.sendParticles(ParticleTypes.CRIT, 
            position.x, position.y, position.z, 
            15, 0.5, 0.5, 0.5, 0.1);
        
        level.sendParticles(ParticleTypes.EXPLOSION, 
            position.x, position.y, position.z, 
            3, 0.3, 0.3, 0.3, 0.05);
    }
}

