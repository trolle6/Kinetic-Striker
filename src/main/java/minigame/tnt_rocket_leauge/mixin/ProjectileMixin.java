package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.Game;
import minigame.tnt_rocket_leauge.game.GameManager;
import minigame.tnt_rocket_leauge.game.Team;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public class ProjectileMixin {
    
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void onProjectileHit(EntityHitResult result, CallbackInfo ci) {
        Projectile projectile = (Projectile) (Object) this;
        Entity hit = result.getEntity();
        
        // Check if we hit a game ball
        if (hit instanceof PrimedTnt tnt && GameManager.getInstance().isGameBall(tnt)) {
            Entity owner = projectile.getOwner();
            
            // Only track if shot by a player
            if (owner instanceof ServerPlayer serverPlayer) {
                Game game = GameManager.getInstance().getCurrentGame();
                if (game != null) {
                    Team team = game.getPlayerTeam(serverPlayer.getUUID());
                    if (team != null) {
                        // Record the hit!
                        GameManager.getInstance().recordBallHit(tnt, serverPlayer.getUUID(), team);
                    }
                }
            }
            
            // Apply ball velocity with skill-based amplification
            Vec3 currentVel = tnt.getDeltaMovement();
            Vec3 projectileVel = projectile.getDeltaMovement();
            
            // Much reduced amplification for more skill-based gameplay
            // Wind charges: 0.8x (was 1.5x - requires much more skill and positioning!)
            double amplification = 0.8;
            double newX = currentVel.x + projectileVel.x * amplification;
            double newZ = currentVel.z + projectileVel.z * amplification;
            
            // Keep Y component natural (combine velocities normally)
            double newY = currentVel.y + projectileVel.y * 0.5;
            
            tnt.setDeltaMovement(newX, newY, newZ);
        }
    }
    
    private String getProjectileType(Projectile projectile) {
        if (projectile instanceof WindCharge) {
            return "Wind Charge";
        } else if (projectile instanceof AbstractArrow) {
            return "Arrow";
        } else if (projectile instanceof Snowball) {
            return "Snowball";
        } else if (projectile instanceof ThrownEgg) {
            return "Egg";
        } else {
            return "Projectile";
        }
    }
}

