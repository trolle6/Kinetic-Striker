package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.GameManager;
import minigame.tnt_rocket_leauge.mixin.accessor.PlayerJumpAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Allay.class)
public abstract class AllayMixin implements PlayerRideableJumping {
    
    @Unique
    private float tntrl$boostCharge = 0f;
    
    @Unique
    private double tntrl$lockedY = -1;
    
    @Unique
    private int tntrl$boostCooldown = 0;
    
    @Unique
    private boolean tntrl$wasJumping = false;
    
    @Unique
    private int tntrl$boostActiveTicks = 0;
    
    @Unique
    private double tntrl$targetBoostVelocity = 0;
    
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        Allay allay = (Allay) (Object) this;
        
        // Check if a player is riding this Allay in an active game
        if (GameManager.getInstance().isGameActive() && allay.getFirstPassenger() instanceof ServerPlayer rider) {
            
            // Cancel normal Allay AI tick completely
            ci.cancel();
            
            // Lock Y position on first tick
            if (tntrl$lockedY < 0) {
                tntrl$lockedY = allay.position().y;
            }
            
            // Check if countdown is active OR player is frozen - FREEZE PLAYERS!
            var game = GameManager.getInstance().getCurrentGame();
            boolean isFrozen = game != null && game.getChaosItemManager().isPlayerFrozen(rider.getUUID());
            
            if (game != null && (game.isCountdownActive() || isFrozen)) {
                // Zero out all movement during countdown/freeze
                allay.setDeltaMovement(Vec3.ZERO);
                allay.setPos(allay.getX(), tntrl$lockedY, allay.getZ());
                
                // Reset boost charge
                tntrl$boostCharge = 0f;
                tntrl$wasJumping = false;
                
                // Clear XP bar
                rider.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                    0f,
                    rider.totalExperience,
                    rider.experienceLevel
                ));
                
