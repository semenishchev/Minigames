package me.mrfunny.minigame.balancer.impl;

import me.mrfunny.minigame.balancer.LoadBalancerClient;
import me.mrfunny.minigame.deployment.Deployment;

import java.util.UUID;

public class DebugBalancerClient extends LoadBalancerClient {
    public DebugBalancerClient(Deployment requestHandler) {
        super(requestHandler);
    }

    @Override
    public void markCanBeDisabled() {

    }

    @Override
    public void reportNewInstanceId(String subtype, String instanceId) {

    }

    @Override
    public void markInstanceDone(String instanceId) {

    }

    @Override
    public void markInstanceDestroyed(String instanceId, String reason) {

    }

    @Override
    public void markPlayerConnected(String instanceId, UUID player) {

    }

    @Override
    public void serverKeepalive(String serverId) {

    }

    @Override
    public void markServerStarted(String serverId) {

    }

    @Override
    public void markServerStopped(String serverId, String error) {

    }

    @Override
    public void reportError(String error) {

    }
}
