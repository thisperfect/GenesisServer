package com.genesis.servermsg.core.isc.msg;

import com.genesis.servermsg.core.isc.ServerType;

import java.io.Serializable;

/**
 * 注册本地ActorRef的组装类到远程Server中所使用的消息。
 *
 * @author pangchong
 *
 */
public class RegisterToRemote implements Serializable {

    private static final long serialVersionUID = 1L;

    public final ServerType serverType;
    public final int serverId;

    public RegisterToRemote(ServerType serverType, int serverId) {
        this.serverType = serverType;
        this.serverId = serverId;
    }

}
