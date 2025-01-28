package me.mrfunny.minigame.minestom;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.mrfunny.minigame.balancer.LoadBalancerClient;
import me.mrfunny.minigame.balancer.impl.DebugBalancerClient;
import me.mrfunny.minigame.balancer.impl.OlekRedisBalancerClient;
import me.mrfunny.minigame.bedwars.BedwarsDeployment;
import me.mrfunny.minigame.bedwars.setup.BedwarsSetup;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.deployment.info.DebugDeploymentInfo;
import me.mrfunny.minigame.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.deployment.info.PterodactylDeploymentInfo;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.common.serial.PosDeserializer;
import me.mrfunny.minigame.common.serial.PosSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

public class    Main {
    public static DeploymentInfo deploymentInfo;
    public static Logger LOGGER;
    public static ObjectMapper YAML;
    public static void main(String[] args) {
        YAML = new ObjectMapper(new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        ).findAndRegisterModules()
            .enable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        YAML.registerModule(new SimpleModule()
            .addSerializer(Point.class, new PosSerializer())
            .addDeserializer(Point.class, new PosDeserializer())
        );
        MinecraftServer minecraftServer = MinecraftServer.init();
        MinecraftServer.setCompressionThreshold(0);
        MinecraftServer.setBrandName("Minigame");
        boolean debug = false;
        // very hardcoded, but end user isn't using this, so very foolproof anyways
        checker: if(args.length > 0) {
            if(args[0].equals("debug")) {
                debug = true;
                break checker;
            }
            if(args[0].startsWith("setup")) {
                String minigame = args[0].split("=")[1];
                deploymentInfo = new DebugDeploymentInfo(minigame);
                LOGGER = getLogger("setup");
                LOGGER.info("Entering setup: {}", args[0]);
                try {
                    loadSetup(minigame, args);
                } catch(Exception e) {
                    LOGGER.error("Failed entering setup", e);
                    System.exit(1);
                    return;
                }
                minecraftServer.start("0.0.0.0", 25565);
                return;
            }
        }
        DeploymentInfo deploymentInfo = debug ? new DebugDeploymentInfo(args[0]) : new PterodactylDeploymentInfo();
        if(deploymentInfo.getMinigameType() == null) {
            System.err.println("Don't know which minigame to start! Use server");
            MinecraftServer.stopCleanly();
            return;
        }
        LOGGER = getLogger("main");
        MinigameDeployment<?> minigame = pickMinigame(deploymentInfo);
        try {
            LoadBalancerClient client;
            if(debug) {
                client = new DebugBalancerClient(minigame);
                Map<String, String> data = new HashMap<>();
                for (int i = 1; i < args.length; i++) {
                    String[] split = args[i].split("=");
                    data.put(split[0], split[1]);
                }
                minigame.createInstance(data.get("type"), data);
            } else {
                client = new OlekRedisBalancerClient(minigame);
            }
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
                    LOGGER.warn("Instance requested by LB not found or deregistered: {}", instanceOf);
                    return;
                }
                player.setGameMode(GameMode.ADVENTURE);
                event.setSpawningInstance(instance);
            });
            minecraftServer.start(deploymentInfo.getServerHost(), deploymentInfo.getServerPort());
        } catch (Throwable t) {
            minigame.setEncounteredError(t);
            throw t;
        }
    }

    private static MinigameDeployment<?> pickMinigame(DeploymentInfo info) {
        return switch(info.getMinigameType()) {
            case BedwarsStorage.COLLECTION_NAME -> new BedwarsDeployment(info);
            default -> throw new IllegalStateException("Unexpected value: " + info.getMinigameType());
        };
    }

    private static void loadSetup(String minigame, String[] args) throws Exception {
        if(args.length < 2) {
            throw new IllegalStateException("Invalid minigame arguments");
        }
        BedwarsSetup.init(args[1], args.length >= 3 ? args[2] : null);
    }

    public static Logger getLogger(String logger) {
        return LoggerFactory.getLogger(deploymentInfo.getMinigameType() + " " + logger);
    }
}