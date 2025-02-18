package me.mrfunny.minigame.api.deployment.info;

public interface DeploymentInfo {
    String getMinigameType();
    String getServerId();
    String getServerHost();
    int getServerPort();
    String getRegion();
    String getLocation();
}
