package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    
    @Unique
    private double tntrl$lockedY = -1;
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        
        // SKY GAME MODE: Allow full vertical movement!
        // Players can now fly up and down freely on their Allays
        // No Y-locking - this is true aerial gameplay!
        
        if (GameManager.getInstance().isGameActive()) {
            var game = GameManager.getInstance().getCurrentGame();
            if (game != null && game.getPlayerTeam(player.getUUID()) != null) {
                // Zero fall distance to prevent fall damage
                player.fallDistance = 0;
            } else {
                tntrl$lockedY = -1; // Reset when not in game
            }
        } else {
            tntrl$lockedY = -1;
        }
    }
}

