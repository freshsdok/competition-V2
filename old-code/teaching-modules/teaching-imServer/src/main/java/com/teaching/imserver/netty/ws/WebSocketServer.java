package com.teaching.imserver.netty.ws;

import com.teaching.imserver.netty.IMChannelHandler;
import com.teaching.imserver.netty.IMServer;
import com.teaching.imserver.netty.ws.endecode.MessageProtocolDecoder;
import com.teaching.imserver.netty.ws.endecode.MessageProtocolEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * WS服务器,用于连接网页的客户端,协议格式: 直接IMSendInfo的JSON序列化
 *
 * @author Blue
 * @date 2022-11-20
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "websocket", value = "enable", havingValue = "true", matchIfMissing = false)
public class WebSocketServer implements IMServer {

    @Value("${websocket.port}")
    private int port;

    private volatile boolean ready = false;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;


    @Override
    public boolean isReady() {
        return ready;
    }

    @Override
    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        // 设置为主从线程模型
        bootstrap.group(bossGroup, workGroup)
                // 设置服务端NIO通信类型
                .channel(NioServerSocketChannel.class)
                // 设置ChannelPipeline，也就是业务职责链，由处理的Handler串联而成，由从线程池处理
                .childHandler(new ChannelInitializer<Channel>() {
                    // 添加处理的Handler，通常包括消息编解码、业务处理，也可以是日志、权限、过滤等
                    @Override
                    protected void initChannel(Channel ch) {
                        // 获取职责链
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast("http-codec", new HttpServerCodec());
                        pipeline.addLast("aggregator", new HttpObjectAggregator(65535));
                        pipeline.addLast("http-chunked", new ChunkedWriteHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler("/im"));
                        pipeline.addLast("encode", new MessageProtocolEncoder());
                        pipeline.addLast("decode", new MessageProtocolDecoder());
                        pipeline.addLast("handler", new IMChannelHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 5)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        try {
            // 绑定端口，启动select线程，轮询监听channel事件，监听到事件之后就会交给从线程池处理
            bootstrap.bind(port).sync().channel();
            // 就绪标志
            this.ready = true;
            log.info("websocket server 初始化完成,端口：{}", port);
            // 等待服务端口关闭
            //channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("websocket server 初始化异常", e);
        }
    }

    @Override
    public void stop() {
        if (bossGroup != null && !bossGroup.isShuttingDown() && !bossGroup.isShutdown()) {
            bossGroup.shutdownGracefully();
        }
        if (workGroup != null && !workGroup.isShuttingDown() && !workGroup.isShutdown()) {
            workGroup.shutdownGracefully();
        }
        this.ready = false;
        log.info("websocket server 停止");
    }


}
