package com.github.venth.training.webflux.grpc.cookie

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.venth.training.webflux.grpc.api.cookie.Cookie
import io.reactivex.Flowable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles('test')
class CookieBakerControllerTest extends Specification {

    def "fetches baked cookies"() {
        when:
            def cookies = Flowable.fromIterable(cookieBaker.get()
                    .uri('/bake')
                    .accept(MediaType.APPLICATION_STREAM_JSON)
                    .retrieve()
                    .bodyToFlux(Cookie.class)
                    .toIterable()
            )
                    .test().await()
        then:
            cookies.assertValueCount(CookieBakerController.COOKIES_LIMIT as int)
    }

    void setup() {
        cookieBaker =  WebClient.getMethod('builder').invoke(WebClient.class)
                .baseUrl("http://localhost:${properties.port}")
                .exchangeStrategies(ExchangeStrategies.getMethod('builder').invoke(ExchangeStrategies.class)
                        .codecs({ configurer ->
                                configurer.defaultCodecs()
                                        .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_STREAM_JSON, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8))
                                configurer.defaultCodecs()
                                        .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_STREAM_JSON, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8))
                        })
                        .build()
                )
                .build()
    }

    WebClient cookieBaker

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ServerProperties properties
}
