package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {
    
    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        
        if (target instanceof PrimedTnt tnt && GameManager.getInstance().isGameBall(tnt)) {
            // Melee hits don't count for scoring, just visual feedback
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                minigame.tnt_rocket_leauge.effects.ParticleEffects.spawnHitEffect(
                    serverLevel, tnt.position());
            }
            
            // AMPLIFY melee hit velocity for fast curling puck physics!
            Vec3 currentVel = tnt.getDeltaMovement();
            Vec3 playerVel = player.getDeltaMovement();
            
            // Get direction from player to TNT
            Vec3 direction = tnt.position().subtract(player.position()).normalize();
            
            // Apply strong melee knockback (2.0 blocks/tick base + player momentum)
            double meleePower = 2.0;
            double newX = currentVel.x + (direction.x * meleePower) + (playerVel.x * 1.5);
            double newZ = currentVel.z + (direction.z * meleePower) + (playerVel.z * 1.5);
            double newY = currentVel.y + (direction.y * 0.5) + (playerVel.y * 0.5);
            
            tnt.setDeltaMovement(newX, newY, newZ);
        }
    }
}

