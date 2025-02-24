package me.mrfunny.minigame.api.balancer.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.mrfunny.minigame.api.balancer.LoadBalancerClient;
import me.mrfunny.minigame.api.deployment.Deployment;
import me.mrfunny.minigame.api.deployment.info.DeploymentInfo;
import me.mrfunny.minigame.api.errors.UserException;
import me.mrfunny.minigame.api.storage.StorageMap;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class OlekRedisBalancerClient extends LoadBalancerClient {
    private final static Logger LOGGER = LoggerFactory.getLogger("Redis Balancer");
    private final Jedis jedis;
    private final String serverId;
    private final HttpServer httpServer;
    private final ObjectMapper JSON = new ObjectMapper(new JsonFactory());

    public OlekRedisBalancerClient(Deployment requestHandler) throws IOException {
        super(requestHandler);
        this.jedis = new Jedis(System.getenv("REDIS_ADDRESS"));
        jedis.connect();
        this.serverId = requestHandler.getDeploymentInfo().getServerId();
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", 3000), 0);
        httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        httpServer.createContext("/probe").setHandler(ctx -> {
            respond(ctx, 200, "ok");
        });
        httpServer.createContext("/new-instance").setHandler(ctx -> {
            try {
                Headers headers = ctx.getRequestHeaders();
                Map<String, String> data = new HashMap<>();
                for(Map.Entry<String, List<String>> entries : headers.entrySet()) {
                    String key = entries.getKey();
                    if(!key.startsWith("Balancer-")) continue;
                    data.put(key.replace("Balancer-", "").toLowerCase(), entries.getValue().getFirst());
                }
                requestHandler.createInstance(headers.getFirst("subtype"), data);
                respond(ctx, 200, "ok");
            } catch(Exception e) {
                LOGGER.error("Failed to create a new instance", e);
                respond(ctx, 500, e.getMessage());
            }
        });
        httpServer.createContext("/destroy-instance").setHandler(ctx -> {
            try {
                Headers headers = ctx.getRequestHeaders();
                UUID uuid = UUID.fromString(headers.getFirst("Balancer-Instance-Id"));
                requestHandler.destroyInstance(uuid);
                respond(ctx, 200, "ok");
            } catch(Exception e) {
                LOGGER.error("Failed to destroy a new instance", e);
                respond(ctx, 500, e.getMessage());
            }
        });
        httpServer.createContext("/available-instance").setHandler(ctx -> {
            try {
                Headers headers = ctx.getRequestHeaders();
                String subtype = headers.getFirst("Balancer-Subtype");
                Map<String, String> data = new HashMap<>();
                Set<UUID> players = new HashSet<>();
                for(Map.Entry<String, List<String>> entries : headers.entrySet()) {
                    String key = entries.getKey();
                    if(!key.startsWith("Balancer-")) continue;
                    String withoutTag = key.replace("Balancer-", "");
                    data.put(withoutTag.toLowerCase(), entries.getValue().getFirst());
                    if(withoutTag.startsWith("Player-")) {
                        players.add(UUID.fromString(withoutTag.replace("Player-", "")));
                    }
                }
                try {
                    UUID availableInstance = requestHandler.getAvailableInstanceOfType(subtype, data, players);
                    if(availableInstance == null) {
                        respond(ctx, 404, "No available instance");
                        return;
                    }
                    respond(ctx, 200, availableInstance.toString());
                } catch (UserException e) {
                    respond(ctx, 400, e.getMessage());
                } catch (Exception e) {
                    respond(ctx, 500, "An internal server error occurred");
                    LOGGER.error("Failed to find an available instance due to an internal error", e);
                }
            } catch(Exception e) {
                LOGGER.error("Failed to retrieve available instance", e);
                respond(ctx, 500, "Failed handling request");
            }
        });
        httpServer.createContext("/total-players").setHandler(ctx -> {
            try {
                respond(ctx, 200, String.valueOf(requestHandler.getTotalPlayers()));
            } catch(Exception e) {
                LOGGER.error("Failed to get total players", e);
                respond(ctx, 500, e.getMessage());
            }
        });
        httpServer.createContext("/stop-server").setHandler(ctx -> {
            try {
                requestHandler.stopServer();
                respond(ctx, 200, "ok");
            } catch(Exception e) {
                LOGGER.error("Failed answering to stop server. Stopping brutally", e);
                respond(ctx, 500, e.getMessage());
                System.exit(-1);
            }
        });
        httpServer.createContext("/server-type").setHandler(ctx -> {
            try {
                respond(ctx, 200, requestHandler.getServerType());
            } catch(Exception e) {
                LOGGER.error("Failed getting server type", e);
                respond(ctx, 500, e.getMessage());
            }
        });
    }

    private void respond(HttpExchange exchange, int code, String content) throws IOException {
        exchange.sendResponseHeaders(code, content.length());
        try(OutputStream os = exchange.getResponseBody()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public @Nullable UUID getInstanceOf(UUID player) {
        return UUID.fromString(jedis.get("player_instance_" + player));
    }

    @Override
    public void markCanBeDisabled() {
        jedis.publish("balancer_server_status", "can_be_disabled:" + this.serverId);
    }

    @Override
    public void reportNewInstanceId(String subtype, UUID instanceId) {
        jedis.publish("balancer_instance_status", "new_instance:" + this.serverId + ":" + instanceId + ":" + subtype);
    }

    @Override
    public void markInstanceDone(UUID instanceId) {
        jedis.publish("balancer_instance_status", "done:" + this.serverId + ":" + instanceId);
    }

    @Override
    public void markInstanceDestroyed(UUID instanceId, String reason) {
        jedis.publish("balancer_instance_status", "destroyed:" + this.serverId + ":" + instanceId + ":" + reason);
    }

    @Override
    public void markPlayerConnected(UUID instanceId, UUID player) {
        jedis.publish("player_connection", "connected_to_instance:" + this.serverId + ":" + instanceId + ":" + player);
    }

    @Override
    public void serverKeepalive() {
        jedis.publish("balancer_server_status", "keepalive:" + this.serverId);
    }

    @Override
    public void markServerStarted() {
        httpServer.start();
        DeploymentInfo info = this.requestHandler.getDeploymentInfo();
        BalancedServerInfo data = new BalancedServerInfo(
            this.serverId,
            info.getServerHost() + ":" + info.getServerPort(),
            info.getMinigameType(),
            info.getRegion(),
            info.getLocation(),
            0,
            true
        );
        String value;
        try {
            value = JSON.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            jedis.publish("balancer_server_status", "error:" + serverId + ":" + e.getMessage());
            throw new RuntimeException(e);
        }
        jedis.set("balancer_server_data", value);
        jedis.publish("balancer_server_status", "started:" + this.serverId);
    }

    @Override
    public void markServerStopped(String error) {
        httpServer.stop(0);
        jedis.publish("balancer_server_status", "stopped:" + this.serverId);
        jedis.close();
    }

    @Override
    public void reportError(String error) {
        jedis.publish("balancer_error", error);
    }

    @Override
    public void sendPlayer(UUID player, String serverId, UUID instanceId) {
        jedis.publish("player_connection", "connect:" + serverId + ":" + instanceId + ":" + player);
    }

    @Override
    public void kickPlayer(UUID player, Component reason) {
        // todo: serializer of reason
        jedis.publish("player_connection", "kick:" + player);
    }

    @Override
    public Set<String> getPlayerPermissionNodes(UUID player) {
        return jedis.smembers("permissions_" + player);
    }

    @Override
    public boolean isBanned(UUID player) {
        return jedis.sismember("banned_players", player.toString());
    }

    @Override
    public void banPlayer(UUID player, Duration duration, String reason) {
        jedis.sadd("banned_players", player.toString());
        jedis.publish("player_connection", "ban:" + player);
    }

    @Override
    public StorageMap getGlobalPlayerData(UUID player) {

        return null; // todo
    }

    @Override
    public StorageMap getPlayerData(UUID player, String collection) {
        return null; // todo
    }

    @Override
    public void updateGlobalPlayerData(UUID player, StorageMap data) {
        // todo
    }

    @Override
    public void updatePlayerData(UUID player, String collection, StorageMap data) {
        // todo
    }

    @Override
    public CompletableFuture<String> requestGameServer(UUID player, String minigameType, String subtype, Map<String, String> data) {
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BalancedServerInfo {
        public String id;
        @JsonProperty("id")
        public String ipAddr;
        public String serverType;
        public String region;
        public String location;
        public int playerCount;
        public boolean available;
        public BalancedServerInfo() {

        }
        public BalancedServerInfo(String id, String ipAddr, String serverType, String region, String location, int playerCount, boolean available) {
            this.id = id;
            this.ipAddr = ipAddr;
            this.serverType = serverType;
            this.region = region;
            this.location = location;
            this.playerCount = playerCount;
            this.available = available;
        }
    }
}
