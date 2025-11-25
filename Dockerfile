FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice

USER nonroot
WORKDIR /app

COPY init.sh /init-scripts/init.sh
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Xmx1024M \
               -Xms512M"

ENTRYPOINT ["/bin/sh", "-c", ". /init-scripts/init.sh && exec java $JAVA_OPTS -jar app.jar"]
