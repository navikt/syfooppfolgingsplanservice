FROM gcr.io/distroless/java21
ENV TZ="Europe/Oslo"
ENV JDK_JAVA_OPTIONS="-Djava.security.egd=file:/dev/./urandom \
                                                         -Dspring.profiles.active=remote \
                                                         -Xmx1024M \
                                                         -Xms512M"
WORKDIR /app
COPY build/libs/*.jar app.jar
COPY init.sh /init-scripts/init.sh
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice


ENTRYPOINT ["java", "-jar", "app.jar"]
