package entralinked.network.dns;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DefaultDnsQuestion;
import io.netty.handler.codec.dns.DefaultDnsRawRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsSection;

public class DnsQueryHandler extends SimpleChannelInboundHandler<DatagramDnsQuery> {
    
    private static final Logger logger = LogManager.getLogger();
    private final InetAddress hostAddress;
    
    public DnsQueryHandler(InetAddress hostAddress) {
        this.hostAddress = hostAddress;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsQuery query) throws Exception {
        DefaultDnsQuestion question = query.recordAt(DnsSection.QUESTION);
        DnsRecordType type = question.type();
        
        // We only need type A (32 bit IPv4) for the DS
        if(type != DnsRecordType.A) {
            logger.warn("Unsupported record type in DNS question: {}", type);
            return;
        }
        
        ByteBuf addressBuffer = Unpooled.wrappedBuffer(hostAddress.getAddress());
        DefaultDnsRawRecord answer = new DefaultDnsRawRecord(question.name(), DnsRecordType.A, 0, addressBuffer);
        DatagramDnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
        response.addRecord(DnsSection.ANSWER, answer);
        ctx.writeAndFlush(response);
    }
}
