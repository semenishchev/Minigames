package me.mrfunny.minigame.minestom;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import me.mrfunny.minigame.api.balancer.LoadBalancerClient;
import me.mrfunny.minigame.api.balancer.impl.DebugBalancerClient;
import me.mrfunny.minigame.api.balancer.impl.OlekRedisBalancerClient;
import me.mrfunny.minigame.bedwars.BedwarsDeployment;
import me.mrfunny.minigame.bedwars.setup.BedwarsSetup;
import me.mrfunny.minigame.bedwars.instance.BedwarsStorage;
import me.mrfunny.minigame.api.deployment.info.DebugDeploymentInfo;
import me.mrfunny.minigame.api.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.api.deployment.info.K8SDeploymentInfo;
import me.mrfunny.minigame.minestom.deployment.MinigameDeployment;
import me.mrfunny.minigame.common.serial.PosDeserializer;
import me.mrfunny.minigame.common.serial.PosSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static DeploymentInfo deploymentInfo;
    public static Logger LOGGER;
    public static ObjectMapper YAML;
    public static void main(String[] args) {
        ForkJoinPool.commonPool().setParallelism(2);
        System.setProperty("minestom.dispatcher-threads", "4");
        LOGGER = LoggerFactory.getLogger("bootstrap");
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
        boolean debug = false;
        // very hardcoded, but end user isn't using this, so very foolproof anyways
        checker: if(args.length > 0) {
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
            } else {
                debug = true;
            }
        }
        DeploymentInfo deploymentInfo = debug ? new DebugDeploymentInfo(args[0]) : getCloudDeployment();
        if(deploymentInfo.getMinigameType() == null) {
            LOGGER.error("Don't know which minigame to start! Use server");
            MinecraftServer.stopCleanly();
            return;
        }
        MinecraftServer.setBrandName(deploymentInfo.getMinigameType());
        Main.deploymentInfo = deploymentInfo;
        LOGGER = getLogger("main");
        MinigameDeployment<?> minigame = pickMinigame(deploymentInfo);
        Map<String, String> data = new HashMap<>();
        try {
            LoadBalancerClient client;
            if(debug) {
                client = new DebugBalancerClient(minigame);
                for (int i = 1; i < args.length; i++) {
                    String[] split = args[i].split("=");
                    data.put(split[0], split[1]);
                }
            } else {
                client = new OlekRedisBalancerClient(minigame);
            }
            minigame.setBalancer(client);
            minigame.load();
            GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
            if(debug) {
                UUID id = minigame.createInstance(data.get("type"), data);
                globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                    event.setSpawningInstance(MinecraftServer.getInstanceManager().getInstance(id));
                });
            } else {
                VelocityProxy.enable(System.getenv("VELOCITY_SECRET"));
                globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
                    final Player player = event.getPlayer();
                    Instance instance = minigame.getAssignedInstance(player);
                    if(instance != null) {
                        event.setSpawningInstance(instance);
                        return;
                    }
                    UUID instanceOf = minigame.getInstanceOf(player.getUuid());
                    if(instanceOf == null || (instance = MinecraftServer.getInstanceManager().getInstance(instanceOf)) == null) {
                        player.kick("Instance not found or deregistered");
                        client.reportError("Instance not found or deregistered");
                        LOGGER.warn("Instance requested by LB not found or deregistered: {}", instanceOf);
                        return;
                    }
                    event.setSpawningInstance(instance);
                });
            }
            minecraftServer.start(deploymentInfo.getServerHost(), deploymentInfo.getServerPort());
            ForkJoinPool.commonPool().setParallelism(2);
        } catch (Throwable t) {
            minigame.setEncounteredError(t);
            LOGGER.error("Failed to load", t);
            MinecraftServer.stopCleanly();
            System.exit(1);
        }
    }

    private static DeploymentInfo getCloudDeployment() {
        if(System.getenv().containsKey("KUBERNETES_SERVICE_PORT")) {
            return new K8SDeploymentInfo();
        }
        return new DebugDeploymentInfo("bedwars");
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