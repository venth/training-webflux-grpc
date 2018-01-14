package com.github.venth.training.webflux.grpc.cookie

import com.github.venth.training.webflux.grpc.api.cookie.BakeRequest
import com.github.venth.training.webflux.grpc.api.cookie.RxCookieBakerGrpc
import com.google.protobuf.util.JsonFormat
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.SslProvider
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.reactivex.Single
import org.lognet.springboot.grpc.autoconfigure.GRpcServerProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles('test')
class CookieBakerGrpcServiceTest extends Specification {

    private static final Logger LOG = LoggerFactory.getLogger(CookieBakerGrpcServiceTest)

    def "fetches requested baked cookies"() {
        given:
            def requestedCookieNumber = 10
        when:
            def cookies = cookieBaker.bake(Single.just(BakeRequest.newBuilder().setNumberOfCookies(requestedCookieNumber).build()))
                    .doOnNext({ LOG.debug(JsonFormat.print(it)) })
                    .test()
        then:
            cookies.await()
                    .assertValueCount(requestedCookieNumber)
    }

    void setup() {
        def channel = NettyChannelBuilder.forAddress('127.0.0.1', gRpcServerProperties.port)
                .usePlaintext(false)
                .negotiationType(NegotiationType.TLS)
                .sslContext(GrpcSslContexts.configure(SslContextBuilder.forClient(), SslProvider.OPENSSL)
                        .trustManager(InsecureTrustManagerFactory.INSTANCE)
                        .build())
                .build()
        cookieBaker = RxCookieBakerGrpc.newRxStub(channel)
    }

    @Autowired
    GRpcServerProperties gRpcServerProperties

    RxCookieBakerGrpc.RxCookieBakerStub cookieBaker
}
