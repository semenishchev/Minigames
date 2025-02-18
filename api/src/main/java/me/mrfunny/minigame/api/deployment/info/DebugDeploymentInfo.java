package me.mrfunny.minigame.api.deployment.info;

import java.util.UUID;

public class DebugDeploymentInfo implements DeploymentInfo {
    private final String minigame;

    public DebugDeploymentInfo(String minigame) {
        this.minigame = minigame;
    }
    @Override
    public String getMinigameType() {
        return this.minigame;
    }

    @Override
    public String getServerId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public String getServerHost() {
        return "0.0.0.0";
    }

    @Override
    public int getServerPort() {
        return 25565;
    }
}
