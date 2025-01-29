package me.mrfunny.minigame.bedwars.data;

import io.github.togar2.pvp.player.CombatPlayer;
import me.mrfunny.minigame.bedwars.instance.BedwarsInstance;
import me.mrfunny.minigame.bedwars.team.BedwarsTeam;
import net.minestom.server.ServerFlag;
import net.minestom.server.collision.Aerodynamics;
import net.minestom.server.collision.PhysicsResult;
import net.minestom.server.collision.PhysicsUtils;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityVelocityEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@SuppressWarnings("UnstableApiUsage")
public class BedwarsPlayerData extends Player implements CombatPlayer {
    private BedwarsTeam memberOf;
    private boolean velocityUpdate = false;
    private PhysicsResult previousPhysicsResult = null;

    public BedwarsPlayerData(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    public BedwarsTeam getBedwarsTeam() {
        return memberOf;
    }

    public void setBedwarsTeam(BedwarsTeam team) {
        BedwarsTeam memberOf = this.memberOf;
        if(memberOf != null) {
            this.memberOf = null;
            memberOf.removeMember(this);
        }
        (this.memberOf = team).addMember(this);
    }

    public void setVelocity(@NotNull Vec velocity) {
        EntityVelocityEvent entityVelocityEvent = new EntityVelocityEvent(this, velocity);
        EventDispatcher.callCancellable(entityVelocityEvent, () -> {
            this.velocity = entityVelocityEvent.getVelocity();
            this.velocityUpdate = true;
        });
    }

    public void setVelocityNoUpdate(Function<Vec, Vec> function) {
        this.velocity = function.apply(this.velocity);
    }

    public void sendImmediateVelocityUpdate() {
        if (this.velocityUpdate) {
            this.velocityUpdate = false;
            this.sendPacketToViewersAndSelf(this.getVelocityPacket());
        }
    }

    @Override
    public CompletableFuture<Void> setInstance(@NotNull Instance instance) {
        if(!(instance instanceof BedwarsInstance bw)) {
            throw new IllegalArgumentException("Instance for a Bedwars player is not a BedwarsInstance");
        }
        setRespawnPoint(bw.getMapConfig().lobbySpawn);
        return super.setInstance(instance);
    }

    protected void movementTick() {
        if(instance.isInVoid(this.position) || this.position.y() <= ((BedwarsInstance) instance).getMapConfig().voidDeathPosition) {
            scheduler().scheduleNextProcess(this::kill);
        }
        this.gravityTickCount = this.onGround ? 0 : this.gravityTickCount + 1;
        if (this.vehicle == null) {
            double tps = (double) ServerFlag.SERVER_TICKS_PER_SECOND;
            Aerodynamics aerodynamics = this.getAerodynamics();
            if (this.velocity.y() < 0.0 && this.hasEffect(PotionEffect.SLOW_FALLING)) {
                aerodynamics = aerodynamics.withGravity(0.01);
            }

            PhysicsResult physicsResult = PhysicsUtils.simulateMovement(this.position, this.velocity.div((double)ServerFlag.SERVER_TICKS_PER_SECOND), this.boundingBox, this.instance.getWorldBorder(), this.instance, aerodynamics, this.hasNoGravity(), this.hasPhysics, this.onGround, this.isFlying(), this.previousPhysicsResult);
            this.previousPhysicsResult = physicsResult;
            Chunk finalChunk = ChunkUtils.retrieve(this.instance, this.currentChunk, physicsResult.newPosition());
            if (ChunkUtils.isLoaded(finalChunk)) {
                this.velocity = physicsResult.newVelocity().mul(tps);
                this.onGround = physicsResult.isOnGround();
                TimedPotion levitation = this.getEffect(PotionEffect.LEVITATION);
                if (levitation != null) {
                    this.velocity = this.velocity.withY((0.05 * (double)(levitation.potion().amplifier() + 1) - this.velocity.y() / tps) * 0.2 * tps);
                }

                this.sendImmediateVelocityUpdate();
            }
        }
    }

    @Override
    public void kill() {
        // todo: start respawn task
        this.setHealth(20);
        this.teleport(this.getRespawnPoint());
        this.sendMessage("uhh suka");
    }
}
