package entralinked.network.gamespy;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import entralinked.network.gamespy.request.GameSpyRequest;
import entralinked.serialization.GameSpyMessageFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class GameSpyRequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    
    protected final ObjectMapper mapper;
    protected final Map<String, Class<GameSpyRequest>> requestTypes;
    
    /**
     * Supplied {@link ObjectMapper} should be configured to use the {@link GameSpyMessageFactory}
     */
    public GameSpyRequestDecoder(ObjectMapper mapper, Map<String, Class<GameSpyRequest>> requestTypes) {
        this.mapper = mapper;
        this.requestTypes = requestTypes;
    }
    
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // Sanity check
        byte b = in.readByte();
        
        if(b != '\\') {
            throw new IOException("Was expecting '\\', got '%s'.".formatted((char)b));
        }
        
        // Get request type
        String typeName = parseString(in, false);
        Class<GameSpyRequest> requestType = requestTypes.get(typeName);
        
        if(requestType == null) {
            throw new IOException("Invalid or unimplemented request type '%s'".formatted(typeName));
        }
        
        // Parse request value (?) if any bytes are remaining
        if(in.readableBytes() > 0) {
            parseString(in, true);
        }
        
        // If there are still bytes left, use ObjectMapper to parse and map them.
        // Otherwise, create empty instance using reflection.
        GameSpyRequest request = null;
        
        if(in.readableBytes() > 0) {
            byte[] bytes = new byte[in.readableBytes() + 1];
            bytes[0] = '\\'; // Cuz it was read as a terminator earlier..
            in.readBytes(bytes, 1, bytes.length - 1);
            request = mapper.readValue(bytes, requestType);
        } else {
            request = requestType.getConstructor().newInstance();
        }
        
        out.add(request);
    }
    
    private String parseString(ByteBuf in, boolean allowEOI) {
        StringBuilder builder = new StringBuilder();
        byte b = 0;
        
        while((b = in.readByte()) != '\\') {
            builder.append((char)b);
            
            if(allowEOI && in.readableBytes() == 0) {
                break;
            }
        }
        
        return builder.toString();
    }
}