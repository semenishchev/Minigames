package me.mrfunny.minigame.api.deployment.info;

public class K8SDeploymentInfo implements DeploymentInfo {
    @Override
    public String getMinigameType() {
        return System.getenv("MINIGAME_TYPE");
    }

    @Override
    public String getServerId() {
        return System.getenv("POD_ID");
    }

    @Override
    public String getServerHost() {
        return System.getenv("POD_IP");
    }

    @Override
    public int getServerPort() {
        return 25565;
    }

    @Override
    public String getRegion() {
        return System.getenv("SERVER_REGION");
    }

    @Override
    public String getLocation() {
        return System.getenv("SERVER_LOCATION");
    }
}
