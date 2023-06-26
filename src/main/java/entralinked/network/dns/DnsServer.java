package entralinked.network.dns;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import entralinked.network.NettyServerBase;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import io.netty.handler.codec.dns.DatagramDnsResponseEncoder;

public class DnsServer extends NettyServerBase {
        
    private static final Logger logger = LogManager.getLogger();
    private InetAddress hostAddress;
    
    public DnsServer(InetAddress hostAddress) {
        super("DNS", 53);
        this.hostAddress = hostAddress;
        logger.info("DNS queries will be resolved to {}", hostAddress.getHostAddress());
    }
    
    @Override
    public ChannelFuture bootstrap(int port) {
        return new Bootstrap()
                .group(eventLoopGroup)
                .channel(usingEpoll ? EpollDatagramChannel.class : NioDatagramChannel.class)
                .handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel channel) throws Exception {
                channel.pipeline().addLast(new DatagramDnsQueryDecoder());
                channel.pipeline().addLast(new DatagramDnsResponseEncoder());
                channel.pipeline().addLast(new DnsQueryHandler(hostAddress));
            }
        }).bind(port).awaitUninterruptibly();
    }
}
