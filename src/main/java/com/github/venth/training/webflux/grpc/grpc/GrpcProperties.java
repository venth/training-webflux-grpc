package com.github.venth.training.webflux.grpc.grpc;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "grpc")
class GrpcProperties {

    private Ssl ssl;

    public Ssl getSsl() {
        return ssl;
    }

    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    static class Ssl {

        private Resource serverCertificate;

        private Resource serverKey;

        public Resource getServerCertificate() {
            return serverCertificate;
        }

        public void setServerCertificate(Resource serverCertificate) {
            this.serverCertificate = serverCertificate;
        }

        public Resource getServerKey() {
            return serverKey;
        }

        public void setServerKey(Resource serverKey) {
            this.serverKey = serverKey;
        }
    }

}
