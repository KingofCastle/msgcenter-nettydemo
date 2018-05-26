package com.qixu.msgcenter.ws;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

@ChannelHandler.Sharable
public class BinaryWebSocketFrameEncoder extends MessageToMessageEncoder<MessageLiteOrBuilder> {
    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out)
            throws Exception {
        if (msg instanceof MessageLite) {
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(wrappedBuffer(((MessageLite) msg).toByteArray()));
            BinaryWebSocketFrame bwf = new BinaryWebSocketFrame(byteBuf);
            out.add(bwf);
            return;
        }
        if (msg instanceof MessageLite.Builder) {
            ByteBuf byteBuf = Unpooled.buffer();
            byteBuf.writeBytes(wrappedBuffer(((MessageLite.Builder) msg).build().toByteArray()));
            BinaryWebSocketFrame bwf = new BinaryWebSocketFrame(byteBuf);
            out.add(bwf);
        }
    }
}