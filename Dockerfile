FROM ghcr.io/navikt/baseimages/temurin:19
ENV APPD_ENABLED=true
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice

COPY init.sh /init-scripts/init.sh

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local|*oera-q.local|*.oera.no|*.intern.nav.no|*.svc.nais.local \
               -Xmx1024M \
               -Xms512M"
