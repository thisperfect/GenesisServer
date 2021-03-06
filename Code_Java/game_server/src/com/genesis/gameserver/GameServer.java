package com.genesis.gameserver;

import com.genesis.core.akka.Akka;
import com.genesis.servermsg.core.config.ServerConfig;
import com.genesis.servermsg.core.isc.ISCActorSupervisor;
import com.genesis.servermsg.core.isc.remote.RemoteServerConfig;
import com.genesis.gameserver.core.config.GameServerConfig;
import com.genesis.gameserver.core.global.Globals;
import com.genesis.gameserver.server.ServerManagerActor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import akka.actor.Props;

public class GameServer {

    private static Logger log = LoggerFactory.getLogger(GameServer.class);

    public static void main(String[] args) {
        try {
            // 加载配置文件并初始化本物理服务器全局数据
            Globals.init();

            connectToAgentServer();

            connectToDataServer();

            // 启动Akka并创建本物理服务器的逻辑服务器管理器
            Akka akka = Globals.getAkka();
            akka.getActorSystem().actorOf(
                    Props.create(ServerManagerActor.class, GameServerConfig.getGameServerConfig()),
                    ServerManagerActor.ACTOR_NAME);

            log.info("GameServer start OK！----------------------------------------------------");

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(-1);
        }

    }

    private static void connectToDataServer() {
        List<ServerConfig> dataConfigs = GameServerConfig.getConnectedDataConfig();
        if (dataConfigs == null || dataConfigs.isEmpty()) {
            log.error("Game server can not start with no data server connected!");
            shutdown();
        }

        for (ServerConfig config : dataConfigs) {
            if (!Globals.getRemoteActorManager().connectRemote(
                    new RemoteServerConfig(config.serverType, config.serverId, config.akkaConfig.ip,
                            config.akkaConfig.port), ISCActorSupervisor.ACTOR_NAME)) {
                log.error(
                        "GameServer Start Failed! Can not connect to DataServer id: [{}], ip: [{}], port: [{}]",
                        config.serverId, config.akkaConfig.ip, config.akkaConfig.port);
                shutdown();
            }
        }
    }

    private static void connectToAgentServer() {
        ServerConfig agentConfig = GameServerConfig.getConnectedAgentConfig();
        if (!Globals.getRemoteActorManager().connectRemote(
                new RemoteServerConfig(agentConfig.serverType, agentConfig.serverId,
                        agentConfig.akkaConfig.ip, agentConfig.akkaConfig.port),
                ISCActorSupervisor.ACTOR_NAME)) {
            log.error(
                    "GameServer Start Failed! Can not connect to AgentServer id: [{}], ip: [{}], port: [{}]",
                    agentConfig.serverId, agentConfig.akkaConfig.ip, agentConfig.akkaConfig.port);
            shutdown();
        }
    }

    private static void shutdown() {
        Globals.getAkka().getActorSystem().terminate();
        System.exit(-1);
    }

}
