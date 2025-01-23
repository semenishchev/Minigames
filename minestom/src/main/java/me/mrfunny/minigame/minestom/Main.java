package me.mrfunny.minigame.minestom;


import me.mrfunny.minigame.balancer.LoadBalancerClient;
import me.mrfunny.minigame.balancer.impl.DebugBalancerClient;
import me.mrfunny.minigame.bedwars.BedwarsDeployment;
import me.mrfunny.minigame.bedwars.BedwarsSetup;
import me.mrfunny.minigame.deployment.info.DebugDeploymentInfo;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.deployment.info.PterodactylDeploymentInfo;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.minestom.instance.BalancedInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventBinding;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setCompressionThreshold(0);
        MinecraftServer.setBrandName("Minigame");
        boolean debug = args.length != 0;
        String setup = System.getProperty("setup");
        if(debug && setup != null) {
            BedwarsSetup.init(args[0], setup);
            minecraftServer.start("0.0.0.0", 25565);
            return;
        }
        DeploymentInfo deploymentInfo = debug ? new DebugDeploymentInfo(args[0]) : new PterodactylDeploymentInfo();
        MinigameDeployment<?> minigame = pickMinigame(deploymentInfo);
        LoadBalancerClient client = new DebugBalancerClient(minigame);
        minigame.setBalancer(client);
        minigame.load();
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            UUID instanceOf = minigame.getInstanceOf(player.getUuid());
            Instance instance;
            if(instanceOf == null || (instance = MinecraftServer.getInstanceManager().getInstance(instanceOf)) == null) {
                player.kick("Instance not found or deregistered");
                client.reportError("Instance not found or deregistered");
                return;
            }
            event.setSpawningInstance(instance);
        });
        minecraftServer.start(deploymentInfo.getServerHost(), deploymentInfo.getServerPort());
    }

    private static MinigameDeployment<?> pickMinigame(DeploymentInfo info) {
        return switch(info.getMinigameType()) {
            case "bedwars" -> new BedwarsDeployment(info);
            default -> throw new IllegalStateException("Unexpected value: " + info.getMinigameType());
        };
    }
}