package com.github.venth.training.webflux.grpc

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class ServiceApplicationTest extends Specification {
    def 'loads context'() {
        expect:
            true
    }
}
