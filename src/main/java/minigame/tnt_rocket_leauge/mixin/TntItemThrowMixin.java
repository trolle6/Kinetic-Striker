package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.PrimedTnt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Detects when a player throws/uses a TNT item during serving.
 * Converts the thrown TNT into a game ball.
 */
@Mixin(PrimedTnt.class)
public class TntItemThrowMixin {
    
    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/entity/LivingEntity;)V", at = @At("TAIL"))
    private void onTntPlaced(CallbackInfo ci) {
        PrimedTnt tnt = (PrimedTnt) (Object) this;
        
        if (!(tnt.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        
        var game = GameManager.getInstance().getCurrentGame();
        if (game == null || !game.isActive()) {
            return;
        }
        
        // Check if this was placed/thrown by the current server
        var servingSystem = game.getServingSystem();
        if (tnt.getOwner() instanceof ServerPlayer player) {
            if (player.getUUID().equals(servingSystem.getCurrentServer())) {
                // This is a serve! Convert to game ball
                // NO GRAVITY - floating ball!
                tnt.setNoGravity(true);
                tnt.setFuse(Integer.MAX_VALUE); // Infinite fuse
                tnt.setDeltaMovement(tnt.getDeltaMovement().multiply(0.5, 0.5, 0.5)); // Reduce initial velocity
                
                // Register as game ball
                GameManager.getInstance().registerGameBall(tnt);
                game.setGameBall(tnt);
                
                // Clear server
                servingSystem.clearServer();
                
                game.broadcastMessage(
                    net.minecraft.network.chat.Component.literal("🎾 Serve! ")
                        .withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD)
                        .append(net.minecraft.network.chat.Component.literal(player.getName().getString())
                            .withStyle(net.minecraft.ChatFormatting.YELLOW))
                );
            }
        }
    }
}

