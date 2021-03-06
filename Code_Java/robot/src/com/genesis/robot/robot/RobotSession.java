package com.genesis.robot.robot;

import com.genesis.robot.login.Status;
import com.google.protobuf.GeneratedMessage;

import com.genesis.servermsg.core.isc.session.ISession;
import com.genesis.core.net.msg.SCMessage;
import com.genesis.protobuf.MessageType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

public class RobotSession implements ISession {

    private static final Logger log = LoggerFactory.getLogger(RobotSession.class);

    private ChannelHandlerContext ctx;

    /**登陆状态*/
    private Status status = Status.Init;

    public RobotSession(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public void disconnect() {
        ctx.disconnect();
    }

    @Override
    public void sendMessage(GeneratedMessage msg) {
        SCMessage csMsg = new SCMessage(
                msg.getDescriptorForType().getOptions().getExtension(MessageType.cgMessageType)
                        .getNumber(), msg.toByteArray());
        ctx.writeAndFlush(csMsg);
        if (log.isDebugEnabled()) {
            log.debug("[[[Send message]]]: id - {}, code - {}, type - {}", getId(),
                    csMsg.messageType, MessageType.CGMessageType.valueOf(csMsg.messageType));
        }
    }

    @Override
    public long getId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T extends com.google.protobuf.GeneratedMessage.Builder<T>> void sendMessage(
            com.google.protobuf.GeneratedMessage.Builder<T> msg) {
        SCMessage csMsg = new SCMessage(
                msg.getDescriptorForType().getOptions().getExtension(MessageType.cgMessageType)
                        .getNumber(), msg.build().toByteArray());
        ctx.writeAndFlush(csMsg);
        if (log.isDebugEnabled()) {
            log.debug("[[[Send message]]]: id - {}, code - {}, type - {}", getId(),
                    csMsg.messageType, MessageType.CGMessageType.valueOf(csMsg.messageType));
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
