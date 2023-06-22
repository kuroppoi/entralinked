package entralinked.network.gamespy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.network.gamespy.message.GameSpyMessage;
import entralinked.serialization.GameSpyMessageFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GameSpyMessageEncoder extends MessageToByteEncoder<Object> {
    
    protected final ObjectMapper mapper;
    
    /**
     * Supplied {@link ObjectMapper} should be configured to use the {@link GameSpyMessageFactory}
     */
    public GameSpyMessageEncoder(ObjectMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        Class<?> type = in.getClass();
        GameSpyMessage messageInfo = type.getAnnotation(GameSpyMessage.class);
        
        if(messageInfo == null) {
            throw new IOException("Outbound message type '%s' must have the GameSpyMessage annotation.".formatted(type.getName()));
        }
        
        out.writeByte('\\');
        writeString(out, messageInfo.name());
        out.writeByte('\\');
        writeString(out, messageInfo.value());
        out.writeBytes(mapper.writeValueAsBytes(in));
        writeString(out, "\\final\\");
    }
    
    private void writeString(ByteBuf out, String string) {
        out.writeCharSequence(string, StandardCharsets.UTF_8);
    }
}
