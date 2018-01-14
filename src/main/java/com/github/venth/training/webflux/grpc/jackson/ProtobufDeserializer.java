package com.github.venth.training.webflux.grpc.jackson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.util.JsonFormat;

class ProtobufDeserializer extends StdDeserializer<GeneratedMessageV3> {

    private final JsonFormat.Parser parser;

    private final Method builderFactory;

    public ProtobufDeserializer(Class<? extends GeneratedMessageV3> clazz) {
        super(clazz);
        parser = JsonFormat.parser().ignoringUnknownFields();
        try {
            builderFactory = clazz.getMethod("newBuilder");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Missing newBuilder method expected in protobuf class: " + clazz.getName(), e);
        }
    }

    @Override
    public Class<?> handledType() {
        return GeneratedMessageV3.class;
    }

    @Override
    public GeneratedMessageV3 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        GeneratedMessageV3.Builder builder = newBuilder();
        TreeNode node = treeNode(p);
        parser.merge(node.toString(), builder);
        return (GeneratedMessageV3) builder.build();
    }

    private TreeNode treeNode(JsonParser p) throws IOException {
        ObjectCodec codec = p.getCodec();
        return codec.readTree(p);
    }

    private GeneratedMessageV3.Builder newBuilder() {
        GeneratedMessageV3.Builder newBuilder;
        try {
            newBuilder = (GeneratedMessageV3.Builder) builderFactory.invoke(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
        return newBuilder;
    }
}
