package com.github.venth.training.webflux.grpc;

import com.github.venth.training.webflux.grpc.grpc.GrpcConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.github.venth.training.webflux.grpc.jackson.JacksonConfiguration;
import com.github.venth.training.webflux.grpc.cookie.CookieConfiguration;

@SpringBootApplication(
        scanBasePackageClasses = {
                JacksonConfiguration.class,
                GrpcConfiguration.class,
                CookieConfiguration.class,
        }
)
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
