package minigame.tnt_rocket_leauge.mixin;

import minigame.tnt_rocket_leauge.game.ChaosItemManager;
import minigame.tnt_rocket_leauge.game.Game;
import minigame.tnt_rocket_leauge.game.GameManager;
import minigame.tnt_rocket_leauge.game.PlayerRole;
import minigame.tnt_rocket_leauge.game.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(Item.class)
public class ItemUseMixin {
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onItemUse(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(player instanceof ServerPlayer serverPlayer) || level.isClientSide()) {
            return;
        }
        
        Game game = GameManager.getInstance().getCurrentGame();
        if (game == null || game.getPlayerTeam(player.getUUID()) == null) {
            return;
        }
        
        ItemStack stack = player.getItemInHand(hand);
        ChaosItemManager.ChaosItem chaosItem = ChaosItemManager.ChaosItem.fromItem(stack.getItem());
        
        if (chaosItem != null) {
            // Handle chaos item usage
            boolean success = handleChaosItem(chaosItem, serverPlayer, game, (ServerLevel) level);
            
            if (success) {
                // Consume the item
                stack.shrink(1);
            }
            
            // Cancel vanilla behavior
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
    
    private boolean handleChaosItem(ChaosItemManager.ChaosItem chaosItem, ServerPlayer user, Game game, ServerLevel level) {
        return switch (chaosItem) {
            case SPYGLASS -> handleSpyglassSwap(user, game, level);
            case TNT -> handleResetBall(user, game, level);
            case ICE -> handleFreezeEnemy(user, game, level);
            // NEW AERIAL POWER-UPS - placeholders for now, will implement functionality later
            case SHULKER_SHELL -> handleLevitationBoost(user, game, level);
            case END_ROD -> handleAltitudeLock(user, game, level);
            case FIRE_CHARGE -> handleMeteorStrike(user, game, level);
            case FEATHER -> handleUpdraftCurrent(user, game, level);
            case ENDER_PEARL -> handleGravityWell(user, game, level);
            case PHANTOM_MEMBRANE -> handleWingBoost(user, game, level);
            case WIND_CHARGE -> handleAirBlast(user, game, level);
        };
    }
    
    private boolean handleSpyglassSwap(ServerPlayer user, Game game, ServerLevel level) {
        // Raycast to find target player OR armor stand
        Vec3 start = user.getEyePosition();
        Vec3 look = user.getLookAngle();
        
        // Find what we're looking at (player or armor stand)
        Entity targetEntity = null;
        double closestDistance = Double.MAX_VALUE;
        
        // Check players
        for (ServerPlayer player : level.players()) {
            if (player.getUUID().equals(user.getUUID())) continue;
            if (game.getPlayerTeam(player.getUUID()) == null) continue;
            
            // Check if player is in line of sight
            Vec3 playerPos = player.position().add(0, player.getEyeHeight() / 2, 0);
            double distance = playerPos.distanceTo(start);
            
            if (distance < closestDistance && distance < 100) {
                Vec3 toPlayer = playerPos.subtract(start).normalize();
                double dot = toPlayer.dot(look);
                
                if (dot > 0.95) { // Very close to looking direction (about 18 degrees)
                    targetEntity = player;
                    closestDistance = distance;
                }
            }
        }
        
        // Check armor stands too!
        for (Entity entity : level.getAllEntities()) {
            if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) {
                if (!armorStand.isInvulnerable()) continue; // Only game armor stands
                
                Vec3 standPos = armorStand.position().add(0, 1.0, 0); // Eye level of armor stand
                double distance = standPos.distanceTo(start);
                
                if (distance < closestDistance && distance < 100) {
                    Vec3 toStand = standPos.subtract(start).normalize();
                    double dot = toStand.dot(look);
                    
                    if (dot > 0.85) { // More lenient for armor stands (about 32 degrees)
                        targetEntity = armorStand;
                        closestDistance = distance;
                    }
                }
            }
        }
        
        if (targetEntity == null) {
            user.sendSystemMessage(
                Component.literal("❌ No target in sight!").withStyle(ChatFormatting.RED)
            );
            return false;
        }
        
        // Give feedback on what we're targeting
        if (targetEntity instanceof ServerPlayer targetPlayer) {
            user.sendSystemMessage(
                Component.literal("🎯 Targeting: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(targetPlayer.getName().getString()).withStyle(ChatFormatting.YELLOW))
            );
        } else if (targetEntity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) {
            String standName = armorStand.hasCustomName() ? armorStand.getCustomName().getString() : "Armor Stand";
            user.sendSystemMessage(
                Component.literal("🎯 Targeting: ").withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(standName).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" (distance: " + (int)closestDistance + " blocks)").withStyle(ChatFormatting.GRAY))
            );
        }
        
