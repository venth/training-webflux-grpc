package com.github.venth.training.webflux.grpc.grpc;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "grpc")
@Getter @Setter
class GrpcProperties {

    private Ssl ssl;

    @Getter @Setter
    static class Ssl {

        private Resource serverCertificate;

        private Resource serverKey;
    }

}
