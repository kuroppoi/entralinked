package entralinked.network;

import java.util.concurrent.ThreadFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;

public abstract class NettyServerBase {
    
    private static final Logger logger = LogManager.getLogger();
    protected final ThreadFactory threadFactory;
    protected final EventLoopGroup eventLoopGroup;
    protected final String name;
    protected final int port;
    protected boolean usingEpoll;
    protected boolean started;
    
    public NettyServerBase(String name, int port) {
        this.threadFactory = new DefaultThreadFactory(name);
        this.name = name;
        this.port = port;
        
        if(Epoll.isAvailable()) {
            eventLoopGroup = new EpollEventLoopGroup(threadFactory);
            usingEpoll = true;
        } else {
            eventLoopGroup = new NioEventLoopGroup(threadFactory);
        }
    }
    
    protected abstract ChannelFuture bootstrap(int port);
    
    public boolean start() {
        if(started) {
            return true;
        }
        
        logger.info("Staring {} server ...", name);
        ChannelFuture future = bootstrap(port);
        
        if(!future.isSuccess()) {
            logger.error("Could not start {} server", name, future.cause());
            return false;
        }
        
        logger.info("{} server listening @ port {}", name, port);
        started = true;
        return true;
    }
    
    public boolean stop() {
        if(!started) {
            return true;
        }
        
        logger.info("Stopping {} server ...", name);
        Future<?> future = eventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        
        if(!future.isSuccess()) {
            logger.info("Could not stop {} server", name, future.cause());
            return false;
        }
        
        logger.info("{} server stopped", name);
        started = false;
        return true;
    }
}
