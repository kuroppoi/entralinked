package entralinked.network.gamespy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.jsontype.NamedType;

import entralinked.Entralinked;
import entralinked.network.NettyServerBase;
import entralinked.network.gamespy.request.GameSpyRequest;
import entralinked.serialization.GameSpyMessageFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;

public class GameSpyServer extends NettyServerBase {
    
    private static final Logger logger = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper(new GameSpyMessageFactory());
    private static final Map<String, Class<GameSpyRequest>> requestTypes = new HashMap<>();
    private final EventExecutorGroup handlerGroup = new DefaultEventExecutor(threadFactory);
    private final Entralinked entralinked;
    
    static {
        logger.info("Mapping GameSpy request types ...");
        DeserializationConfig config = mapper.getDeserializationConfig();
        AnnotatedClass annotated = AnnotatedClassResolver.resolveWithoutSuperTypes(config, GameSpyRequest.class);
        Collection<NamedType> types = mapper.getSubtypeResolver().collectAndResolveSubtypesByClass(config, annotated);
        
        for(NamedType type : types) {
            if(type.hasName()) {
                requestTypes.put(type.getName(), (Class<GameSpyRequest>)type.getType());
            }
        }
    }
    
    public GameSpyServer(Entralinked entralinked) {
        super("GameSpy", 29900);
        this.entralinked = entralinked;
    }
    
    @Override
    public ChannelFuture bootstrap(int port) {
        return new ServerBootstrap()
                .group(eventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new DelimiterBasedFrameDecoder(512, Unpooled.wrappedBuffer("\\final\\".getBytes())));
                pipeline.addLast(new GameSpyRequestDecoder(mapper, requestTypes));
                pipeline.addLast(new GameSpyMessageEncoder(mapper));
                pipeline.addLast(handlerGroup, new GameSpyHandler(entralinked));
            }
        }).bind(port).awaitUninterruptibly();
    }
}
