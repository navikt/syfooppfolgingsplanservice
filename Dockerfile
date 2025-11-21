FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21@sha256:086efc0e24d318f47e804fc58b0b1ad5356bc05d27f2c174955f13c9a1efa6a8
LABEL org.opencontainers.image.source=https://github.com/navikt/syfooppfolgingsplanservice

USER apprunner
WORKDIR /app

COPY init.sh /init-scripts/init.sh
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Xmx1024M \
               -Xms512M"

ENTRYPOINT ["sh", "/init-scripts/init.sh"]
