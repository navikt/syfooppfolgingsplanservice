FROM eclipse-temurin:21-jdk-noble
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice

COPY init.sh /init-scripts/init.sh

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Xmx1024M \
               -Xms512M"
