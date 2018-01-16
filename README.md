# Overview

Combination of Webflux and gRPC to see how easy is to use both technologies.

# Plan

The evaluation of technology combination - webflux and gRPC wile be done by exposure of:
* rest service and
* gRPC service
at the same time.

I use bakery as a sample and exposed /bake via REST and /cookie.CookieBaker/bake via gRPC.

# Execution

## Contract

The first thing was to generate the webflux project. In oder to do it quickly I've used:
[Application generator from sping.io](https://start.spring.io).

After few seconds I got working project. Then I started to extend it a bit by: 
- contract; I could externalize the contract completely and then
use it in the project, but for simplicity I decided to embed the contract inside the project itself;
Following convention given by gradle protobuf plugin I placed the contract inside:
`src/main/proto`.

The proto file looks as follows:
```
syntax = "proto3";

package cookie;

option java_generic_services = true;
option java_multiple_files = true;
option java_package = "com.github.venth.training.webflux.grpc.api.cookie";
option java_outer_classname = "CookieBakerProto";

service CookieBaker {
    rpc bake (BakeRequest) returns (stream Cookie);
}

message BakeRequest {
    int64 numberOfCookies = 1;
}

message Cookie {
    int64 id = 1;
} 
```
- contract generation in build.gradle via gradle protobuf plugin and rxgrpc protobuf plugin
```
buildscript {
    ext {
        ver = [
                ...
                protobufPlugin              : '[0.8.1, 0.9.0)',
                ...
        ]
    }
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
        maven { url "https://repo.spring.io/snapshot" }
        maven { url "https://repo.spring.io/milestone" }
    }
    dependencies {
        classpath(
                ...
                "com.google.protobuf:protobuf-gradle-plugin:${ver.protobufPlugin}",
                ...
        )
    }
}

apply plugin: 'com.google.protobuf'
...

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${ver.protobuf}"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${ver.grpc}"
        }
        rxgrpc {
            artifact = "com.salesforce.servicelibs:rxgrpc:${ver.rxgrpc}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {
                // To generate deprecated interfaces and static bindService method,
                // turn the enable_deprecated option to true below:
                option 'enable_deprecated=false'
            }
            rxgrpc {}
        }
    }
}

```
## Versioning
- project versioning based on git tags via axion plugin
```
buildscript {
    ext {
        ver = [
                ...
                axionRelease                : '[1.8.1, 1.9.0)',
                ...
        ]
    }
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
        maven { url "https://repo.spring.io/snapshot" }
        maven { url "https://repo.spring.io/milestone" }
    }
    dependencies {
        classpath(
                ...
                "pl.allegro.tech.build:axion-release-plugin:${ver.axionRelease}",
                ...
        )
    }
}

...

apply plugin: 'pl.allegro.tech.build.axion-release'

...

```

## Services
### Serializarion / Deserialization Jackson with Protobuf
Finally two endpoints along with tests:
- CookieBakerController and
- CookieBakerGrpcService

Then I had first issue. I wanted to use the same cookie message described in proto file both for gRpc and Rest.
Jackson library states that it handles protobuf version 2. I used to describe the service 3th version of protobuf.

I googled a little and came to the conclusion that implementing 3th version of protobuf serialization and deserialization 
in Jackson isn't so hard.

I've started with serialization which seems to be easy. Protobuf has JsonFormat class which does the magic.

The serializer class looks like this:
```java
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
``` 

The deserialization was a more challenging:
```java
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
```

In order to use JsonFormat to deserialize json object via protobuf I had to get my fingers on raw json.
The string version of the json is available in TreeNode. The next challange was to create a builder for 
a proper protobuf message type - I used reflection to achieve this.

The last challenge here was to find all protobuf messages on classpath. I managed to do this by using reflections
library.

```java
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
```

As the result I can now use protobuf to describe services in gRpc and transport object for Rest.

Webflux works awesome and it seems that even usage of gRpc in webflux is easy as well.

### gRpc adaptation with RxGRpc from salesforce

To easier work with a service I prefer to have rx stream as an argument and as a result. Thanks to Salesforce I'm no loger forced
to write an adapter for each service or client call. 

- The magic is done with protobuf plugin - rxgrpc
```
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${ver.protobuf}"
    }
    plugins {
        grpc {
            artifact = "io.grpc:protoc-gen-grpc-java:${ver.grpc}"
        }
        rxgrpc {
            artifact = "com.salesforce.servicelibs:rxgrpc:${ver.rxgrpc}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {
                // To generate deprecated interfaces and static bindService method,
                // turn the enable_deprecated option to true below:
                option 'enable_deprecated=false'
            }
            rxgrpc {}
        }
    }
}
```

- and pointing out source location for IntelliJ
```
idea {
    module {
        ...
        
        sourceDirs += file("${buildDir}/generated/source/proto/main/java")
        generatedSourceDirs += files("${buildDir}/generated/source/proto/main/java")

        sourceDirs += file("${buildDir}/generated/source/proto/main/grpc")
        generatedSourceDirs += files("${buildDir}/generated/source/proto/main/grpc")

        sourceDirs += file("${buildDir}/generated/source/proto/main/rxgrpc")
        generatedSourceDirs += files("${buildDir}/generated/source/proto/main/rxgrpc")
        
        ...
    }
}
```