        // Swap positions
        Vec3 userPos = user.position();
        Vec3 targetPos = targetEntity.position();
        
        var userAllay = game.getPlayerMounts().get(user.getUUID());
        
        if (targetEntity instanceof ServerPlayer targetPlayer) {
            // Don't allow swapping with goalies!
            PlayerRole targetRole = game.getPlayerRole(targetPlayer.getUUID());
            if (targetRole == PlayerRole.GOALIE) {
                user.sendSystemMessage(
                    Component.literal("❌ Can't swap with goalies!").withStyle(ChatFormatting.RED)
                );
                return false;
            }
            
            // Swap with player
            var targetAllay = game.getPlayerMounts().get(targetPlayer.getUUID());
            
            if (userAllay != null && targetAllay != null) {
                // Dismount both
                user.stopRiding();
                targetPlayer.stopRiding();
                
                // Swap Allay positions
                Vec3 tempPos = new Vec3(userAllay.getX(), userAllay.getY(), userAllay.getZ());
                userAllay.teleportTo(targetAllay.getX(), targetAllay.getY(), targetAllay.getZ());
                targetAllay.teleportTo(tempPos.x, tempPos.y, tempPos.z);
                
                // Remount
                user.startRiding(userAllay, true, false);
                targetPlayer.startRiding(targetAllay, true, false);
            }
            
            game.broadcastMessage(
                Component.literal("⚡ ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(user.getName().getString()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" swapped with ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(targetPlayer.getName().getString()).withStyle(ChatFormatting.YELLOW))
            );
        } else if (targetEntity instanceof net.minecraft.world.entity.decoration.ArmorStand armorStand) {
            // Swap with armor stand
            if (userAllay != null) {
                user.stopRiding();
                
                // Swap positions
                Vec3 tempPos = new Vec3(userAllay.getX(), userAllay.getY(), userAllay.getZ());
                userAllay.teleportTo(armorStand.getX(), armorStand.getY(), armorStand.getZ());
                armorStand.teleportTo(tempPos.x, tempPos.y, tempPos.z);
                
                // Remount user
                user.startRiding(userAllay, true, false);
            }
            
            String standName = armorStand.hasCustomName() ? armorStand.getCustomName().getString() : "Armor Stand";
            game.broadcastMessage(
                Component.literal("⚡ ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(user.getName().getString()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" swapped with ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(standName).withStyle(ChatFormatting.YELLOW))
            );
        }
        
