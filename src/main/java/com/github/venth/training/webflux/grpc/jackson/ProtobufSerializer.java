package com.github.venth.training.webflux.grpc.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.util.JsonFormat;

class ProtobufSerializer extends StdSerializer<GeneratedMessageV3> {

    private final JsonFormat.Printer printer;

    ProtobufSerializer() {
        super(GeneratedMessageV3.class);
        printer = JsonFormat.printer()
                .omittingInsignificantWhitespace();
    }

    @Override
    public Class<GeneratedMessageV3> handledType() {
        return GeneratedMessageV3.class;
    }

    @Override
    public void serialize(GeneratedMessageV3 value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeRawValue(printer.print(value));
    }
}