                // Show frozen effect particles
                if (isFrozen && allay.level().getGameTime() % 5 == 0) {
                    // Spawn ice particles every 5 ticks
                    for (int i = 0; i < 3; i++) {
                        double offsetX = (allay.getRandom().nextDouble() - 0.5);
                        double offsetY = allay.getRandom().nextDouble() * 2;
                        double offsetZ = (allay.getRandom().nextDouble() - 0.5);
                        ((net.minecraft.server.level.ServerLevel)allay.level()).sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                            allay.getX() + offsetX, allay.getY() + offsetY, allay.getZ() + offsetZ,
                            1, 0, 0, 0, 0);
                    }
                }
                
                return; // Don't process any movement during countdown/freeze
            }
            
            // Make the Allay respond to player movement input
            float forward = rider.zza; // Forward/backward input
            float strafe = rider.xxa;  // Left/right input
            
            // BOOST MECHANIC - Detect space bar manually
            // Check if player is trying to jump (space bar)
            boolean isJumping = ((PlayerJumpAccessor) rider).tntrl$isJumping();
            
            if (tntrl$boostCooldown > 0) {
                tntrl$boostCooldown--;
            }
            
            if (tntrl$boostActiveTicks > 0) {
                tntrl$boostActiveTicks--;
            }
            
            if (isJumping && tntrl$boostCooldown <= 0) {
                // Charging boost
                tntrl$boostCharge = Math.min(tntrl$boostCharge + 0.02f, 1.0f);
                tntrl$wasJumping = true;
                
                // Update XP bar to show charge (like horse jumping!)
                rider.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                    tntrl$boostCharge,  // Progress bar (0.0 to 1.0)
                    rider.totalExperience,
                    rider.experienceLevel
                ));
            } else if (!isJumping && tntrl$wasJumping) {
                // Released space - BOOST!
                tntrl$wasJumping = false;
                
                if (tntrl$boostCharge > 0.1f) {
                    float boostPower = tntrl$boostCharge;
                    tntrl$boostCharge = 0f;
                    
                    // POWERFUL BOOST - 5x camel power!
                    // Camel dash: 1.1 blocks/tick = 22 blocks/sec
                    // We want: 5.5 blocks/tick = 110 blocks/sec!
                    float pitch = rider.getXRot(); // -90 = up, 0 = forward, 90 = down
                    float yaw = rider.getYRot();
                    
                    // 5x camel power scaled by charge
                    double boostStrength = 5.5 * boostPower;
                    
                    // Calculate boost in the direction player is looking
                    // Vertical component (pitch)
                    double verticalBoost = -Math.sin(Math.toRadians(pitch)) * boostStrength;
                    
                    // Horizontal component (pitch and yaw)
                    double horizontalStrength = Math.cos(Math.toRadians(pitch)) * boostStrength;
                    double horizontalX = -Math.sin(Math.toRadians(yaw)) * horizontalStrength;
                    double horizontalZ = Math.cos(Math.toRadians(yaw)) * horizontalStrength;
                    
                    // Store target velocity for smooth acceleration
                    tntrl$targetBoostVelocity = verticalBoost;
                    
                    // Keep some of current velocity and add boost direction
                    Vec3 currentVel = allay.getDeltaMovement();
                    allay.setDeltaMovement(
                        currentVel.x * 0.3 + horizontalX * 0.7, 
                        verticalBoost, 
                        currentVel.z * 0.3 + horizontalZ * 0.7
                    );
                    
                    // Mark boost as active for 20 ticks (1 second of smooth flight)
                    tntrl$boostActiveTicks = 20;
                    
                    // Unlock Y during boost
                    tntrl$lockedY = -1;
                    
                    // Set cooldown to 1 second (20 ticks)
                    tntrl$boostCooldown = 20;
                    
                    // Play camel dash sound!
                    allay.level().playSound(null, allay.getX(), allay.getY(), allay.getZ(),
                        net.minecraft.sounds.SoundEvents.CAMEL_DASH, 
                        net.minecraft.sounds.SoundSource.PLAYERS, 
                        1.0f, 1.0f); // Camel dash sound
                    
                    // Reset XP bar to normal
                    rider.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                        rider.experienceProgress,
                        rider.totalExperience,
                        rider.experienceLevel
                    ));
                    
                    // Cooldown
                    tntrl$boostCooldown = 40; // 2 seconds
                } else {
                    // Released too early
                    tntrl$boostCharge = 0f;
                    
                    // Reset XP bar
                    rider.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                        rider.experienceProgress,
                        rider.totalExperience,
                        rider.experienceLevel
                    ));
                }
            } else if (!isJumping) {
                // Not jumping and wasn't jumping - reset XP bar if needed
                if (tntrl$wasJumping || tntrl$boostCharge > 0) {
                    tntrl$boostCharge = 0f;
                    rider.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                        rider.experienceProgress,
                        rider.totalExperience,
                        rider.experienceLevel
                    ));
                }
                tntrl$wasJumping = false;
            }
            
            float speedMultiplier = 0.3f; // Base speed
            
            // If boost is active, smooth vertical movement
            if (tntrl$boostActiveTicks > 0) {
                double yVel = tntrl$targetBoostVelocity * (tntrl$boostActiveTicks / 20.0); // Linear decay
                
                // FULL SPEED horizontal control during boost!
                double moveX = 0;
                double moveZ = 0;
                if (forward != 0 || strafe != 0) {
                    float yaw = rider.getYRot();
                    double yawRad = Math.toRadians(yaw);
                    double horizSpeed = speedMultiplier; // SAME speed as normal movement!
                    
                    // Calculate movement direction
                    double dirX = -Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe;
                    double dirZ = Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe;
                    
                    // Normalize and apply full speed
                    double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
                    if (length > 0) {
                        moveX = (dirX / length) * horizSpeed;
                        moveZ = (dirZ / length) * horizSpeed;
                    }
                }
                
                allay.setDeltaMovement(moveX, yVel, moveZ);
                allay.move(net.minecraft.world.entity.MoverType.SELF, allay.getDeltaMovement());
                
                // When boost ends, lock Y at current position
                if (tntrl$boostActiveTicks == 1) {
                    tntrl$lockedY = allay.getY();
                }
            } else {
                // Normal movement (not boosting)
                if (forward != 0 || strafe != 0) {
                    // Get player's look direction
                    float yaw = rider.getYRot();
                    double yawRad = Math.toRadians(yaw);
                    
                    // Calculate movement direction based on input
                    double moveX = -Math.sin(yawRad) * forward + Math.cos(yawRad) * strafe;
                    double moveZ = Math.cos(yawRad) * forward + Math.sin(yawRad) * strafe;
                    
                    // Normalize and apply speed
                    double length = Math.sqrt(moveX * moveX + moveZ * moveZ);
                    if (length > 0) {
                        moveX = (moveX / length) * speedMultiplier;
                        moveZ = (moveZ / length) * speedMultiplier;
                    }
                    
                    // Add to current velocity (for smoother movement)
                    Vec3 currentVel = allay.getDeltaMovement();
                    allay.setDeltaMovement(
                        currentVel.x * 0.8 + moveX * 0.2, 
                        currentVel.y, 
                        currentVel.z * 0.8 + moveZ * 0.2
                    );
                    
                    // Update Allay rotation to match player look direction
                    allay.setYRot(yaw);
                    allay.yRotO = yaw;
                } else {
                    // No input - slow down with friction
                    Vec3 vel = allay.getDeltaMovement();
                    allay.setDeltaMovement(vel.x * 0.85, vel.y, vel.z * 0.85);
                }
                
                // Apply slow falling effect (like levitation/slow falling potion)
                // Slow fall: -0.02 blocks/tick (vs normal gravity of -0.08)
                Vec3 finalVel = allay.getDeltaMovement();
                double slowFallGravity = -0.02;
                allay.setDeltaMovement(finalVel.x, slowFallGravity, finalVel.z);
                
                // Manually handle basic entity ticking (movement only)
                allay.move(net.minecraft.world.entity.MoverType.SELF, allay.getDeltaMovement());
                
                // Update locked Y to current position (so it tracks the slow fall)
                tntrl$lockedY = allay.getY();
            }
        } else {
            // Reset locked Y when not in game
            tntrl$lockedY = -1;
        }
    }
    
    // Implement PlayerRideableJumping for boost mechanic
    @Override
    public void onPlayerJump(int jumpPower) {
        Allay allay = (Allay) (Object) this;
        if (allay.getFirstPassenger() instanceof ServerPlayer rider) {
            // Apply boost when player releases space
            float boostPower = tntrl$boostCharge;
            if (boostPower > 0.1f) {
                tntrl$boostCharge = 0f;
                
                // Apply forward boost based on charge amount
                float yaw = rider.getYRot();
                double yawRad = Math.toRadians(yaw);
                double boostStrength = 1.5 * boostPower; // Max 1.5 blocks/tick
                double boostX = -Math.sin(yawRad) * boostStrength;
                double boostZ = Math.cos(yawRad) * boostStrength;
                
                Vec3 currentVel = allay.getDeltaMovement();
                allay.setDeltaMovement(currentVel.x + boostX, 0, currentVel.z + boostZ);
                
                // Play boost sound
                allay.level().playSound(null, allay.getX(), allay.getY(), allay.getZ(),
                    net.minecraft.sounds.SoundEvents.WIND_CHARGE_THROW, 
                    net.minecraft.sounds.SoundSource.PLAYERS, 
                    1.0f, 1.2f);
                
                // Cooldown before next boost
                tntrl$boostCooldown = 40; // 2 seconds
            }
        }
    }
    
    @Override
    public boolean canJump() {
        return tntrl$boostCooldown <= 0;
    }
    
    @Override
    public void handleStartJump(int jumpPower) {
        // Called every tick while player holds space - charge up boost
        if (tntrl$boostCooldown <= 0) {
            tntrl$boostCharge = Math.min(tntrl$boostCharge + 0.02f, 1.0f);
        }
    }
    
    @Override
    public void handleStopJump() {
        // Called when player releases space without a full charge
        if (tntrl$boostCharge > 0 && tntrl$boostCharge < 0.1f) {
            tntrl$boostCharge = 0f;
        }
    }
    
    @Override
    public int getJumpCooldown() {
        return tntrl$boostCooldown;
    }
}

