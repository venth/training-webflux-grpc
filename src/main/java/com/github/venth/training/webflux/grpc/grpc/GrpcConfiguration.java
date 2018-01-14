package com.github.venth.training.webflux.grpc.grpc;

import java.io.IOException;

import io.grpc.ServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
@EnableConfigurationProperties(GrpcProperties.class)
public class GrpcConfiguration {

    @Bean
    GRpcServerBuilderConfigurer serverBuilderConfigurer(GrpcProperties properties) {
        return new GRpcServerBuilderConfigurer() {
            @Override
            public void configure(ServerBuilder<?> serverBuilder) {
                super.configure(serverBuilder);
                try {
                    ((NettyServerBuilder)serverBuilder)
                        .sslContext(sslContextConfiguration(properties));
                } catch (IOException e) {
                    throw new RuntimeException("SSL cert or key files are missing", e);
                }
            }
        };
    }

    private SslContext sslContextConfiguration(GrpcProperties properties) throws IOException {
        GrpcProperties.Ssl ssl = properties.getSsl();
        Resource serverCertificate = ssl.getServerCertificate();
        Resource serverKey = ssl.getServerKey();
        return GrpcSslContexts
                .configure(
                    SslContextBuilder.forServer(serverCertificate.getFile(), serverKey.getFile()),
                    SslProvider.OPENSSL
                ).startTls(true)
                .build();
    }
}
