package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PrimedTnt.class)
public class DispenserTntMixin {
    
    @Unique
    private boolean tntrl$fromDispenser = false;
    
    @Unique
    private boolean tntrl$converted = false;
    
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/entity/LivingEntity;)V", at = @At("TAIL"))
    private void onSpawn(CallbackInfo ci) {
        // Check if there's an active game
        if (GameManager.getInstance().isGameActive()) {
            // Mark as potentially from dispenser (we'll verify in tick)
            tntrl$fromDispenser = true;
        }
    }
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onDispenserTntTick(CallbackInfo ci) {
        PrimedTnt tnt = (PrimedTnt) (Object) this;
        
        // Only process dispenser TNT that hasn't been converted yet
        if (!tntrl$fromDispenser || tntrl$converted) {
            return;
        }
        
        // Check if this is already a game ball
        if (GameManager.getInstance().isGameBall(tnt)) {
            tntrl$converted = true;
            return;
        }
        
        // IMMEDIATELY convert on first tick to prevent vanilla TNT from existing
        if (tnt.level() instanceof ServerLevel serverLevel && tnt.tickCount <= 1) {
            Vec3 pos = tnt.position();
            
            // KILL THIS VANILLA TNT IMMEDIATELY
            tnt.discard();
            ci.cancel(); // Cancel the rest of the tick
            
            // Spawn game ball BELOW dispenser - ECHO VR STYLE (zero gravity!)
            var game = GameManager.getInstance().getCurrentGame();
            if (game != null) {
                // Spawn 2 blocks below to clear the dispenser block
                double spawnY = pos.y - 2.0;
                
                PrimedTnt gameBall = new PrimedTnt(
                    serverLevel,
                    pos.x, spawnY, pos.z,
                    null
                );
                // ECHO VR DISC: ZERO GRAVITY - just floats in mid-air!
                gameBall.setNoGravity(true);
                gameBall.setDeltaMovement(Vec3.ZERO);
                gameBall.setFuse(Integer.MAX_VALUE); // Infinite fuse
                serverLevel.addFreshEntity(gameBall);
                
                // Register as game ball
                GameManager.getInstance().registerGameBall(gameBall);
                game.setGameBall(gameBall);
            }
            
            tntrl$converted = true;
        }
    }
}

