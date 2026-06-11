package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import minigame.tnt_rocket_leauge.game.PlayerRole;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class ShieldBlockMixin {
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void onPlayerTick(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        // Only run for goalies actively blocking
        if (!player.isBlocking()) {
            return;
        }
        
        var game = GameManager.getInstance().getCurrentGame();
        if (game == null) {
            return;
        }
        
        PlayerRole role = game.getPlayerRole(serverPlayer.getUUID());
        if (role != PlayerRole.GOALIE) {
            return;
        }
        
        // Check for nearby TNT
        PrimedTnt ball = game.getBall();
        if (ball == null || ball.isRemoved()) {
            return;
        }
        
        // Check if TNT is close to player
        double distance = player.position().distanceTo(ball.position());
        if (distance < 3.0) { // 3 block range
            // Get direction from player to TNT
            Vec3 playerPos = player.position();
            Vec3 tntPos = ball.position();
            Vec3 direction = tntPos.subtract(playerPos).normalize();
            
            // Get player's facing direction
            Vec3 lookVec = player.getLookAngle();
            
            // Check if TNT is in front of player (blocking direction)
            double dot = direction.dot(lookVec);
            if (dot > 0.5) { // TNT is in front
                // Shield knockback - softer push (0.4 blocks/tick = 8 blocks/sec)
                Vec3 knockback = direction.multiply(0.4, 0.2, 0.4); // Gentle horizontal, minimal vertical
                ball.setDeltaMovement(ball.getDeltaMovement().add(knockback));
                
                // Play shield block sound
                player.level().playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
                
                // Play ravager roar sound for effect!
                player.level().playSound(null, tntPos.x, tntPos.y, tntPos.z,
                    SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 0.5f, 1.2f);
                
                // Damage shield slightly (like vanilla)
                player.getUseItem().hurtAndBreak(1, serverPlayer,
                    net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
    }
}

