package com.github.venth.training.webflux.grpc.jackson;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.google.protobuf.GeneratedMessageV3;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Collections.singletonList;

@Configuration
public class JacksonConfiguration {

    @Bean
    public Module protobufModule() {
        SimpleModule module = new SimpleModule("protobufModule");

        Map<Class<?>, JsonDeserializer<?>> deserializers = new Reflections("com.github.venth.training.webflux.grpc")
                .getSubTypesOf(GeneratedMessageV3.class)
                .stream()
                .collect(Collectors.toMap(clazz -> clazz, ProtobufDeserializer::new));

        module.setDeserializers(new SimpleDeserializers(deserializers));
        module.setSerializers(new SimpleSerializers(singletonList(new ProtobufSerializer())));

        return module;
    }
}
