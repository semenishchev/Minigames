package me.mrfunny.minigame.deployment.info;

public class PterodactylDeploymentInfo implements DeploymentInfo {
    @Override
    public String getMinigameType() {
        return System.getenv("MINIGAME_TYPE");
    }

    @Override
    public String getServerId() {
        return System.getenv("P_SERVER_UUID").substring(0, 8);
    }

    @Override
    public String getServerHost() {
        return System.getenv("SERVER_IP");
    }

    @Override
    public int getServerPort() {
        return Integer.parseInt(System.getenv("SERVER_PORT"));
    }
}
