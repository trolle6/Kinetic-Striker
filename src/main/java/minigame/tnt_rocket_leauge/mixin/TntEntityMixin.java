package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public class TntEntityMixin {
    
    // Keep the fuse at max time so it never explodes (vanilla behavior, just infinite fuse)
    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        PrimedTnt tnt = (PrimedTnt) (Object) this;
        
        if (GameManager.getInstance().isGameBall(tnt)) {
            // Keep fuse at max - NO EXPLOSION
            tnt.setFuse(Integer.MAX_VALUE);
            
            // SUPER SLIPPERY DISC PHYSICS (Echo VR style!)
            Vec3 currentVel = tnt.getDeltaMovement();
            
            // Apply slow falling effect (vertical)
            double slowFallGravity = -0.02;
            
            // Apply VERY low friction for fast gliding disc physics!
            // 0.998 = loses only 0.2% per tick = extremely slippery!
            double iceFriction = 0.998;  // Super slippery disc!
            
            // Apply friction to horizontal velocities
            double newX = currentVel.x * iceFriction;
            double newZ = currentVel.z * iceFriction;
            
            // Set new velocity with ice physics!
            tnt.setDeltaMovement(newX, slowFallGravity, newZ);
            
            // Let GameManager know the ball ticked (for particles, goal detection, etc.)
            GameManager.getInstance().tickGameBall(tnt);
        }
    }
    
    // Safety: prevent explosion if it somehow gets triggered
    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        PrimedTnt tnt = (PrimedTnt) (Object) this;
        
        if (GameManager.getInstance().isGameBall(tnt)) {
            ci.cancel();
        }
    }
}