        // Effects
        level.playSound(null, userPos.x, userPos.y, userPos.z,
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.playSound(null, targetPos.x, targetPos.y, targetPos.z,
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        spawnParticles(level, userPos);
        spawnParticles(level, targetPos);
        
        return true;
    }
    
    private boolean handleResetBall(ServerPlayer user, Game game, ServerLevel level) {
        PrimedTnt ball = game.getBall();
        if (ball == null || ball.isRemoved()) {
            return false;
        }
        
        Vec3 center = game.getArena().getCenterPos();
        double groundY = Math.min(game.getArena().getCorner1().getY(), game.getArena().getCorner2().getY()) + 1;
        Vec3 resetPos = new Vec3(center.x, groundY, center.z);
        
        ball.teleportTo(resetPos.x, resetPos.y, resetPos.z);
        ball.setDeltaMovement(Vec3.ZERO);
        
        // Effects
        level.playSound(null, resetPos.x, resetPos.y, resetPos.z,
            SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0f, 2.0f);
        
        for (int i = 0; i < 30; i++) {
            double offsetX = (new Random().nextDouble() - 0.5) * 3;
            double offsetY = new Random().nextDouble() * 2;
            double offsetZ = (new Random().nextDouble() - 0.5) * 3;
            level.sendParticles(ParticleTypes.FLAME,
                resetPos.x + offsetX, resetPos.y + offsetY, resetPos.z + offsetZ,
                1, 0, 0, 0, 0);
        }
        
        game.broadcastMessage(
            Component.literal("💣 ").withStyle(ChatFormatting.RED)
                .append(Component.literal(user.getName().getString()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" reset the ball!").withStyle(ChatFormatting.GRAY))
        );
        
        return true;
    }
    
    private boolean handleFreezeEnemy(ServerPlayer user, Game game, ServerLevel level) {
        // Get user's team
        Team userTeam = game.getPlayerTeam(user.getUUID());
        if (userTeam == null) {
            return false;
        }
        
        // Get opposite team
        Team enemyTeam = userTeam == Team.RED ? Team.BLUE : Team.RED;
        
        // Get all enemy players (real players only, not armor stands)
        List<ServerPlayer> enemyPlayers = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (game.getPlayerTeam(player.getUUID()) == enemyTeam) {
                enemyPlayers.add(player);
            }
        }
        
        if (enemyPlayers.isEmpty()) {
            user.sendSystemMessage(
                Component.literal("❌ No enemies to freeze!").withStyle(ChatFormatting.RED)
            );
            return false;
        }
        
        // Randomly pick an enemy to freeze
        ServerPlayer victim = enemyPlayers.get(new Random().nextInt(enemyPlayers.size()));
        
        // Freeze them!
        game.getChaosItemManager().freezePlayer(victim.getUUID(), level);
        
        // Effects on victim
        Vec3 victimPos = victim.position();
        level.playSound(null, victimPos.x, victimPos.y, victimPos.z,
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
        
        // Spawn ice particles around victim
        for (int i = 0; i < 50; i++) {
            double offsetX = (new Random().nextDouble() - 0.5) * 1.5;
            double offsetY = new Random().nextDouble() * 2;
            double offsetZ = (new Random().nextDouble() - 0.5) * 1.5;
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                victimPos.x + offsetX, victimPos.y + offsetY, victimPos.z + offsetZ,
                1, 0, 0, 0, 0);
        }
        
        // Broadcast
        game.broadcastMessage(
            Component.literal("❄ ").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
                .append(Component.literal(user.getName().getString()).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" FROZE ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal(victim.getName().getString()).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" for 3 seconds!").withStyle(ChatFormatting.GRAY))
        );
        
        victim.sendSystemMessage(
            Component.literal("❄ You're FROZEN! Can't move for 3 seconds!").withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)
        );
        
        return true;
    }
    
    // NEW AERIAL POWER-UP HANDLERS (placeholders for now)
    
    private boolean handleLevitationBoost(ServerPlayer user, Game game, ServerLevel level) {
        user.sendSystemMessage(Component.literal("⬆ Levitation Boost activated! (Coming soon)").withStyle(ChatFormatting.LIGHT_PURPLE));
        return true;
    }
    
    private boolean handleAltitudeLock(ServerPlayer user, Game game, ServerLevel level) {
        // Locks enemy's vertical movement for 0.5 seconds
        user.sendSystemMessage(Component.literal("🔒 Altitude Lock activated! (Coming soon)").withStyle(ChatFormatting.YELLOW));
        return true;
    }
    
    private boolean handleMeteorStrike(ServerPlayer user, Game game, ServerLevel level) {
        // PVP: Aerial slam attack
        user.sendSystemMessage(Component.literal("☄ Meteor Strike activated! (Coming soon)").withStyle(ChatFormatting.GOLD));
        return true;
    }
    
    private boolean handleUpdraftCurrent(ServerPlayer user, Game game, ServerLevel level) {
        user.sendSystemMessage(Component.literal("🌪 Updraft Current activated! (Coming soon)").withStyle(ChatFormatting.WHITE));
        return true;
    }
    
    private boolean handleGravityWell(ServerPlayer user, Game game, ServerLevel level) {
        user.sendSystemMessage(Component.literal("🌀 Gravity Well activated! (Coming soon)").withStyle(ChatFormatting.DARK_PURPLE));
        return true;
    }
    
    private boolean handleWingBoost(ServerPlayer user, Game game, ServerLevel level) {
        user.sendSystemMessage(Component.literal("👻 Wing Boost activated! (Coming soon)").withStyle(ChatFormatting.GRAY));
        return true;
    }
    
    private boolean handleAirBlast(ServerPlayer user, Game game, ServerLevel level) {
        user.sendSystemMessage(Component.literal("💨 Air Blast activated! (Coming soon)").withStyle(ChatFormatting.AQUA));
        return true;
    }
    
    private void spawnParticles(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 20; i++) {
            double offsetX = (new Random().nextDouble() - 0.5) * 2;
            double offsetY = new Random().nextDouble() * 2;
            double offsetZ = (new Random().nextDouble() - 0.5) * 2;
            level.sendParticles(ParticleTypes.PORTAL,
                pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ,
                1, 0, 0, 0, 0);
        }
    }
}

