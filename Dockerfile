FROM ghcr.io/navikt/baseimages/temurin:21
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice

USER root
RUN apt remove wget -y
USER apprunner
COPY init.sh /init-scripts/init.sh

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Xmx1024M \
               -Xms512M"
