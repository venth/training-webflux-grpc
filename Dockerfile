FROM openjdk:9-jre-slim

RUN mkdir -p /var/external/deps && \
    mkdir -p /var/service && \
    mkdir -p /var/external/container-libs

COPY ./build/libs/ /var/service/
COPY ./build/container-libs/ /var/external/container-libs/

RUN find /var/service -maxdepth 1 -name '*.jar' | grep -v 'javadoc' | grep -v 'sources' | xargs -I {} ln -sf {} /var/service/deps.jar && \
    mkdir /var/service/unpacked && \
    unzip -oq /var/service/deps.jar -d /var/service/unpacked && \
    find /var/service/unpacked/BOOT-INF/lib/ -name 'netty-tcpnative*.jar' -exec rm {} \; && \
    (mv /var/external/container-libs/*.jar /var/service/unpacked/BOOT-INF/lib/ || true) && \
    rm -f /var/service/*.jar

WORKDIR /var/service/unpacked

VOLUME /var/external/deps

EXPOSE 8080 8081 6565

CMD ["/bin/sh", "-c", "(cp /var/external/deps/*.jar /var/service/unpacked/BOOT-INF/lib/ || true) && java $JAVA_OPTS org.springframework.boot.loader.JarLauncher" ]

